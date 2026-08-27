#!/bin/bash

PROJECT=$1 # in the form of repo/project
COMMIT=$2
OUTPUT=$3 # A csv with result of 4 configurations

ORG=$(echo ${PROJECT} | cut -d '/' -f 1)
REPO=$(echo ${PROJECT} | cut -d '/' -f 2)
CONFIGS="tracemop valgt lazymop valgl"

DOCKER_HOME="/home/tinymop"
DOCKER_PATH="PATH=/home/tinymop/apache-maven/bin:/usr/lib/jvm/java-8-openjdk/bin:/home/tinymop/aspectj-1.9.7/bin:/home/tinymop/aspectj-1.9.7/lib/aspectjweaver.jar:/home/tinymop/aspectj-1.9.7/lib/aspectjrt.jar:/home/tinymop/aspectj-1.9.7/lib/aspectjtools.jar:/home/tinymop/aspectj-1.9.7/lib/aspectjweaver.jar:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin"
DOCKER_CLASSPATH="CLASSPATH=/usr/lib/jvm/java-8-openjdk/jre/lib/resources.jar:/usr/lib/jvm/java-8-openjdk/jre/lib/rt.jar:/usr/lib/jvm/java-8-openjdk/jre/lib/sunrsasign.jar:/usr/lib/jvm/java-8-openjdk/jre/lib/jsse.jar:/usr/lib/jvm/java-8-openjdk/jre/lib/jce.jar:/usr/lib/jvm/java-8-openjdk/jre/lib/charsets.jar:/usr/lib/jvm/java-8-openjdk/jre/lib/jfr.jar:/usr/lib/jvm/java-8-openjdk/jre/classes:/usr/lib/jvm/java-8-openjdk/jre/lib/ext/nashorn.jar:/usr/lib/jvm/java-8-openjdk/jre/lib/ext/icedtea-sound.jar:/usr/lib/jvm/java-8-openjdk/jre/lib/ext/cldrdata.jar:/usr/lib/jvm/java-8-openjdk/jre/lib/ext/jaccess.jar:/usr/lib/jvm/java-8-openjdk/jre/lib/ext/zipfs.jar:/usr/lib/jvm/java-8-openjdk/jre/lib/ext/java-atk-wrapper.jar:/usr/lib/jvm/java-8-openjdk/jre/lib/ext/sunec.jar:/usr/lib/jvm/java-8-openjdk/jre/lib/ext/localedata.jar:/usr/lib/jvm/java-8-openjdk/jre/lib/ext/sunpkcs11.jar:/usr/lib/jvm/java-8-openjdk/jre/lib/ext/dnsns.jar:/usr/lib/jvm/java-8-openjdk/jre/lib/ext/sunjce_provider.jar:/home/tinymop/tinymop/tracemop/scripts/resources:/home/tinymop/tinymop/tracemop/rv-monitor/target/release/rv-monitor/lib/rv-monitor-rt.jar:/home/tinymop/tinymop/tracemop/rv-monitor/target/release/rv-monitor/lib/rv-monitor.jar:/home/tinymop/aspectj-1.9.7/lib/aspectjrt.jar:/home/tinymop/aspectj-1.9.7/lib/aspectjtools.jar:/home/tinymop/aspectj-1.9.7/lib/aspectjweaver.jar"

