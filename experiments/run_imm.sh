#!/bin/bash
SCRIPT_DIR=$( cd $( dirname $0 ) && pwd )

export SKIP_MOP="false"
export SKIP_TRACK="true"
export SKIP_GEN="false"
export SKIP_IMM="false"
export SKIP_IMOP="true"
export SKIP_EMOP="false"
export SKIP_IMM_IMOP="true"
export SKIP_IMM_EMOP="false"
export SKIP_UNSAFE="false"
export SKIP_EVO_GEN="false"

while getopts :m:t:g:d:i:e:p:x:u:k: opts; do
    case "${opts}" in
        m ) SKIP_MOP="${OPTARG}" ;;
        t ) SKIP_TRACK="${OPTARG}" ;;
        g ) SKIP_GEN="${OPTARG}" ;;
        d ) SKIP_IMM="${OPTARG}" ;;
        i ) SKIP_IMOP="${OPTARG}" ;;
        e ) SKIP_EMOP="${OPTARG}" ;;
        p ) SKIP_IMM_IMOP="${OPTARG}" ;;
        x ) SKIP_IMM_EMOP="${OPTARG}" ;;
        u ) SKIP_UNSAFE="${OPTARG}" ;;
        k ) SKIP_EVO_GEN="${OPTARG}" ;;
    esac
done
shift $((${OPTIND} - 1))

REPO=$1
OUTPUT_DIR=$2
FIRST_SHA=$3
PROJECT=$(echo ${REPO} | tr / -)
shift 2
source ${SCRIPT_DIR}/constants.sh
export RVMLOGGINGLEVEL=UNIQUE
export EXTENSION_PATH=${SCRIPT_DIR}/../extensions/tinymop-extension-1.0.jar
export IMM_AGENT_PATH=$(pwd)/gen-imm.jar
if [[ -z ${TMP_DIRECTORY} ]]; then
    export TMP_DIRECTORY="${TMP_DIR}"
fi
mkdir -p ${TMP_DIRECTORY}

if [[ -z ${MAVEN_OPTS} ]]; then
    export MAVEN_OPTS="-Xmx500g -XX:-UseGCOverheadLimit -Djava.io.tmpdir=${TMP_DIRECTORY}"
fi
RETRY_DELAY=60
if [[ -z ${M2_DIR} ]]; then
    export M2_DIR=${HOME}/.m2
fi

function setup() {
    if [[ -z ${FIRST_SHA} ]]; then
        echo "Usage: bash run_imm.sh <project> <output-dir> <sha1, sha2, ...>"
        exit 1
    fi

    if [[ ! -f ${SCRIPT_DIR}/../agents/gen-normal.jar ]]; then
        echo "Building [normal] gen.jar agent"
        pushd ${SCRIPT_DIR}/.. &> /dev/null
        bash make-jars.sh
        mv ${SCRIPT_DIR}/../agents/gen.jar ${SCRIPT_DIR}/../agents/gen-normal.jar
        popd &> /dev/null
    fi

    cp ${SCRIPT_DIR}/../agents/gen-normal.jar ${SCRIPT_DIR}/../agents/emop-gen-ps1c.jar
    cp ${SCRIPT_DIR}/../agents/gen-normal.jar ${SCRIPT_DIR}/../agents/emop-gen-ps3cl.jar
    cp ${SCRIPT_DIR}/../agents/three.jar ${SCRIPT_DIR}/../agents/emop-mop-ps1c.jar
    cp ${SCRIPT_DIR}/../agents/three.jar ${SCRIPT_DIR}/../agents/emop-mop-ps3cl.jar

    if [[ ! -f ${SCRIPT_DIR}/../agents/gen-imm.jar ]]; then
        echo "Building [imm] gen.jar agent"
        pushd ${SCRIPT_DIR}/.. &> /dev/null
        bash make-jars.sh true
        mv ${SCRIPT_DIR}/../agents/gen.jar ${SCRIPT_DIR}/../agents/gen-imm.jar
        popd &> /dev/null
    fi

    cp ${SCRIPT_DIR}/../agents/gen-imm.jar ${SCRIPT_DIR}/../agents/emop-gen-imop-ps1c.jar
    cp ${SCRIPT_DIR}/../agents/gen-imm.jar ${SCRIPT_DIR}/../agents/emop-gen-imop-ps3cl.jar

    if [[ ! ${OUTPUT_DIR} =~ ^/.* ]]; then
        OUTPUT_DIR=${SCRIPT_DIR}/${OUTPUT_DIR}
    fi
}

function setup_project() {
    mkdir -p ${OUTPUT_DIR}
    cd ${OUTPUT_DIR}

    git clone https://github.com/${REPO} project &> ${OUTPUT_DIR}/clone.log
    status=$?
    echo "Clone status: ${status}"
    if [[ ${status} -ne 0 ]]; then
        exit 1
    fi

    cd project
}

function download_dependencies() {
    local sha=$1
    local attempt=${2:-1}
    echo "Download dependencies: (attempt: ${attempt})"
    filename="download.log"
    if [[ ${attempt} -ne 1 ]]; then
        filename="download.${attempt}.log"
    fi
    export ADD_AGENT=0

    (time timeout ${TIMEOUT} mvn clean test-compile surefire:test ${SKIP} -Djava.io.tmpdir=${TMP_DIRECTORY} -Dmaven.ext.class.path=${EXTENSION_PATH}) &> ${OUTPUT_DIR}/${sha}/${filename}
    status=$?

    process_time=$(echo "$(tail ${OUTPUT_DIR}/${sha}/${filename} | grep "real" | cut -d $'\t' -f 2 | cut -d 's' -f 1 | cut -d 'm' -f 1)*60+$(tail ${OUTPUT_DIR}/${sha}/${filename} | grep "real" | cut -d $'\t' -f 2 | cut -d 's' -f 1 | cut -d 'm' -f 2)" | bc -l)
    echo "Download time: ${process_time} sec and Download status: ${status}"

    if [[ ${status} -ne 0 ]]; then
        if [[ ${attempt} -ge 3 ]]; then
            echo "Unable to run download on ${sha} 3 times, exiting..."
            echo ",${status},${process_time}" >> ${OUTPUT_DIR}/report.csv
            echo "" >> ${OUTPUT_DIR}/report.csv
            exit 1
        else
            echo "Unable to run download on ${sha}, try again after 60 seconds"
            sleep ${RETRY_DELAY}
            download_dependencies ${sha} $((attempt + 1))
            return $?
        fi
    else
        echo -n ",${status},${process_time}" >> ${OUTPUT_DIR}/report.csv
    fi

    unset ADD_AGENT
    return 0
}

