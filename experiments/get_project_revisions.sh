#!/bin/bash
#
# Select projects and SHAs for evaluation
# Usage: get_project_revisions.sh <revisions> <repo> <sha> <output-dir>
# Run compile, test, JavaMOP, and TinyMOP
#
REVISIONS=$1
REPO=$2
SHA=$3
OUTPUT_DIR=$4
SCRIPT_DIR=$(cd $(dirname $0) && pwd)
PROJECT_NAME=$(echo ${REPO} | tr / -)

export EXTENSION_PATH=${SCRIPT_DIR}/../extensions/tinymop-extension-1.0.jar

source ${SCRIPT_DIR}/constants.sh
source ${SCRIPT_DIR}/utils.sh

TMP_DIR=${TMP_DIR}-${PROJECT_NAME}

function check_input() {
  if [[ -z ${REVISIONS} || -z ${REPO} || -z ${SHA} || -z ${OUTPUT_DIR} ]]; then
    echo "Usage bash get_project_revisions.sh <revisions> <repo> <sha> <output-dir>"
    exit 1
  fi
  
  if [[ ! ${OUTPUT_DIR} =~ ^/.* ]]; then
    OUTPUT_DIR=${SCRIPT_DIR}/${OUTPUT_DIR}
  fi
  
  if [[ ! -f ${SCRIPT_DIR}/../agents/gen-normal.jar ]]; then
    echo "Building [normal] gen.jar agent"
    pushd ${SCRIPT_DIR}/.. &> /dev/null
    bash make-jars.sh
    mv ${SCRIPT_DIR}/../agents/gen.jar ${SCRIPT_DIR}/../agents/gen-normal.jar
    popd &> /dev/null
  fi

  LOG_DIR=${OUTPUT_DIR}/logs
  mkdir -p ${OUTPUT_DIR}
  mkdir -p ${LOG_DIR}
}

function log() {
  local message=$1
  echo "[TinyMOP] ${message}"
  echo "[TinyMOP] ${message}" &>> ${LOG_DIR}/output.log
}

function test_commit() {
  local sha=$1
  mkdir -p ${LOG_DIR}/${sha}
  git checkout ${sha} &> /dev/null
  
  run_compile ${sha}
  if [[ $? -ne 0 ]]; then
    echo "${sha},0,0,0,0" >> ${LOG_DIR}/commits-check.txt
    
    log "Cannot use commit ${sha} due to compile error"
    return 1
  fi
  
  run_test ${sha}
  if [[ $? -ne 0 ]]; then
    echo "${sha},1,0,0,0" >> ${LOG_DIR}/commits-check.txt
    
    log "Cannot use commit ${sha} due to test error"
    return 1
  fi
  
  run_test_with_javamop ${sha}
  if [[ $? -ne 0 ]]; then
    echo "${sha},1,1,0,0" >> ${LOG_DIR}/commits-check.txt
    
    log "Cannot use commit ${sha} due to JavaMOP error"
    return 1
  fi
  
  run_test_with_tinymop ${sha}
  if [[ $? -ne 0 ]]; then
    echo "${sha},1,1,1,0" >> ${LOG_DIR}/commits-check.txt
    
    log "Cannot use commit ${sha} due to TinyMOP error"
    return 1
  fi
  
  echo "${sha},1,1,1,1" >> ${LOG_DIR}/commits-check.txt
  log "Finished testing commit ${sha}"
  return 0
}

function run_compile() {
  local sha=$1
  mkdir -p ${TMP_DIR} && chmod -R +w ${TMP_DIR} && rm -rf ${TMP_DIR} && mkdir -p ${TMP_DIR}
  
  log "Running test-compile"
  local start=$(date +%s%3N)
  (time timeout ${TIMEOUT} mvn -Djava.io.tmpdir=${TMP_DIR} ${SKIP} clean test-compile) &>> ${LOG_DIR}/${sha}/compile.log
  local status=$?
  local end=$(date +%s%3N)
  local duration=$((end - start))
  echo "[TinyMOP] Duration: ${duration} ms, status: ${status}" |& tee -a ${LOG_DIR}/${sha}/compile.log
  return ${status}
}

