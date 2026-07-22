#!/bin/bash
#
# Build TinyMOP Java agent
#

FOR_IMM=${1:-false}
ENABLE_ON_DEMAND_SYNC=${2:-true}
ENABLE_INT_ENCODING=${3:-true}
COLLECT_TEST_TRACES=${4:-true}

if [[ $# -gt 0 && "$1" != -* ]]; then
    FOR_IMM="$1"
    shift
fi
if [[ $# -gt 0 && "$1" != -* ]]; then
    ENABLE_ON_DEMAND_SYNC="$1"
    shift
fi
if [[ $# -gt 0 && "$1" != -* ]]; then
    ENABLE_INT_ENCODING="$1"
    shift
fi
if [[ $# -gt 0 && "$1" != -* ]]; then
    COLLECT_TEST_TRACES="$1"
    shift
fi

VALG_ARGS=("$@")
valg=false
traj=false
spec_configured=false
index=0
while [[ ${index} -lt ${#VALG_ARGS[@]} ]]; do
    option="${VALG_ARGS[index]}"
    case "${option}" in
        -valg)
            valg=true
            index=$((index + 1))
            if [[ ${index} -lt ${#VALG_ARGS[@]} && "${VALG_ARGS[index]}" != -* ]]; then
                if [[ ! "${VALG_ARGS[index]}" =~ ^\{[0-9.eE+-]+(,[0-9.eE+-]+){4}\}$ ]]; then
                    echo "[make-jars.sh] Error: -valg expects {alpha,epsilon,threshold,initc,initn}"
                    exit 1
                fi
                index=$((index + 1))
            fi
            ;;
        -traj)
            traj=true
            index=$((index + 1))
            ;;
        -spec)
            if [[ $((index + 2)) -ge ${#VALG_ARGS[@]} ]]; then
                echo "[make-jars.sh] Error: -spec expects a specification name and a configuration or off"
                exit 1
            fi
            spec_value="${VALG_ARGS[index + 2]}"
            if [[ "${spec_value}" != "off" && ! "${spec_value}" =~ ^\{[0-9.eE+-]+(,[0-9.eE+-]+){4}\}$ ]]; then
                echo "[make-jars.sh] Error: -spec expects {alpha,epsilon,threshold,initc,initn} or off"
                exit 1
            fi
            spec_configured=true
            index=$((index + 3))
            ;;
        *)
            echo "[make-jars.sh] Error: unknown option ${option}"
            exit 1
            ;;
    esac
done

if [[ "${spec_configured}" == "true" && "${valg}" != "true" ]]; then
    echo "[make-jars.sh] Error: -spec can only be used with -valg"
    exit 1
fi
if [[ "${traj}" == "true" && "${valg}" != "true" ]]; then
    echo "[make-jars.sh] Error: -traj can only be used with -valg"
    exit 1
fi

git clean -f &> /dev/null 
git checkout monitoring-engine/ &> /dev/null

agent_dir="agents"
rm -rf ${agent_dir}/gen.jar
mkdir -p ${agent_dir}

# jar name based on test collection flag
if [[ ${COLLECT_TEST_TRACES} == "false" ]]; then
    jar_name="gen.jar"
else
    jar_name="gen-test.jar"
fi

bash s.sh "${FOR_IMM}" "${ENABLE_ON_DEMAND_SYNC}" "${ENABLE_INT_ENCODING}" \
    "${COLLECT_TEST_TRACES}" "${VALG_ARGS[@]}" &> gol-build-agent.log
mv agent.jar ${agent_dir}/${jar_name}
grep BUILD gol-build-agent.log

mv gol-build-agent.log ${agent_dir}
