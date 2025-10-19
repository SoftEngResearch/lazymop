#!/bin/bash

SCRIPT_DIR=$( cd $( dirname $0 ) && pwd )
TINYMOP_DIR="${SCRIPT_DIR}/tinymop"

function clone_repository() {
  echo "Cloning lazymop repository"
  pushd ${SCRIPT_DIR} &> /dev/null
  git clone https://github.com/SoftEngResearch/lazymop tinymop
  popd &> /dev/null
}

function build_extension() {
  echo "Building lazymop extension"
  pushd ${TINYMOP_DIR}/extensions/tinymop-extension &> /dev/null
  mvn package
  cp target/tinymop-extension-1.0.jar ${TINYMOP_DIR}/extensions/
  popd &> /dev/null
}

function install() {
  echo "Install lazymop's frontend"
  pushd ${TINYMOP_DIR} &> /dev/null
  bash scripts/install-javamop-frontend.sh
  popd &> /dev/null
}

function setup() {
  clone_repository
  build_extension
  install
}

setup
