#!/bin/bash
#
# Build TinyMOP Java agent
#

FOR_IMM=${1:-false}
ENABLE_ON_DEMAND_SYNC=${2:-true}
ENABLE_INT_ENCODING=${3:-true}

git clean -f &> /dev/null 
git checkout monitoring-engine/ &> /dev/null

agent_dir="agents"
rm -rf ${agent_dir}/gen.jar
mkdir -p ${agent_dir}

bash s.sh ${FOR_IMM} ${ENABLE_ON_DEMAND_SYNC} ${ENABLE_INT_ENCODING} &> gol-build-agent.log
mv agent.jar ${agent_dir}/gen.jar
grep BUILD gol-build-agent.log

mv gol-build-agent.log ${agent_dir}