function setup() {
    rm -rf ${OUTPUT}
    touch ${OUTPUT}
    echo "configuration,time,traces,unique_traces,violations,unique_violations,unique_violating_traces" >> ${OUTPUT}
    docker image list | grep "lazymop:latest"
    # if [ $? -ne 0 ]; then
    #     echo "Make sure that you have the Docker image lazymop:latest"
    #     echo "Build with: docker build -f Docker/Dockerfile . --tag=lazymop:latest"
    #     exit 1
    # fi
    CONTAINER_ID=$(docker run -itd --rm lazymop:latest)
    echo "CONTAINER_ID: ${CONTAINER_ID}"
    echo "Begin setup"
    docker exec -e ${DOCKER_PATH} ${CONTAINER_ID} ./setup.sh
    docker exec -e ${DOCKER_PATH} ${CONTAINER_ID} git clone https://github.com/${PROJECT}.git
    docker exec -e ${DOCKER_PATH} -w ${DOCKER_HOME}/${REPO} ${CONTAINER_ID} git checkout -f ${COMMIT}
    for CONFIG in ${CONFIGS}; do
        docker exec -e ${DOCKER_PATH} ${CONTAINER_ID} cp -r ${REPO} ${CONFIG}
    done
    docker exec -e ${DOCKER_PATH} -w ${DOCKER_HOME}/tinymop/tracemop ${CONTAINER_ID} git pull origin master
    docker exec -e ${DOCKER_PATH} -e ${DOCKER_CLASSPATH} -w ${DOCKER_HOME}/tinymop/tracemop/scripts ${CONTAINER_ID} bash install.sh true false false
    # This is just to make sure that there's a directory for javamop agent
    docker exec -e ${DOCKER_PATH} -e ${DOCKER_CLASSPATH} -w ${DOCKER_HOME}/tinymop/tracemop/scripts ${CONTAINER_ID} mvn install:install-file -Dfile=track-no-stats-agent.jar -DgroupId="javamop-agent" -DartifactId="javamop-agent" -Dversion="1.0" -Dpackaging="jar"
    docker exec -e ${DOCKER_PATH} -w ${DOCKER_HOME}/tinymop/tracemop/scripts ${CONTAINER_ID} mv track-no-stats-agent.jar tracemop.jar
    docker exec -e ${DOCKER_PATH} -e ${DOCKER_CLASSPATH} -w ${DOCKER_HOME}/tinymop/tracemop/scripts ${CONTAINER_ID} bash install.sh true false false -valg
    docker exec -e ${DOCKER_PATH} -w ${DOCKER_HOME}/tinymop/tracemop/scripts ${CONTAINER_ID} mv track-no-stats-agent.jar valgt.jar
    docker exec -e ${DOCKER_PATH} -w ${DOCKER_HOME}/tinymop/tracemop/plugin ${CONTAINER_ID} mvn install
    docker exec -e ${DOCKER_PATH} -w ${DOCKER_HOME}/tinymop ${CONTAINER_ID} git pull origin integrate-valg
    docker exec -e ${DOCKER_PATH} -w ${DOCKER_HOME}/tinymop ${CONTAINER_ID} git checkout integrate-valg
    docker exec -e ${DOCKER_PATH} -w ${DOCKER_HOME}/tinymop ${CONTAINER_ID} bash make-jars.sh false true true true
    docker exec -e ${DOCKER_PATH} -w ${DOCKER_HOME}/tinymop ${CONTAINER_ID} mv agents/gen-test.jar lazymop.jar
    docker exec -e ${DOCKER_PATH} -w ${DOCKER_HOME}/tinymop ${CONTAINER_ID} bash make-jars.sh false true true true -valg
    docker exec -e ${DOCKER_PATH} -w ${DOCKER_HOME}/tinymop ${CONTAINER_ID} mv agents/gen-test.jar valgl.jar
}

