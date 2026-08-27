#!/bin/bash

PROJECTS_CSV=$1

rm -rf tmp_commands.sh
rm -rf output
mkdir -p output
rm -rf logs
mkdir -p logs

while IFS=',' read -r project commit unused; do
    if [ "$project" == "repo" ]; then
        continue
    fi
    project_transformed=$(echo $project | tr '/' '_')
    echo "bash test_valgl_one.sh $project $commit output/$project_transformed.csv &> logs/$project_transformed.log" >> tmp_commands.sh
done < $PROJECTS_CSV
