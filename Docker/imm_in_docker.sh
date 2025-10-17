#!/bin/bash
#
# Run imm experiment in Docker
# Before running this script, run `docker login`
# Usage: imm_in_docker.sh <projects-list> <output-dir> <sha-dir> [branch=false] [timeout=86400s] [test-timeout=1800s]
#
SCRIPT_DIR=$(cd $(dirname $0) && pwd)

PROJECTS_LIST=$1
OUTPUT_DIR=$2
SHA_DIR=$3
BRANCH=$4
TIMEOUT=$5
TEST_TIMEOUT=$6

function check_input() {
  if [[ ! -f ${PROJECTS_LIST} || -z ${OUTPUT_DIR} ]]; then
    echo "Usage: imm_in_docker.sh <projects-list> <output-dir> <sha-dir> [branch=false] [timeout=86400s] [test-timeout=1800s]"
    exit 1
  fi

  if [[ ! ${OUTPUT_DIR} =~ ^/.* ]]; then
    OUTPUT_DIR=${SCRIPT_DIR}/${OUTPUT_DIR}
  fi

  if [[ ! ${SHA_DIR} =~ ^/.* ]]; then
    SHA_DIR=${SCRIPT_DIR}/${SHA_DIR}
  fi


  mkdir -p ${OUTPUT_DIR}

  if [[ ! -s ${PROJECTS_LIST} ]]; then
    echo "${PROJECTS_LIST} is empty..."
    exit 0
  fi

  if [[ -z $(grep "###" ${PROJECTS_LIST}) ]]; then
    echo "You must end your projects-list file with ###"
    exit 1
  fi

  if [[ -z ${TIMEOUT} ]]; then
    TIMEOUT=86400s
  fi
}


function run_project() {
  local repo=$1

  local project_name=$(echo ${repo} | tr / -)

  local start=$(date +%s%3N)
  echo "Running ${project_name} with SHA ${sha}"
  
  if [[ ! -f ${SHA_DIR}/${project_name}.txt ]]; then
    echo "Unable to find SHA file for ${project_name}"
    return
  fi

  mkdir -p ${OUTPUT_DIR}/${project_name}

  local id=$(docker run -itd --name ${project_name} lazymop)
  docker exec -w /home/tinymop/tinymop ${id} git pull
  if [[ $? -ne 0 ]]; then
    echo "Unable to pull project ${project_name}, try again in 60 seconds" |& tee -a docker.log
    sleep 60
    docker exec -w /home/tinymop/tinymop ${id} git pull
    if [[ $? -ne 0 ]]; then
      echo "Skip ${project_name} because script can't pull" |& tee -a docker.log
      return
    fi
  fi
  
  if [[ -n ${BRANCH} && ${BRANCH} != "false" ]]; then
    docker exec -w /home/tinymop/tinymop ${id} git checkout ${BRANCH}
    docker exec -w /home/tinymop/tinymop ${id} git pull
  fi
  
  sha=$(cat ${SHA_DIR}/${project_name}.txt | paste -sd' ')
  
  if [[ -n ${TEST_TIMEOUT} ]]; then
    echo "Setting test timeout to ${TEST_TIMEOUT}"
    docker exec -w /home/tinymop/tinymop ${id} sed -i "s/TIMEOUT=.*/TIMEOUT=${TEST_TIMEOUT}/" experiments/constants.sh
  fi
  
  echo "Run command: timeout ${TIMEOUT} bash experiments/run_imm.sh ${repo} /home/tinymop/output ${sha}"
  timeout ${TIMEOUT} docker exec -w /home/tinymop/tinymop -e M2_HOME=/home/tinymop/apache-maven -e MAVEN_HOME=/home/tinymop/apache-maven -e CLASSPATH=/home/tinymop/aspectj-1.9.7/lib/aspectjtools.jar:/home/tinymop/aspectj-1.9.7/lib/aspectjrt.jar:/home/tinymop/aspectj-1.9.7/lib/aspectjweaver.jar: -e PATH=/home/tinymop/apache-maven/bin:/usr/lib/jvm/java-8-openjdk/bin:/home/tinymop/aspectj-1.9.7/bin:/home/tinymop/aspectj-1.9.7/lib/aspectjweaver.jar:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin ${id} timeout ${TIMEOUT} bash experiments/run_imm.sh ${repo} /home/tinymop/output ${sha} &> ${OUTPUT_DIR}/${project_name}/docker.log

  docker cp ${id}:/home/tinymop/output ${OUTPUT_DIR}/${project_name}/output
  docker cp ${id}:/home/tinymop/.m2/repository ${OUTPUT_DIR}/${project_name}/repo

  docker rm -f ${id}
  
  local end=$(date +%s%3N)
  local duration=$((end - start))
  echo "Finished running ${project_name} in ${duration} ms" |& tee -a docker.log
}

function run_all() {
  local start=$(date +%s%3N)
  while true; do
    if [[ ! -s ${PROJECTS_LIST} ]]; then
      echo "${PROJECTS_LIST} is empty..."
      exit 0
    fi

    local project=$(head -n 1 ${PROJECTS_LIST})
    if [[ ${project} == "###" ]]; then
      local end=$(date +%s%3N)
      local duration=$((end - start))
      echo "Finished running all projects in ${duration} ms" |& tee -a docker.log

      exit 0
    fi

    if [[ -z $(grep "###" ${PROJECTS_LIST}) ]]; then
      echo "You must end your projects-list file with ###"
      exit 1
    fi

    sed -i 1d ${PROJECTS_LIST}
    echo $project >> ${PROJECTS_LIST}
    run_project ${project} $@
  done

}

check_input
run_all $@
