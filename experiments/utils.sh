#!/bin/bash

function move_violations() {
  local directory=$1
  local filename=${2:-"violation-counts"}
  local copy=${3:-"false"}

  for violation in $(find -name "violation-counts"); do
    if [[ -n ${violation} ]]; then
      mkdir -p ${directory}
      local name=$(echo "${violation}" | rev | cut -d '/' -f 2 | rev)
      if [[ ${name} != "." ]]; then
        # Is MMMP, add module name to file name
        if [[ ${copy} == "true" ]]; then
          cp ${violation} ${directory}/${filename}_${name}.txt
        else
          mv ${violation} ${directory}/${filename}_${name}.txt
        fi
      else
        if [[ ${copy} == "true" ]]; then
          cp ${violation} ${directory}/${filename}.txt
        else
          mv ${violation} ${directory}/${filename}.txt
        fi
      fi
    fi
  done
}

function delete_violations() {
  for violation in $(find -name "violation-counts"); do
    if [[ -n ${violation} ]]; then
      rm ${violation}
    fi
  done
}

function move_jfr() {
  local directory=$1
  local filename=$2
  for jfr in $(find -name "profile.jfr"); do
    if [[ -n ${jfr} ]]; then
      local name=$(echo "${jfr}" | rev | cut -d '/' -f 2 | rev)
      mkdir -p ${directory}
      if [[ ${name} != "." ]]; then
        # Is MMMP, add module name to file name
        mv ${jfr} ${directory}/${filename}_${name}.jfr
      else
        mv ${jfr} ${directory}/${filename}.jfr
      fi
    fi
  done
}

function log() {
  echo "[TinyMOP] $1"
}