function run_test() {
    local sha=$1
    local attempt=${2:-1}
    echo "Test: (attempt: ${attempt})"
    filename="test.log"
    if [[ ${attempt} -ne 1 ]]; then
        filename="test.${attempt}.log"
    fi
    export ADD_AGENT=0

    CMD_PID=0
    bash ${SCRIPT_DIR}/../scripts/monitor_memory.sh ${OUTPUT_DIR}/${sha}/memory-${filename} &> ${OUTPUT_DIR}/${sha}/.monitor_memory-${filename} &
    CMD_PID=$!
    disown

    (time timeout ${TIMEOUT} mvn surefire:test ${SKIP} -Djava.io.tmpdir=${TMP_DIRECTORY} -Dmaven.ext.class.path=${EXTENSION_PATH}) &> ${OUTPUT_DIR}/${sha}/${filename}
    status=$?

    process_time=$(echo "$(tail ${OUTPUT_DIR}/${sha}/${filename} | grep "real" | cut -d $'\t' -f 2 | cut -d 's' -f 1 | cut -d 'm' -f 1)*60+$(tail ${OUTPUT_DIR}/${sha}/${filename} | grep "real" | cut -d $'\t' -f 2 | cut -d 's' -f 1 | cut -d 'm' -f 2)" | bc -l)
    echo "Test Time: ${process_time} sec and Test status: ${status}"

    if [[ ${CMD_PID} -ne 0 ]]; then
        kill -9 ${CMD_PID} &> /dev/null
        CMD_PID=0
    fi
    if [[ ${status} -ne 0 ]]; then
        if [[ ${attempt} -ge 3 ]]; then
            echo "Unable to run test on ${sha} 3 times, exiting..."
            echo ",${status},${process_time}" >> ${OUTPUT_DIR}/report.csv
            exit 1
        else
            echo "Unable to run test on ${sha}, try again after 60 seconds"
            sleep ${RETRY_DELAY}
            git clean -f
            run_test ${sha} $((attempt + 1))
            return $?
        fi
    else
        echo -n ",${status},${process_time}" >> ${OUTPUT_DIR}/report.csv
    fi

    unset ADD_AGENT
    return 0
}

function run_mop() {
    local sha=$1
    local attempt=${2:-1}
    echo "MOP: (attempt: ${attempt})"
    filename="mop.log"
    if [[ ${attempt} -ne 1 ]]; then
        filename="mop.${attempt}.log"
    fi
    export MOP_AGENT_PATH="-javaagent:${SCRIPT_DIR}/../agents/three.jar"

    CMD_PID=0
    bash ${SCRIPT_DIR}/../scripts/monitor_memory.sh ${OUTPUT_DIR}/${sha}/memory-${filename} &> ${OUTPUT_DIR}/${sha}/.monitor_memory-${filename} &
    CMD_PID=$!
    disown

    (time timeout ${TIMEOUT} mvn surefire:test ${SKIP} -Dsurefire.exitTimeout=172800 -Djava.io.tmpdir=${TMP_DIRECTORY} -Dmaven.ext.class.path=${EXTENSION_PATH}) &> ${OUTPUT_DIR}/${sha}/${filename}
    status=$?

    process_time=$(echo "$(tail ${OUTPUT_DIR}/${sha}/${filename} | grep "real" | cut -d $'\t' -f 2 | cut -d 's' -f 1 | cut -d 'm' -f 1)*60+$(tail ${OUTPUT_DIR}/${sha}/${filename} | grep "real" | cut -d $'\t' -f 2 | cut -d 's' -f 1 | cut -d 'm' -f 2)" | bc -l)
    echo "MOP Time: ${process_time} sec and MOP status: ${status}"

    if [[ ${CMD_PID} -ne 0 ]]; then
        kill -9 ${CMD_PID} &> /dev/null
    fi
    if [[ ${status} -ne 0 ]]; then
        if [[ ${attempt} -ge 3 ]]; then
            echo "Unable to run MOP on ${sha} 3 times, exiting..."
            echo ",${status},${process_time}" >> ${OUTPUT_DIR}/report.csv
            exit 1
        else
            echo "Unable to run MOP on ${sha}, try again after 60 seconds"
            sleep ${RETRY_DELAY}
            rm -f violation-counts
            git clean -f
            run_mop ${sha} $((attempt + 1))
            return $?
        fi
    else
        echo -n ",${status},${process_time}" >> ${OUTPUT_DIR}/report.csv
    fi

    if [[ -f violation-counts ]]; then
        mv violation-counts ${OUTPUT_DIR}/${sha}/violations/violation-counts-mop
    fi

    unset MOP_AGENT_PATH
    return 0
}

