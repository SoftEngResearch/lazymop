#!/bin/bash
SCRIPT_DIR=$( cd $( dirname $0 ) && pwd )
CURRENT_DIR=$(pwd)

export PROFILE=""
export ENABLE_ON_DEMAND_SYNC="true"
export ENABLE_INT_ENCODING="true"

while getopts :p:s:e: opts; do
    case "${opts}" in
        p ) PROFILE="${OPTARG}" ;;
        s ) ENABLE_ON_DEMAND_SYNC="${OPTARG}" ;;
        e ) ENABLE_INT_ENCODING="${OPTARG}" ;;
    esac
done
shift $((${OPTIND} - 1))

REPO=$1
SHA=$2
TINYMOP_ONLY=${3:-false}
RUN_IMM=${4:-false}
SKIP_TRACEMOP=${5:-false}
SKIP_JAVAMOP=${6:-false}
PROJECT=$(echo ${REPO} | tr / -)

if [[ -z ${SHA} ]]; then
    echo "Usage: bash run_project.sh <project> <sha> [tinymop-only: false]"
    exit 1
fi

if [[ ! -f ${SCRIPT_DIR}/../agents/gen-normal.jar ]]; then
    echo "Building [normal] gen.jar agent"
    pushd ${SCRIPT_DIR}/.. &> /dev/null
    bash make-jars.sh false ${ENABLE_ON_DEMAND_SYNC} ${ENABLE_INT_ENCODING}
    mv ${SCRIPT_DIR}/../agents/gen.jar ${SCRIPT_DIR}/../agents/gen-normal.jar
    popd &> /dev/null
fi

if [[ ! -f ${SCRIPT_DIR}/../agents/gen-imm.jar && ${RUN_IMM} == "true" ]]; then
    echo "Building [imm] gen.jar agent"
    pushd ${SCRIPT_DIR}/.. &> /dev/null
    bash make-jars.sh true ${ENABLE_ON_DEMAND_SYNC} ${ENABLE_INT_ENCODING}
    mv ${SCRIPT_DIR}/../agents/gen.jar ${SCRIPT_DIR}/../agents/gen-imm.jar
    popd &> /dev/null
fi

echo "Running ${PROJECT} with SHA ${SHA}, TINYMOP_ONLY: ${TINYMOP_ONLY}, RUN_IMM: ${RUN_IMM}, SKIP_TRACEMOP: ${SKIP_TRACEMOP}, PROFILE: ${PROFILE}, ENABLE_ON_DEMAND_SYNC: ${ENABLE_ON_DEMAND_SYNC}, and ENABLE_INT_ENCODING: ${ENABLE_INT_ENCODING}"

source ${SCRIPT_DIR}/constants.sh
rm -rf ${PROJECT}

function move_jfr() {
    local directory=$1
    local filename=$2
    for jfr in $(find -name "profile.jfr"); do
        local name=$(echo "${jfr}" | rev | cut -d '/' -f 2 | rev)
        if [[ ${name} != "." ]]; then
            # Is MMMP, add module name to file name
            mv ${jfr} ${directory}/${filename}_${name}
        else
            mv ${jfr} ${directory}/${filename}
        fi
    done
}


git clone https://github.com/${REPO} ${PROJECT} &> clone.log
status=$?
echo "Clone status: ${status}"
if [[ ${status} -ne 0 ]]; then
    exit 1
fi

pushd ${PROJECT} &> /dev/null
mkdir -p tinymop-logs/violations
mkdir -p tinymop-logs/tinymop-traces
mv ${CURRENT_DIR}/clone.log tinymop-logs/clone.log
git checkout ${SHA} &>> tinymop-logs/clone.log

echo "Download dependencies"
export ADD_AGENT=0
mvn test-compile surefire:test ${SKIP} -Dmaven.ext.class.path=${SCRIPT_DIR}/../extensions/tinymop-extension-1.0.jar &> tinymop-logs/download.log
status=$?
echo "Download status: ${status}"
if [[ ${status} -ne 0 ]]; then
    exit 1
