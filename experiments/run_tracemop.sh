#!/bin/bash
SCRIPT_DIR=$( cd $( dirname $0 ) && pwd )

REPO=$1
SHA=$2
PROJECT=$(echo ${REPO} | tr / -)

if [[ -z ${SHA} ]]; then
    echo "Usage: bash run_tracemop.sh <project> <sha> [tinymop-only: false]"
    exit 1
fi

if [[ ! -d ${PROJECT} ]]; then
    git clone https://github.com/${REPO} ${PROJECT}
fi
pushd ${PROJECT} &> /dev/null
git checkout ${SHA}

echo "Download dependencies"
mvn test-compile
export MAVEN_OPTS="-Xmx500g -XX:-UseGCOverheadLimit"

echo -n "Track: "
export RVMLOGGINGLEVEL=UNIQUE
rm -rf ${SCRIPT_DIR}/${PROJECT}/all-traces && mkdir -p ${SCRIPT_DIR}/${PROJECT}/all-traces
export TRACEDB_PATH=${SCRIPT_DIR}/${PROJECT}/all-traces
export COLLECT_TRACES=1
export COLLECT_MONITORS=1
export TRACEDB_RANDOM=1

export MOP_AGENT_PATH="-javaagent:${SCRIPT_DIR}/../agents/threeTrack.jar"
filename="track.log"
(time mvn surefire:test -Dsurefire.exitTimeout=172800 -Dmaven.ext.class.path=${SCRIPT_DIR}/../extensions/tinymop-extension-1.0.jar)

num_db=0
last_db=""
for db in $(ls | grep "all-traces-"); do
    # search directory starts with all-traces-*
    if [[ ! -f ${db}/unique-traces.txt || ! -f ${db}/specs-frequency.csv || ! -f ${db}/locations.txt || ! -f ${db}/traces.txt ]]; then
        continue
    fi
    
    mv ${db}/unique-traces.txt ${db}/traces-id.txt
    (time python3 ${SCRIPT_DIR}/../tracemop/scripts/count-traces-frequency.py ${db}/)
    rm ${db}/traces-id.txt ${db}/traces.txt
    num_db=$((num_db + 1))
    last_db=${db}
done