function run_track() {
    local sha=$1
    local attempt=${2:-1}
    echo "Track: (attempt: ${attempt})"
    filename="track.log"
    if [[ ${attempt} -ne 1 ]]; then
        filename="track.${attempt}.log"
    fi
    export MOP_AGENT_PATH="-javaagent:${SCRIPT_DIR}/../agents/threeTrack.jar"
    export TRACEDB_PATH=${OUTPUT_DIR}/${sha}/tracemop-traces/all-traces
    mkdir -p ${TRACEDB_PATH}
    export COLLECT_MONITORS=1
    export TRACEDB_RANDOM=1
    
    CMD_PID=0
    bash ${SCRIPT_DIR}/../scripts/monitor_memory.sh ${OUTPUT_DIR}/${sha}/memory-${filename} &> ${OUTPUT_DIR}/${sha}/.monitor_memory-${filename} &
    CMD_PID=$!
    disown
    
    (time timeout ${TIMEOUT} mvn surefire:test ${SKIP} -Dsurefire.exitTimeout=172800 -Djava.io.tmpdir=${TMP_DIRECTORY} -Dmaven.ext.class.path=${EXTENSION_PATH}) &> ${OUTPUT_DIR}/${sha}/${filename}
    status=$?
    
    process_time=$(echo "$(tail ${OUTPUT_DIR}/${sha}/${filename} | grep "real" | cut -d $'\t' -f 2 | cut -d 's' -f 1 | cut -d 'm' -f 1)*60+$(tail ${OUTPUT_DIR}/${sha}/${filename} | grep "real" | cut -d $'\t' -f 2 | cut -d 's' -f 1 | cut -d 'm' -f 2)" | bc -l)
    echo "Track Time: ${process_time} sec and Track status: ${status}"
    
    if [[ ${CMD_PID} -ne 0 ]]; then
        kill -9 ${CMD_PID} &> /dev/null
    fi
    if [[ ${status} -ne 0 ]]; then
        if [[ ${status} -eq 124 ]]; then
            echo "Unable to run Track on ${sha} due to timeout, exiting..."
            echo -n ",${status},${process_time}" >> ${OUTPUT_DIR}/report.csv # don't exit
        elif [[ ${attempt} -ge 3 ]]; then
            echo "Unable to run Track on ${sha} 3 times, exiting..."
            echo -n ",${status},${process_time}" >> ${OUTPUT_DIR}/report.csv # don't exit
        else
            echo "Unable to run Track on ${sha}, try again after 60 seconds"
            sleep ${RETRY_DELAY}
            rm -rf ${TRACEDB_PATH}
            rm -f violation-counts
            git clean -f
            run_track ${sha} $((attempt + 1))
            return $?
        fi
    else
        echo -n ",${status},${process_time}" >> ${OUTPUT_DIR}/report.csv
    fi
    
    if [[ -f violation-counts ]]; then
        mv violation-counts ${OUTPUT_DIR}/${sha}/violations/violation-counts-track
    fi
    
    unset MOP_AGENT_PATH
    unset TRACEDB_PATH
    unset COLLECT_MONITORS
    unset TRACEDB_RANDOM
    return 0
}

function run_gen() {
    local sha=$1
    local attempt=${2:-1}
    echo "Gen: (attempt: ${attempt})"
    filename="gen.log"
    if [[ ${attempt} -ne 1 ]]; then
        filename="gen.${attempt}.log"
    fi
    export MOP_AGENT_PATH="-javaagent:${SCRIPT_DIR}/../agents/gen-normal.jar"
    export TINYMOP_TRACEDB_PATH=${OUTPUT_DIR}/${sha}/tinymop-traces
    mkdir -p ${TINYMOP_TRACEDB_PATH}
    export COLLECT_TRACES=1

    bash ${SCRIPT_DIR}/../scripts/monitor_memory.sh ${OUTPUT_DIR}/${sha}/memory-${filename} &> ${OUTPUT_DIR}/${sha}/.monitor_memory-${filename} &
    CMD_PID=$!
    disown

    (time timeout ${TIMEOUT} mvn surefire:test ${SKIP} -Dsurefire.exitTimeout=172800 -Djava.io.tmpdir=${TMP_DIRECTORY} -Dmaven.ext.class.path=${EXTENSION_PATH}) &> ${OUTPUT_DIR}/${sha}/${filename}
    status=$?

    process_time=$(echo "$(tail ${OUTPUT_DIR}/${sha}/${filename} | grep "real" | cut -d $'\t' -f 2 | cut -d 's' -f 1 | cut -d 'm' -f 1)*60+$(tail ${OUTPUT_DIR}/${sha}/${filename} | grep "real" | cut -d $'\t' -f 2 | cut -d 's' -f 1 | cut -d 'm' -f 2)" | bc -l)
    echo "GEN Time: ${process_time} sec and GEN status: ${status}"

    if [[ ${CMD_PID} -ne 0 ]]; then
        kill -9 ${CMD_PID} &> /dev/null
        CMD_PID=0
    fi
    if [[ ${status} -ne 0 ]]; then
        if [[ ${attempt} -ge 3 ]]; then
            echo "Unable to run GEN on ${sha} 3 times, exiting..."
            echo ",${status},${process_time}" >> ${OUTPUT_DIR}/report.csv
            exit 1
        else
            echo "Unable to run GEN on ${sha}, try again after 60 seconds"
            sleep ${RETRY_DELAY}
            rm -rf ${TINYMOP_TRACEDB_PATH}
            git clean -f
            run_gen ${sha} $((attempt + 1))
            return $?
        fi
    else
        echo -n ",${status},${process_time}" >> ${OUTPUT_DIR}/report.csv
    fi

    unset MOP_AGENT_PATH
    unset TINYMOP_TRACEDB_PATH
    unset COLLECT_TRACES
    return 0
}