function run_test() {
  local sha=$1
  mkdir -p ${TMP_DIR} && chmod -R +w ${TMP_DIR} && rm -rf ${TMP_DIR} && mkdir -p ${TMP_DIR}
  
  log "Running test without MOP"
  local start=$(date +%s%3N)
  (time timeout ${TIMEOUT} mvn -Djava.io.tmpdir=${TMP_DIR} ${SKIP} surefire:test) &>> ${LOG_DIR}/${sha}/test.log
  local status=$?
  local end=$(date +%s%3N)
  local duration=$((end - start))
  echo "[TinyMOP] Duration: ${duration} ms, status: ${status}" |& tee -a ${LOG_DIR}/${sha}/test.log
  
  return ${status}
}

function run_test_with_javamop() {
  local sha=$1
  mkdir -p ${TMP_DIR} && chmod -R +w ${TMP_DIR} && rm -rf ${TMP_DIR} && mkdir -p ${TMP_DIR}
  export MOP_AGENT_PATH="-javaagent:${SCRIPT_DIR}/../agents/three.jar"
  
  log "Running test with MOP"
  local start=$(date +%s%3N)
  (time timeout ${TIMEOUT} mvn -Djava.io.tmpdir=${TMP_DIR} ${SKIP} surefire:test -Dmaven.ext.class.path=${EXTENSION_PATH}) &>> ${LOG_DIR}/${sha}/mop.log
  local status=$?
  local end=$(date +%s%3N)
  local duration=$((end - start))
  echo "[TinyMOP] Duration: ${duration} ms, status: ${status}" |& tee -a ${LOG_DIR}/${sha}/mop.log

  move_violations ${LOG_DIR}/${sha} violations
  return ${status}
}

function run_test_with_tinymop() {
  local sha=$1
  mkdir -p ${TMP_DIR} && chmod -R +w ${TMP_DIR} && rm -rf ${TMP_DIR} && mkdir -p ${TMP_DIR}
  export MOP_AGENT_PATH="-javaagent:${SCRIPT_DIR}/../agents/gen-normal.jar"
  export TINYMOP_TRACEDB_PATH=${LOG_DIR}/${sha}/tinymop-traces
  export COLLECT_TRACES=1
  mkdir -p ${TINYMOP_TRACEDB_PATH}
  
  log "Running test with GEN"
  local start=$(date +%s%3N)
  (time timeout ${TIMEOUT} mvn -Djava.io.tmpdir=${TMP_DIR} ${SKIP} surefire:test -Dsurefire.exitTimeout=172800 -Dmaven.ext.class.path=${EXTENSION_PATH}) &>> ${LOG_DIR}/${sha}/gen.log
  local status=$?
  local end=$(date +%s%3N)
  local duration=$((end - start))
  echo "[TinyMOP] Duration: ${duration} ms, status: ${status}" |& tee -a ${LOG_DIR}/${sha}/gen.log
  return ${status}
}

function get_project() {
  pushd ${OUTPUT_DIR} &> /dev/null
  
  export GIT_TERMINAL_PROMPT=0
  git clone https://github.com/${REPO} project &>> ${LOG_DIR}/clone.log
  pushd ${OUTPUT_DIR}/project &> /dev/null
  git checkout ${SHA} &>> ${LOG_DIR}/clone.log
  if [[ $? -ne 0 ]]; then
    log "Skip project: cannot clone repository"
    exit 1
  fi
  
  if [[ -f ${OUTPUT_DIR}/project/.gitmodules ]]; then
    log "Skip project: project contains submodule"
    exit 1
  fi
  
  local failure=0
  for commit in $(git rev-list --first-parent --topo-order --remove-empty --no-merges --branches=master -n 500 HEAD); do
    if [[ -z $(git show ${commit} --name-status | grep \\\.java) ]]; then
      # java files not changed
      continue
    fi
    
    if [[ ${commit} == ${SHA} ]]; then
      continue
    fi

    log "Testing commit ${commit}"
    test_commit ${commit}
    if [[ $? -ne 0 ]]; then
      failure=$((failure + 1))
      if [[ ${failure} -ge 10 ]]; then
        log "Skip project: 10 failures in a row"
        exit 1
      fi
    else
      failure=0
    fi
    
    local success=$(grep ,1,1,1,1 ${LOG_DIR}/commits-check.txt | wc -l)
    if [[ ${success} -ge ${REVISIONS} ]]; then
      break
    fi
    log "Found ${success} projects"
  done

  popd &> /dev/null
  popd &> /dev/null
  
  log "Done, found $(grep ,1,1,1,1 ${LOG_DIR}/commits-check.txt | wc -l)/$(cat ${LOG_DIR}/commits-check.txt | wc -l) valid commits"
}

export RVMLOGGINGLEVEL=UNIQUE
check_input
get_project
