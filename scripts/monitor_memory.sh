#!/bin/bash

OUTPUT=$1
ID=$2

MAX_MEMORY=0
while true; do
  while [[ -z ${ID} ]]; do
    ID=$(ps aux | grep "/usr/lib/jvm/java-8-openjdk/jre/bin/java" | grep " \-javaagent:" | awk '{print $2}')
    sleep 1
  done

  memory=$(ps faux | grep -Fv -e 'grep' -e 'bash' | grep $ID | grep "/usr/lib/jvm/java-8-openjdk/jre/bin/java" | grep " \-javaagent:" | cut -d '\' -f 1 | awk '{print $6}')
  if [[ -n "${memory}" ]]; then
    if [[ "${memory}" -gt "${MAX_MEMORY}" ]]; then
      MAX_MEMORY=${memory}
      echo "${MAX_MEMORY}" > ${OUTPUT}
    fi
  else
    ID=""
  fi

  sleep 1
done