function run_imm() {
    local sha=$1
    local attempt=${2:-1}
    echo "IMM: (attempt: ${attempt})"
    filename="imm.log"
    if [[ ${attempt} -ne 1 ]]; then
        filename="imm.${attempt}.log"
    fi
    rm -rf ${M2_DIR}-backup && cp -r ${M2_DIR} ${M2_DIR}-backup # backup .m2
    cd ${OUTPUT_DIR}
    rm -rf project-backup && cp -a project project-backup # backup project
    cd project
    cp ${SCRIPT_DIR}/../agents/gen-imm.jar ${IMM_AGENT_PATH}
    if [[ -d .tinymop-imm ]]; then
        mv .tinymop-imm .tinymop
    fi
    export MOP_AGENT_PATH="-javaagent:${IMM_AGENT_PATH}"
    export TINYMOP_CHANGED_CLASSES=$(pwd)/changedClasses.txt
    export TINYMOP_TRACEDB_PATH=${OUTPUT_DIR}/${sha}/imm-traces
    mkdir -p ${TINYMOP_TRACEDB_PATH}
    export COLLECT_TRACES=1

    CMD_PID=0
    bash ${SCRIPT_DIR}/../scripts/monitor_memory.sh ${OUTPUT_DIR}/${sha}/memory-${filename} &> ${OUTPUT_DIR}/${sha}/.monitor_memory-${filename} &
    CMD_PID=$!
    disown

    mvn clean test-compile -Djava.io.tmpdir=${TMP_DIRECTORY} &> /dev/null
    (time timeout ${TIMEOUT} mvn edu.lazymop.tinymop:imm-plugin:1.0-SNAPSHOT:incremental-run ${SKIP} -Dsurefire.exitTimeout=172800 -Djava.io.tmpdir=${TMP_DIRECTORY} -Dmaven.ext.class.path=${EXTENSION_PATH} -DtracesDir=${TINYMOP_TRACEDB_PATH} -Dstats=true -DagentPath=${IMM_AGENT_PATH}) &> ${OUTPUT_DIR}/${sha}/${filename}
    status=$?

    process_time=$(echo "$(tail ${OUTPUT_DIR}/${sha}/${filename} | grep "real" | cut -d $'\t' -f 2 | cut -d 's' -f 1 | cut -d 'm' -f 1)*60+$(tail ${OUTPUT_DIR}/${sha}/${filename} | grep "real" | cut -d $'\t' -f 2 | cut -d 's' -f 1 | cut -d 'm' -f 2)" | bc -l)
    echo "IMM Time: ${process_time} sec and IMM status: ${status}"

    if [[ ${CMD_PID} -ne 0 ]]; then
        kill -9 ${CMD_PID} &> /dev/null
        CMD_PID=0
    fi
    if [[ -f ${TINYMOP_CHANGED_CLASSES} ]]; then
        mv ${TINYMOP_CHANGED_CLASSES} ${OUTPUT_DIR}/${sha}/changedClasses.txt
    fi
    rm -f ${IMM_AGENT_PATH}
    if [[ ${status} -ne 0 ]]; then
        if [[ ${attempt} -ge 3 ]]; then
            echo "Unable to run IMM on ${sha} 3 times, exiting..."
            echo ",${status},${process_time}" >> ${OUTPUT_DIR}/report.csv
            exit 1
        else
            echo "Unable to run IMM on ${sha}, try again after 60 seconds"
            sleep ${RETRY_DELAY}
            rm -rf ${TINYMOP_TRACEDB_PATH}
            rm -rf ${M2_DIR} && cp -r ${M2_DIR}-backup ${M2_DIR} # restore .m2
            cd ${OUTPUT_DIR}
            rm -rf project && mv project-backup project # restore project
            cd project
            git clean -f
            run_imm ${sha} $((attempt + 1))
            return $?
        fi
    else
        echo -n ",${status},${process_time}" >> ${OUTPUT_DIR}/report.csv
    fi

    rm -rf ${M2_DIR} && cp -r ${M2_DIR}-backup ${M2_DIR} # restore .m2
    rm -rf project-backup # remove backup
    cp -r .tinymop ${OUTPUT_DIR}/${sha}/imm-artifact
    mv .tinymop .tinymop-imm
    unset MOP_AGENT_PATH
    unset TINYMOP_CHANGED_CLASSES
    unset TINYMOP_TRACEDB_PATH
    unset COLLECT_TRACES
    return 0
}

function run_imop() {
    local sha=$1
    local variant=$2
    local attempt=${3:-1}
    echo "iMOP - ${variant}: (attempt: ${attempt})"
    filename="imop-${variant}.log"
    if [[ ${attempt} -ne 1 ]]; then
        filename="imop-${variant}.${attempt}.log"
    fi
    if [[ -d ${TMP_DIRECTORY}/aspectj-${PROJECT}-cache-${variant} ]]; then
        rm -rf ${TMP_DIRECTORY}/aspectj-${PROJECT}-cache-${variant}-backup && cp -r ${TMP_DIRECTORY}/aspectj-${PROJECT}-cache-${variant} ${TMP_DIRECTORY}/aspectj-${PROJECT}-cache-${variant}-backup # backup cache
    fi

    if [[ ${variant} == "gen" ]]; then
        export MOP_AGENT_PATH="-javaagent:${SCRIPT_DIR}/../agents/gen-normal.jar -Daj.weaving.cache.enabled=true -Daj.weaving.cache.dir=${TMP_DIRECTORY}/aspectj-${PROJECT}-cache-${variant}/"
    else
        export MOP_AGENT_PATH="-javaagent:${SCRIPT_DIR}/../agents/three.jar -Daj.weaving.cache.enabled=true -Daj.weaving.cache.dir=${TMP_DIRECTORY}/aspectj-${PROJECT}-cache-${variant}/"
    fi
    export TINYMOP_TRACEDB_PATH=${OUTPUT_DIR}/${sha}/imop-traces
    mkdir -p ${TINYMOP_TRACEDB_PATH}
    export COLLECT_TRACES=1
    
    bash ${SCRIPT_DIR}/../scripts/monitor_memory.sh ${OUTPUT_DIR}/${sha}/memory-${filename} &> ${OUTPUT_DIR}/${sha}/.monitor_memory-${filename} &
    CMD_PID=$!
    disown
    
    (time timeout ${TIMEOUT} mvn surefire:test ${SKIP} -Dsurefire.exitTimeout=172800 -Djava.io.tmpdir=${TMP_DIRECTORY} -Dmaven.ext.class.path=${EXTENSION_PATH}) &> ${OUTPUT_DIR}/${sha}/${filename}
    status=$?
    
    process_time=$(echo "$(tail ${OUTPUT_DIR}/${sha}/${filename} | grep "real" | cut -d $'\t' -f 2 | cut -d 's' -f 1 | cut -d 'm' -f 1)*60+$(tail ${OUTPUT_DIR}/${sha}/${filename} | grep "real" | cut -d $'\t' -f 2 | cut -d 's' -f 1 | cut -d 'm' -f 2)" | bc -l)
    echo "iMOP - ${variant} Time: ${process_time} sec and iMOP - ${variant} status: ${status}"
    
    if [[ ${CMD_PID} -ne 0 ]]; then
        kill -9 ${CMD_PID} &> /dev/null
        CMD_PID=0
    fi
    if [[ ${status} -ne 0 ]]; then
        if [[ ${attempt} -ge 3 ]]; then
            echo "Unable to run iMOP - ${variant} on ${sha} 3 times, exiting..."
            echo ",${status},${process_time}" >> ${OUTPUT_DIR}/report.csv
            exit 1
        else
            echo "Unable to run iMOP - ${variant} on ${sha}, try again after 60 seconds"
            sleep ${RETRY_DELAY}
            rm -rf ${TMP_DIRECTORY}/aspectj-${PROJECT}-cache-${variant}
            if [[ -d ${TMP_DIRECTORY}/aspectj-${PROJECT}-cache-${variant}-backup ]]; then
                mv ${TMP_DIRECTORY}/aspectj-${PROJECT}-cache-${variant}-backup ${TMP_DIRECTORY}/aspectj-${PROJECT}-cache-${variant}
            fi
            rm -rf ${TINYMOP_TRACEDB_PATH}
            rm -f violation-counts
            git clean -f
            run_imop ${sha} ${variant} $((attempt + 1))
            return $?
        fi
    else
        echo -n ",${status},${process_time}" >> ${OUTPUT_DIR}/report.csv
    fi

    if [[ -f violation-counts ]]; then
        mv violation-counts ${OUTPUT_DIR}/${sha}/violations/violation-imop-${variant}
    fi

    unset MOP_AGENT_PATH
    unset TINYMOP_TRACEDB_PATH
    unset COLLECT_TRACES
    return 0
}