function main() {
    # TraceMOP
    # Ensure the destination directory exists before copying, to prevent 'No such file or directory' errors from 'cp'
    docker exec -e ${DOCKER_PATH} -e ${DOCKER_CLASSPATH} -w ${DOCKER_HOME}/tinymop/tracemop ${CONTAINER_ID} mkdir -p ${DOCKER_HOME}/.m2/repository/javamop-agent/javamop-agent/1.0
    docker exec -e ${DOCKER_PATH} -e ${DOCKER_CLASSPATH} -w ${DOCKER_HOME}/tinymop/tracemop ${CONTAINER_ID} cp scripts/tracemop.jar ${DOCKER_HOME}/.m2/repository/javamop-agent/javamop-agent/1.0/track-no-stats-agent.jar
    start_time=$(date +%s%3N)
    docker exec -e ${DOCKER_PATH} -e ${DOCKER_CLASSPATH} -w ${DOCKER_HOME}/tracemop ${CONTAINER_ID} mvn edu.cornell:tracemop-maven-plugin:1.0:run
    end_time=$(date +%s%3N)
    duration_ms=$((end_time - start_time))
    trace_count=$(docker exec -e ${DOCKER_PATH} -w ${DOCKER_HOME}/tracemop ${CONTAINER_ID} tail -n +2 target/tracemop/all-traces/unique-traces.txt | cut -d ' ' -f 2 | awk '{sum += $1} END {print sum}')
    unique_trace_count=$(docker exec -e ${DOCKER_PATH} -w ${DOCKER_HOME}/tracemop ${CONTAINER_ID} tail -n +2 target/tracemop/all-traces/unique-traces.txt | wc -l | xargs)
    if docker exec -e ${DOCKER_PATH} -w ${DOCKER_HOME}/tracemop ${CONTAINER_ID} test -f violation-counts; then
        violation_count=$(docker exec -e ${DOCKER_PATH} -w ${DOCKER_HOME}/tracemop ${CONTAINER_ID} cat violation-counts | cut -d ' ' -f 1 | awk '{sum += $1} END {print sum}')
        unique_violation_count=$(docker exec -e ${DOCKER_PATH} -w ${DOCKER_HOME}/tracemop ${CONTAINER_ID} cat violation-counts | cut -d ' ' -f 2- | sort | uniq | wc -l | xargs)
    else
        violation_count=0
        unique_violation_count=0
    fi
    unique_violating_trace_count=0
    echo "tracemop,${duration_ms},${trace_count},${unique_trace_count},${violation_count},${unique_violation_count},${unique_violating_trace_count}" >> ${OUTPUT}
    # ValgT
    docker exec -e ${DOCKER_PATH} -e ${DOCKER_CLASSPATH} -w ${DOCKER_HOME}/tinymop/tracemop ${CONTAINER_ID} cp scripts/valgt.jar ${DOCKER_HOME}/.m2/repository/javamop-agent/javamop-agent/1.0/track-no-stats-agent.jar
    start_time=$(date +%s%3N)
    docker exec -e ${DOCKER_PATH} -e ${DOCKER_CLASSPATH} -w ${DOCKER_HOME}/valgt ${CONTAINER_ID} mvn edu.cornell:tracemop-maven-plugin:1.0:run
    end_time=$(date +%s%3N)
    duration_ms=$((end_time - start_time))
    trace_count=$(docker exec -e ${DOCKER_PATH} -w ${DOCKER_HOME}/valgt ${CONTAINER_ID} tail -n +2 target/tracemop/all-traces/unique-traces.txt | cut -d ' ' -f 2 | awk '{sum += $1} END {print sum}')
    unique_trace_count=$(docker exec -e ${DOCKER_PATH} -w ${DOCKER_HOME}/valgt ${CONTAINER_ID} tail -n +2 target/tracemop/all-traces/unique-traces.txt | wc -l | xargs)
    if docker exec -e ${DOCKER_PATH} -w ${DOCKER_HOME}/valgt ${CONTAINER_ID} test -f violation-counts; then
        violation_count=$(docker exec -e ${DOCKER_PATH} -w ${DOCKER_HOME}/valgt ${CONTAINER_ID} cat violation-counts | cut -d ' ' -f 1 | awk '{sum += $1} END {print sum}')
        unique_violation_count=$(docker exec -e ${DOCKER_PATH} -w ${DOCKER_HOME}/valgt ${CONTAINER_ID} cat violation-counts | cut -d ' ' -f 2- | sort | uniq | wc -l | xargs)
    else
        violation_count=0
        unique_violation_count=0
    fi
    unique_violating_trace_count=0
    echo "valgt,${duration_ms},${trace_count},${unique_trace_count},${violation_count},${unique_violation_count},${unique_violating_trace_count}" >> ${OUTPUT}
    # Install extension
    docker exec -e ${DOCKER_PATH} -e ${DOCKER_CLASSPATH} -w ${DOCKER_HOME}/tinymop ${CONTAINER_ID} cp extensions/tinymop-extension-1.0.jar ${DOCKER_HOME}/apache-maven/lib/ext
    # LazyMOP
    docker exec -e ${DOCKER_PATH} -e ${DOCKER_CLASSPATH} -w ${DOCKER_HOME}/tinymop ${CONTAINER_ID} cp lazymop.jar ${DOCKER_HOME}/.m2/repository/javamop-agent/javamop-agent/1.0/javamop-agent-1.0.jar
    docker exec -e ${DOCKER_PATH} -e ${DOCKER_CLASSPATH} -w ${DOCKER_HOME}/lazymop ${CONTAINER_ID} mkdir -p lazymop-traces
    start_time=$(date +%s%3N)
    docker exec -e ${DOCKER_PATH} -e ${DOCKER_CLASSPATH} -e "COLLECT_TRACES=1" -e "TINYMOP_TRACEDB_PATH=${DOCKER_HOME}/lazymop/lazymop-traces" -w ${DOCKER_HOME}/lazymop ${CONTAINER_ID} mvn test
    end_time=$(date +%s%3N)
    duration_ms=$((end_time - start_time))
    trace_count=$(docker exec -e ${DOCKER_PATH} -w ${DOCKER_HOME}/lazymop/lazymop-traces ${CONTAINER_ID} grep -ir "bindings" | cut -d ' ' -f 2 | awk '{sum += $1} END {print sum}')
    unique_trace_count=$(docker exec -e ${DOCKER_PATH} -w ${DOCKER_HOME}/lazymop/lazymop-traces ${CONTAINER_ID} sh -c 'echo $(($(find . -name "*-traces" | xargs wc -l | tail -1 | xargs | cut -d " " -f 1) - $(find . -name "*-traces" | wc -l | xargs) * 2))')
    unique_violating_trace_count=$(docker exec -e ${DOCKER_PATH} -w ${DOCKER_HOME}/lazymop/lazymop-traces ${CONTAINER_ID} sh -c 'echo $(($(find . -name "*-violations" | xargs wc -l | tail -n 1 | xargs | cut -d " " -f 1) - $(find . -name "*-violations" | wc -l | xargs) * 3))')
    violation_count=0
    unique_violation_count=0
    echo "lazymop,${duration_ms},${trace_count},${unique_trace_count},${violation_count},${unique_violation_count},${unique_violating_trace_count}" >> ${OUTPUT}
    # ValgL
    docker exec -e ${DOCKER_PATH} -e ${DOCKER_CLASSPATH} -w ${DOCKER_HOME}/tinymop ${CONTAINER_ID} cp valgl.jar ${DOCKER_HOME}/.m2/repository/javamop-agent/javamop-agent/1.0/javamop-agent-1.0.jar
    start_time=$(date +%s%3N)
    docker exec -e ${DOCKER_PATH} -e ${DOCKER_CLASSPATH} -e "COLLECT_TRACES=1" -e "TINYMOP_TRACEDB_PATH=${DOCKER_HOME}/valgl/lazymop-traces" -w ${DOCKER_HOME}/valgl ${CONTAINER_ID} mvn test
    end_time=$(date +%s%3N)
    duration_ms=$((end_time - start_time))
    trace_count=$(docker exec -e ${DOCKER_PATH} -w ${DOCKER_HOME}/valgl/lazymop-traces ${CONTAINER_ID} grep -ir "bindings" | cut -d ' ' -f 2 | awk '{sum += $1} END {print sum}')
    unique_trace_count=$(docker exec -e ${DOCKER_PATH} -w ${DOCKER_HOME}/valgl/lazymop-traces ${CONTAINER_ID} sh -c 'echo $(($(find . -name "*-traces" | xargs wc -l | tail -1 | xargs | cut -d " " -f 1) - $(find . -name "*-traces" | wc -l | xargs) * 2))')
    unique_violating_trace_count=$(docker exec -e ${DOCKER_PATH} -w ${DOCKER_HOME}/valgl/lazymop-traces ${CONTAINER_ID} sh -c 'echo $(($(find . -name "*-violations" | xargs wc -l | tail -n 1 | xargs | cut -d " " -f 1) - $(find . -name "*-violations" | wc -l | xargs) * 3))')
    violation_count=0
    unique_violation_count=0
    echo "valgl,${duration_ms},${trace_count},${unique_trace_count},${violation_count},${unique_violation_count},${unique_violating_trace_count}" >> ${OUTPUT}
}

function cleanup() {
    docker stop ${CONTAINER_ID}
}

setup
main
cleanup