fi
export MAVEN_OPTS="-Xmx500g -XX:-UseGCOverheadLimit"
unset ADD_AGENT
CMD_PID=0

if [[ ${TINYMOP_ONLY} != "true" && ${SKIP_JAVAMOP} != "true" ]]; then
    echo -n "Test: "
    filename="test.log"
    bash ${SCRIPT_DIR}/../scripts/monitor_memory.sh ${CURRENT_DIR}/${PROJECT}/tinymop-logs/memory-${filename} &> ${CURRENT_DIR}/${PROJECT}/tinymop-logs/.monitor_memory-${filename} &
    CMD_PID=$!
    disown
    
    export ADD_AGENT=0
    (time mvn surefire:test ${SKIP} -Dmaven.ext.class.path=${SCRIPT_DIR}/../extensions/tinymop-extension-1.0.jar) &> tinymop-logs/${filename}
    status=$?
    tail -n 10 tinymop-logs/${filename} | grep "BUILD"
    echo "Test Time: $(echo "$(tail tinymop-logs/${filename} | grep "real" | cut -d $'\t' -f 2 | cut -d 's' -f 1 | cut -d 'm' -f 1)*60+$(tail tinymop-logs/${filename} | grep "real" | cut -d $'\t' -f 2 | cut -d 's' -f 1 | cut -d 'm' -f 2)" | bc -l) sec and Test status: ${status}"
    if [[ ${status} -ne 0 ]]; then
        exit 1
    fi
    unset ADD_AGENT
    if [[ ${CMD_PID} -ne 0 ]]; then
        kill -9 ${CMD_PID} &> /dev/null
        CMD_PID=0
    fi
    
    echo -n "MOP: "
    export RVMLOGGINGLEVEL=UNIQUE

    if [[ -n ${PROFILE} ]]; then
        export MOP_AGENT_PATH="-javaagent:${SCRIPT_DIR}/../agents/three.jar -agentpath:${PROFILE}=start,alloc,interval=5ms,event=wall,file=profile.jfr"
    else
        export MOP_AGENT_PATH="-javaagent:${SCRIPT_DIR}/../agents/three.jar"
    fi

    filename="mop.log"
    bash ${SCRIPT_DIR}/../scripts/monitor_memory.sh ${CURRENT_DIR}/${PROJECT}/tinymop-logs/memory-${filename} &> ${CURRENT_DIR}/${PROJECT}/tinymop-logs/.monitor_memory-${filename} &
    CMD_PID=$!
    disown
    
    (time mvn surefire:test ${SKIP} -Dsurefire.exitTimeout=172800 -Dmaven.ext.class.path=${SCRIPT_DIR}/../extensions/tinymop-extension-1.0.jar) &> tinymop-logs/${filename}
    status=$?
    tail -n 10 tinymop-logs/${filename} | grep "BUILD"
    echo "MOP Time: $(echo "$(tail tinymop-logs/${filename} | grep "real" | cut -d $'\t' -f 2 | cut -d 's' -f 1 | cut -d 'm' -f 1)*60+$(tail tinymop-logs/${filename} | grep "real" | cut -d $'\t' -f 2 | cut -d 's' -f 1 | cut -d 'm' -f 2)" | bc -l) sec and MOP status: ${status}"
    if [[ ${status} -ne 0 ]]; then
        exit 1
    fi
    if [[ ${CMD_PID} -ne 0 ]]; then
        kill -9 ${CMD_PID} &> /dev/null
        CMD_PID=0
    fi
    if [[ -f violation-counts ]]; then
        mv violation-counts tinymop-logs/violations/violation-counts-mop
    fi
    if [[ -n ${PROFILE} ]]; then
        move_jfr ${CURRENT_DIR}/${PROJECT}/tinymop-logs mop.jfr
    fi
fi

echo -n "Gen: "
export TINYMOP_TRACEDB_PATH=$(pwd)/tinymop-logs/tinymop-traces
export COLLECT_TRACES=1