function run_emop() {
    local sha=$1
    local variant=$2
    local attempt=${3:-1}
    echo "eMOP - ${variant}: (attempt: ${attempt})"
    filename="emop-${variant}.log"
    if [[ ${attempt} -ne 1 ]]; then
        filename="emop-${variant}.${attempt}.log"
    fi
    cd ${OUTPUT_DIR}
    rm -rf project-backup && cp -a project project-backup # backup project
    cd project
    if [[ -d .starts-emop-${variant} ]]; then
        mv .starts-emop-${variant} .starts
    fi
    export MOP_AGENT_PATH="-javaagent:${SCRIPT_DIR}/../agents/emop-${variant}.jar"
    export TINYMOP_TRACEDB_PATH=${OUTPUT_DIR}/${sha}/emop-${variant}-traces
    mkdir -p ${TINYMOP_TRACEDB_PATH}
    export COLLECT_TRACES=1
    
    CMD_PID=0
    bash ${SCRIPT_DIR}/../scripts/monitor_memory.sh ${OUTPUT_DIR}/${sha}/memory-${filename} &> ${OUTPUT_DIR}/${sha}/.monitor_memory-${filename} &
    CMD_PID=$!
    disown
    
    if [[ ${variant} == "gen-ps1c" || ${variant} == "mop-ps1c" ]]; then
        (time timeout ${TIMEOUT} mvn edu.cornell:emop-maven-plugin:1.0-SNAPSHOT:rps -DjavamopAgent=${SCRIPT_DIR}/../agents/emop-${variant}.jar -DincludeNonAffected=false -DclosureOption=PS1 ${SKIP} -Dsurefire.exitTimeout=172800 -Djava.io.tmpdir=${TMP_DIRECTORY} -Dmaven.ext.class.path=${EXTENSION_PATH}) &> ${OUTPUT_DIR}/${sha}/${filename}
        status=$?
    elif [[ ${variant} == "gen-ps3cl" || ${variant} == "mop-ps3cl" ]]; then
        (time timeout ${TIMEOUT} mvn edu.cornell:emop-maven-plugin:1.0-SNAPSHOT:rps -DjavamopAgent=${SCRIPT_DIR}/../agents/emop-${variant}.jar -DincludeNonAffected=false -DincludeLibraries=false -DclosureOption=PS3 ${SKIP} -Dsurefire.exitTimeout=172800 -Djava.io.tmpdir=${TMP_DIRECTORY} -Dmaven.ext.class.path=${EXTENSION_PATH}) &> ${OUTPUT_DIR}/${sha}/${filename}
        status=$?
    fi
    
    process_time=$(echo "$(tail ${OUTPUT_DIR}/${sha}/${filename} | grep "real" | cut -d $'\t' -f 2 | cut -d 's' -f 1 | cut -d 'm' -f 1)*60+$(tail ${OUTPUT_DIR}/${sha}/${filename} | grep "real" | cut -d $'\t' -f 2 | cut -d 's' -f 1 | cut -d 'm' -f 2)" | bc -l)
    echo "eMOP - ${variant} Time: ${process_time} sec and eMOP - ${variant} status: ${status}"
    
    if [[ ${CMD_PID} -ne 0 ]]; then
        kill -9 ${CMD_PID} &> /dev/null
        CMD_PID=0
    fi
    if [[ ${status} -ne 0 ]]; then
        if [[ ${attempt} -ge 3 ]]; then
            echo "Unable to run eMOP - ${variant} on ${sha} 3 times, exiting..."
            echo ",${status},${process_time}" >> ${OUTPUT_DIR}/report.csv
            exit 1
        else
            echo "Unable to run eMOP - ${variant} on ${sha}, try again after 60 seconds"
            sleep ${RETRY_DELAY}
            rm -rf ${TINYMOP_TRACEDB_PATH}
            cd ${OUTPUT_DIR}
            rm -rf project && mv project-backup project # restore project
            cd project
            rm -f violation-counts
            git clean -f
            run_emop ${sha} ${variant} $((attempt + 1))
            return $?
        fi
    else
        echo -n ",${status},${process_time}" >> ${OUTPUT_DIR}/report.csv
    fi
    
    if [[ -f violation-counts ]]; then
        mv violation-counts ${OUTPUT_DIR}/${sha}/violations/violation-emop-${variant}
    fi

    rm -rf project-backup # remove backup
    cp -r .starts ${OUTPUT_DIR}/${sha}/emop-${variant}-artifact
    mv .starts .starts-emop-${variant}
    unset MOP_AGENT_PATH
    unset TINYMOP_TRACEDB_PATH
    unset COLLECT_TRACES
    return 0
}