if [[ -n ${PROFILE} ]]; then
    export MOP_AGENT_PATH="-javaagent:${SCRIPT_DIR}/../agents/gen-normal.jar -agentpath:${PROFILE}=start,alloc,interval=5ms,event=wall,file=profile.jfr"
else
    export MOP_AGENT_PATH="-javaagent:${SCRIPT_DIR}/../agents/gen-normal.jar"
fi

filename="gen.log"
bash ${SCRIPT_DIR}/../scripts/monitor_memory.sh ${CURRENT_DIR}/${PROJECT}/tinymop-logs/memory-${filename} &> ${CURRENT_DIR}/${PROJECT}/tinymop-logs/.monitor_memory-${filename} &
CMD_PID=$!
disown

(time mvn surefire:test ${SKIP} -Dsurefire.exitTimeout=172800 -Dmaven.ext.class.path=${SCRIPT_DIR}/../extensions/tinymop-extension-1.0.jar) &> tinymop-logs/${filename}
status=$?
tail -n 10 tinymop-logs/${filename} | grep "BUILD"
echo "GEN Time: $(echo "$(tail tinymop-logs/${filename} | grep "real" | cut -d $'\t' -f 2 | cut -d 's' -f 1 | cut -d 'm' -f 1)*60+$(tail tinymop-logs/${filename} | grep "real" | cut -d $'\t' -f 2 | cut -d 's' -f 1 | cut -d 'm' -f 2)" | bc -l) sec and GEN status: ${status}"
#if [[ ${status} -ne 0 ]]; then
#   exit 1
#fi
if [[ ${CMD_PID} -ne 0 ]]; then
    kill -9 ${CMD_PID} &> /dev/null
    CMD_PID=0
fi
if [[ -n ${PROFILE} ]]; then
    move_jfr ${CURRENT_DIR}/${PROJECT}/tinymop-logs gen.jfr
fi

if [[ ${TINYMOP_ONLY} != "true" && ${SKIP_TRACEMOP} == "false" ]]; then
    echo -n "Track: "
    rm -rf ${CURRENT_DIR}/${PROJECT}/all-traces && mkdir -p ${CURRENT_DIR}/${PROJECT}/all-traces
    export TRACEDB_PATH=${CURRENT_DIR}/${PROJECT}/all-traces
    export COLLECT_MONITORS=1
    export TRACEDB_RANDOM=1

    if [[ -n ${PROFILE} ]]; then
        export MOP_AGENT_PATH="-javaagent:${SCRIPT_DIR}/../agents/threeTrack.jar -agentpath:${PROFILE}=start,alloc,interval=5ms,event=wall,file=profile.jfr"
    else
        export MOP_AGENT_PATH="-javaagent:${SCRIPT_DIR}/../agents/threeTrack.jar"
    fi

    filename="track.log"
    bash ${SCRIPT_DIR}/../scripts/monitor_memory.sh ${CURRENT_DIR}/${PROJECT}/tinymop-logs/memory-${filename} &> ${CURRENT_DIR}/${PROJECT}/tinymop-logs/.monitor_memory-${filename} &
    CMD_PID=$!
    disown
    
    (time mvn surefire:test ${SKIP} -Dsurefire.exitTimeout=172800 -Dmaven.ext.class.path=${SCRIPT_DIR}/../extensions/tinymop-extension-1.0.jar) &> tinymop-logs/${filename}
    status=$?
    tail -n 10 tinymop-logs/${filename} | grep "BUILD"
    echo "Track Time: $(echo "$(tail tinymop-logs/${filename} | grep "real" | cut -d $'\t' -f 2 | cut -d 's' -f 1 | cut -d 'm' -f 1)*60+$(tail tinymop-logs/${filename} | grep "real" | cut -d $'\t' -f 2 | cut -d 's' -f 1 | cut -d 'm' -f 2)" | bc -l) sec and Track status: ${status}"
    if [[ ${status} -ne 0 ]]; then
        exit 1
    fi
    if [[ ${CMD_PID} -ne 0 ]]; then
        kill -9 ${CMD_PID} &> /dev/null
        CMD_PID=0
    fi
    if [[ -f violation-counts ]]; then
        mv violation-counts tinymop-logs/violations/violation-counts-track
    fi
    if [[ -n ${PROFILE} ]]; then
        move_jfr ${CURRENT_DIR}/${PROJECT}/tinymop-logs track.jfr
    fi
    
    num_db=0
    for db in $(ls | grep "all-traces-"); do
        # search directory starts with all-traces-*
        if [[ ! -f ${db}/unique-traces.txt || ! -f ${db}/specs-frequency.csv || ! -f ${db}/locations.txt || ! -f ${db}/traces.txt ]]; then
            continue
        fi
        
        mv ${db}/unique-traces.txt ${db}/traces-id.txt
        (python3 ${SCRIPT_DIR}/../tracemop/scripts/count-traces-frequency.py ${db}/)
        rm ${db}/traces-id.txt ${db}/traces.txt
        mv ${db} tinymop-logs
    done
    rmdir all-traces