function run_imm_imop() {
    local sha=$1
    local attempt=${2:-1}
    echo "IMM+iMOP: (attempt: ${attempt})"
    filename="imm-imop.log"
    if [[ ${attempt} -ne 1 ]]; then
        filename="imm-imop.${attempt}.log"
    fi
    if [[ -d ${TMP_DIRECTORY}/aspectj-${PROJECT}-cache-imm ]]; then
        rm -rf ${TMP_DIRECTORY}/aspectj-${PROJECT}-cache-imm-backup && cp -r ${TMP_DIRECTORY}/aspectj-${PROJECT}-cache-imm ${TMP_DIRECTORY}/aspectj-${PROJECT}-cache-imm-backup # backup cache
    fi
    rm -rf ${M2_DIR}-backup && cp -r ${M2_DIR} ${M2_DIR}-backup # backup .m2
    cd ${OUTPUT_DIR}
    rm -rf project-backup && cp -a project project-backup # backup project
    cd project
    cp ${SCRIPT_DIR}/../agents/gen-imm.jar ${IMM_AGENT_PATH}
    if [[ -d .tinymop-imm-imop ]]; then
        mv .tinymop-imm-imop .tinymop
    fi
    export MOP_AGENT_PATH="-javaagent:${IMM_AGENT_PATH} -Daj.weaving.cache.enabled=true -Daj.weaving.cache.dir=${TMP_DIRECTORY}/aspectj-${PROJECT}-cache-imm/"
    export TINYMOP_CHANGED_CLASSES=$(pwd)/changedClasses-imop.txt
    export TINYMOP_TRACEDB_PATH=${OUTPUT_DIR}/${sha}/imm-imop-traces
    mkdir -p ${TINYMOP_TRACEDB_PATH}
    export COLLECT_TRACES=1
    
    CMD_PID=0
    bash ${SCRIPT_DIR}/../scripts/monitor_memory.sh ${OUTPUT_DIR}/${sha}/memory-${filename} &> ${OUTPUT_DIR}/${sha}/.monitor_memory-${filename} &
    CMD_PID=$!
    disown
    
    mvn clean test-compile -Djava.io.tmpdir=${TMP_DIRECTORY} &> /dev/null
    (time timeout ${TIMEOUT} mvn edu.lazymop.tinymop:imm-plugin:1.0-SNAPSHOT:incremental-run ${SKIP} -DdisableInitRemover=true -Dsurefire.exitTimeout=172800 -Djava.io.tmpdir=${TMP_DIRECTORY} -Dmaven.ext.class.path=${EXTENSION_PATH} -DtracesDir=${TINYMOP_TRACEDB_PATH} -Dstats=true -DagentPath=${IMM_AGENT_PATH}) &> ${OUTPUT_DIR}/${sha}/${filename}
    status=$?
    
    process_time=$(echo "$(tail ${OUTPUT_DIR}/${sha}/${filename} | grep "real" | cut -d $'\t' -f 2 | cut -d 's' -f 1 | cut -d 'm' -f 1)*60+$(tail ${OUTPUT_DIR}/${sha}/${filename} | grep "real" | cut -d $'\t' -f 2 | cut -d 's' -f 1 | cut -d 'm' -f 2)" | bc -l)
    echo "IMM+iMOP Time: ${process_time} sec and IMM+iMOP status: ${status}"
    
    if [[ ${CMD_PID} -ne 0 ]]; then
        kill -9 ${CMD_PID} &> /dev/null
        CMD_PID=0
    fi
    if [[ -f ${TINYMOP_CHANGED_CLASSES} ]]; then
        mv ${TINYMOP_CHANGED_CLASSES} ${OUTPUT_DIR}/${sha}/changedClasses-imop.txt
    fi
    rm -f ${IMM_AGENT_PATH}
    if [[ ${status} -ne 0 ]]; then
        if [[ ${attempt} -ge 3 ]]; then
            echo "Unable to run IMM+iMOP on ${sha} 3 times, exiting..."
            echo ",${status},${process_time}" >> ${OUTPUT_DIR}/report.csv
            exit 1
        else
            echo "Unable to run IMM+iMOP on ${sha}, try again after 60 seconds"
            sleep ${RETRY_DELAY}
            rm -rf ${TMP_DIRECTORY}/aspectj-${PROJECT}-cache-imm
            if [[ -d ${TMP_DIRECTORY}/aspectj-${PROJECT}-cache-imm-backup ]]; then
                mv ${TMP_DIRECTORY}/aspectj-${PROJECT}-cache-imm-backup ${TMP_DIRECTORY}/aspectj-${PROJECT}-cache-imm
            fi
            rm -rf ${TINYMOP_TRACEDB_PATH}
            rm -rf ${M2_DIR} && cp -r ${M2_DIR}-backup ${M2_DIR} # restore .m2
            cd ${OUTPUT_DIR}
            rm -rf project && mv project-backup project # restore project
            cd project
            git clean -f
            run_imm_imop ${sha} $((attempt + 1))
            return $?
        fi
    else
        echo -n ",${status},${process_time}" >> ${OUTPUT_DIR}/report.csv
    fi

    rm -rf ${M2_DIR} && cp -r ${M2_DIR}-backup ${M2_DIR} # restore .m2
    rm -rf project-backup # remove backup
    cp -r .tinymop ${OUTPUT_DIR}/${sha}/imm-imop-artifact
    mv .tinymop .tinymop-imm-imop
    unset MOP_AGENT_PATH
    unset TINYMOP_CHANGED_CLASSES
    unset TINYMOP_TRACEDB_PATH
    unset COLLECT_TRACES
    return 0
}