fi



if [[ ${RUN_IMM} == "true" ]]; then
    echo -n "IMM: "
    unset TRACEDB_PATH
    unset COLLECT_MONITORS
    unset TRACEDB_RANDOM
    rm -rf ${HOME}/.m2-backup && cp -r ${HOME}/.m2 ${HOME}/.m2-backup
    mkdir $(pwd)/tinymop-logs/tinymop-traces-imm
    export TINYMOP_TRACEDB_PATH=$(pwd)/tinymop-logs/tinymop-traces-imm

    if [[ -n ${PROFILE} ]]; then
        export MOP_AGENT_PATH="-javaagent:${CURRENT_DIR}/${PROJECT}/gen-imm.jar -agentpath:${PROFILE}=start,alloc,interval=5ms,event=wall,file=profile.jfr"
    else
        export MOP_AGENT_PATH="-javaagent:${CURRENT_DIR}/${PROJECT}/gen-imm.jar"
    fi

    filename="imm.log"
    bash ${SCRIPT_DIR}/../scripts/monitor_memory.sh ${CURRENT_DIR}/${PROJECT}/tinymop-logs/memory-${filename} &> ${CURRENT_DIR}/${PROJECT}/tinymop-logs/.monitor_memory-${filename} &
    CMD_PID=$!
    disown

    cp ${SCRIPT_DIR}/../agents/gen-imm.jar gen-imm.jar
    (time mvn edu.lazymop.tinymop:imm-plugin:1.0-SNAPSHOT:run ${SKIP} -Dsurefire.exitTimeout=172800 -Dmaven.ext.class.path=${SCRIPT_DIR}/../extensions/tinymop-extension-1.0.jar -DtracesDir="$(pwd)/tinymop-logs/tinymop-traces" -Dstats=true -DagentPath=gen-imm.jar) &> tinymop-logs/${filename}
    status=$?
    tail -n 10 tinymop-logs/${filename} | grep "BUILD"
    echo "IMM Time: $(echo "$(tail tinymop-logs/${filename} | grep "real" | cut -d $'\t' -f 2 | cut -d 's' -f 1 | cut -d 'm' -f 1)*60+$(tail tinymop-logs/${filename} | grep "real" | cut -d $'\t' -f 2 | cut -d 's' -f 1 | cut -d 'm' -f 2)" | bc -l) sec and IMM status: ${status}"
    rm -rf ${HOME}/.m2 && cp -r ${HOME}/.m2-backup ${HOME}/.m2
    if [[ ${status} -ne 0 ]]; then
        exit 1
    fi
    if [[ ${CMD_PID} -ne 0 ]]; then
        kill -9 ${CMD_PID} &> /dev/null
        CMD_PID=0
    fi
    if [[ -n ${PROFILE} ]]; then
        move_jfr ${CURRENT_DIR}/${PROJECT}/tinymop-logs imm.jfr
    fi
fi