function run_imm_emop() {
    local sha=$1
    local variant=$2
    local attempt=${3:-1}
    echo "IMM+eMOP - ${variant}: (attempt: ${attempt})"
    filename="imm-emop-${variant}.log"
    if [[ ${attempt} -ne 1 ]]; then
        filename="imm-emop-${variant}.${attempt}.log"
    fi
    rm -rf ${M2_DIR}-backup && cp -r ${M2_DIR} ${M2_DIR}-backup # backup .m2
    cd ${OUTPUT_DIR}
    rm -rf project-backup && cp -a project project-backup # backup project
    cd project
    cp ${SCRIPT_DIR}/../agents/emop-gen-imop-${variant}.jar ${IMM_AGENT_PATH}
    if [[ -d .tinymop-imm-emop-${variant} ]]; then
        mv .tinymop-imm-emop-${variant} .tinymop
    fi
    if [[ -d .starts-imm-emop-${variant} ]]; then
        mv .starts-imm-emop-${variant} .starts
    fi
    export MOP_AGENT_PATH="-javaagent:${IMM_AGENT_PATH}"
    export TINYMOP_CHANGED_CLASSES=$(pwd)/changedClasses-emop-${variant}.txt
    export TINYMOP_TRACEDB_PATH=${OUTPUT_DIR}/${sha}/imm-emop-${variant}-traces
    mkdir -p ${TINYMOP_TRACEDB_PATH}
    export COLLECT_TRACES=1
    
    CMD_PID=0
    bash ${SCRIPT_DIR}/../scripts/monitor_memory.sh ${OUTPUT_DIR}/${sha}/memory-${filename} &> ${OUTPUT_DIR}/${sha}/.monitor_memory-${filename} &
    CMD_PID=$!
    disown
    
    mvn clean test-compile -Djava.io.tmpdir=${TMP_DIRECTORY} &> /dev/null
    if [[ ${variant} == "ps1c" ]]; then
        (time timeout ${TIMEOUT} mvn edu.lazymop.tinymop:imm-plugin:1.0-SNAPSHOT:incremental-run ${SKIP} -DeMOP=true -DdisableInitRemover=true -DjavamopAgent=${IMM_AGENT_PATH} -DincludeNonAffected=false -DclosureOption=PS1 -Dsurefire.exitTimeout=172800 -Djava.io.tmpdir=${TMP_DIRECTORY} -Dmaven.ext.class.path=${EXTENSION_PATH} -DtracesDir=${TINYMOP_TRACEDB_PATH} -Dstats=true -DagentPath=${IMM_AGENT_PATH}) &> ${OUTPUT_DIR}/${sha}/${filename}
        status=$?
    else
        (time timeout ${TIMEOUT} mvn edu.lazymop.tinymop:imm-plugin:1.0-SNAPSHOT:incremental-run ${SKIP} -DeMOP=true -DdisableInitRemover=true -DjavamopAgent=${IMM_AGENT_PATH} -DincludeNonAffected=false -DincludeLibraries=false -DclosureOption=PS3 -Dsurefire.exitTimeout=172800 -Djava.io.tmpdir=${TMP_DIRECTORY} -Dmaven.ext.class.path=${EXTENSION_PATH} -DtracesDir=${TINYMOP_TRACEDB_PATH} -Dstats=true -DagentPath=${IMM_AGENT_PATH}) &> ${OUTPUT_DIR}/${sha}/${filename}
        status=$?
    fi
    
    if [[ -n $(grep --text "Cannot find traces" ${OUTPUT_DIR}/${sha}/${filename}) ]]; then
        # this is not an error, change status back to 0
        status=0
    fi
    
    process_time=$(echo "$(tail ${OUTPUT_DIR}/${sha}/${filename} | grep "real" | cut -d $'\t' -f 2 | cut -d 's' -f 1 | cut -d 'm' -f 1)*60+$(tail ${OUTPUT_DIR}/${sha}/${filename} | grep "real" | cut -d $'\t' -f 2 | cut -d 's' -f 1 | cut -d 'm' -f 2)" | bc -l)
    echo "IMM+eMOP - ${variant} Time: ${process_time} sec and IMM+eMOP - ${variant} status: ${status}"
    
    if [[ ${CMD_PID} -ne 0 ]]; then
        kill -9 ${CMD_PID} &> /dev/null
        CMD_PID=0
    fi
    if [[ -f ${TINYMOP_CHANGED_CLASSES} ]]; then
        mv ${TINYMOP_CHANGED_CLASSES} ${OUTPUT_DIR}/${sha}/changedClasses-emop-${variant}.txt
    fi
    rm -f ${IMM_AGENT_PATH}
    if [[ ${status} -ne 0 ]]; then
        if [[ ${attempt} -ge 3 ]]; then
            echo "Unable to run IMM+eMOP - ${variant} on ${sha} 3 times, exiting..."
            echo ",${status},${process_time}" >> ${OUTPUT_DIR}/report.csv
            exit 1
        else
            echo "Unable to run IMM+eMOP - ${variant} on ${sha}, try again after 60 seconds"
            sleep ${RETRY_DELAY}
            rm -rf ${TINYMOP_TRACEDB_PATH}
            rm -rf ${M2_DIR} && cp -r ${M2_DIR}-backup ${M2_DIR} # restore .m2
            cd ${OUTPUT_DIR}
            rm -rf project && mv project-backup project # restore project
            cd project
            git clean -f
            run_imm_emop ${sha} ${variant} $((attempt + 1))
            return $?
        fi
    else
        echo -n ",${status},${process_time}" >> ${OUTPUT_DIR}/report.csv
    fi
    
    rm -rf ${M2_DIR} && cp -r ${M2_DIR}-backup ${M2_DIR} # restore .m2
    rm -rf project-backup # remove backup
    cp -r .tinymop ${OUTPUT_DIR}/${sha}/imm-emop-${variant}-artifact
    cp -r .starts ${OUTPUT_DIR}/${sha}/imm-emop-${variant}-artifact.2
    mv .tinymop .tinymop-imm-emop-${variant}
    mv .starts .starts-imm-emop-${variant}
    unset MOP_AGENT_PATH
    unset TINYMOP_CHANGED_CLASSES
    unset TINYMOP_TRACEDB_PATH
    unset COLLECT_TRACES
    return 0
}

function run_project() {
    # Print header
    echo -n "sha,download status,download time,test status,test time" > ${OUTPUT_DIR}/report.csv
    if [[ ${SKIP_MOP} != "true" ]]; then
        echo -n ",mop status,mop time" >> ${OUTPUT_DIR}/report.csv
    fi
    if [[ ${SKIP_TRACK} != "true" ]]; then
        echo -n ",track status,track time" >> ${OUTPUT_DIR}/report.csv
    fi
    if [[ ${SKIP_GEN} != "true" ]]; then
        echo -n ",gen status,gen time" >> ${OUTPUT_DIR}/report.csv
    fi
    if [[ ${SKIP_IMM} != "true" ]]; then
        echo -n ",imm status,imm time" >> ${OUTPUT_DIR}/report.csv
    fi
    if [[ ${SKIP_IMOP} != "true" ]]; then
        if [[ ${SKIP_EVO_GEN} != "true" ]]; then
            echo -n ",imop-gen status,imop-gen time" >> ${OUTPUT_DIR}/report.csv
        fi
        echo -n ",imop-mop status,imop-mop time" >> ${OUTPUT_DIR}/report.csv
    fi
    if [[ ${SKIP_EMOP} != "true" ]]; then
        if [[ ${SKIP_EVO_GEN} != "true" ]]; then
            echo -n ",gen+ps1c status,gen+ps1c time" >> ${OUTPUT_DIR}/report.csv
        fi
        if [[ ${SKIP_UNSAFE} != "true" && ${SKIP_EVO_GEN} != "true" ]]; then
            echo -n ",gen+ps3cl status,gen+ps3cl time" >> ${OUTPUT_DIR}/report.csv
        fi
        echo -n ",mop+ps1c status,mop+ps1c time" >> ${OUTPUT_DIR}/report.csv
        if [[ ${SKIP_UNSAFE} != "true" ]]; then
            echo -n ",mop+ps3cl status,mop+ps3cl time" >> ${OUTPUT_DIR}/report.csv
        fi
    fi
    if [[ ${SKIP_IMM_IMOP} != "true" ]]; then
        echo -n ",imm+imop status,imm+imop time" >> ${OUTPUT_DIR}/report.csv
    fi
    if [[ ${SKIP_IMM_EMOP} != "true" ]]; then
        echo -n ",imm+ps1c status,imm+ps1c time" >> ${OUTPUT_DIR}/report.csv
        if [[ ${SKIP_UNSAFE} != "true" ]]; then
            echo -n ",imm+ps3cl status,imm+ps3cl time" >> ${OUTPUT_DIR}/report.csv
        fi
    fi
    echo "" >> ${OUTPUT_DIR}/report.csv

    for sha in "$@"; do
        git checkout ${sha} &> /dev/null
        if [[ $? -ne 0 ]]; then
            echo "Unable to find SHA ${sha}"
            exit 1
        else
            echo ">>> ${sha}"
        fi

        echo -n "${sha}" >> ${OUTPUT_DIR}/report.csv
        mkdir -p ${OUTPUT_DIR}/${sha}/violations

        download_dependencies ${sha}
        run_test ${sha}
        
        if [[ ${SKIP_MOP} != "true" ]]; then
            run_mop ${sha}
        fi
        if [[ ${SKIP_TRACK} != "true" ]]; then
            run_track ${sha}
        fi
        if [[ ${SKIP_GEN} != "true" ]]; then
            run_gen ${sha}
        fi
        if [[ ${SKIP_IMM} != "true" ]]; then
            run_imm ${sha}
        fi
        if [[ ${SKIP_IMOP} != "true" ]]; then
            if [[ ${SKIP_EVO_GEN} != "true" ]]; then
                run_imop ${sha} gen
            fi
            run_imop ${sha} mop
        fi
        if [[ ${SKIP_EMOP} != "true" ]]; then
            pushd ${SCRIPT_DIR} &> /dev/null
            bash ${SCRIPT_DIR}/setup_emop.sh ${SCRIPT_DIR}/../props tinymop &> ${TMP_DIRECTORY}/install-emop-tinymop.log
            popd &> /dev/null

            if [[ ${SKIP_EVO_GEN} != "true" ]]; then
                run_emop ${sha} gen-ps1c
            fi
            if [[ ${SKIP_UNSAFE} != "true" && ${SKIP_EVO_GEN} != "true" ]]; then
                run_emop ${sha} gen-ps3cl
            fi

            pushd ${SCRIPT_DIR} &> /dev/null
            bash ${SCRIPT_DIR}/setup_emop.sh ${SCRIPT_DIR}/../props javamop &> ${TMP_DIRECTORY}/install-emop-javamop.log
            popd &> /dev/null
            
            run_emop ${sha} mop-ps1c
            if [[ ${SKIP_UNSAFE} != "true" ]]; then
                run_emop ${sha} mop-ps3cl
            fi
        fi
        if [[ ${SKIP_IMM_IMOP} != "true" ]]; then
            run_imm_imop ${sha}
        fi
        if [[ ${SKIP_IMM_EMOP} != "true" ]]; then
            pushd ${SCRIPT_DIR} &> /dev/null
            bash ${SCRIPT_DIR}/setup_emop.sh ${SCRIPT_DIR}/../props tinymop &> ${TMP_DIRECTORY}/install-emop-tinymop.log
            popd &> /dev/null

            run_imm_emop ${sha} ps1c
            if [[ ${SKIP_UNSAFE} != "true" ]]; then
                run_imm_emop ${sha} ps3cl
            fi
        fi

        git clean -f
        echo "" >> ${OUTPUT_DIR}/report.csv
    done
}

setup
setup_project
run_project $@
