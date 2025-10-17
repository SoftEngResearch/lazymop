#!/bin/bash

TINYMOP_SPEC=$1
JAVAMOP_OR_TINYMOP=$2

if [[ -z ${TMP_DIRECTORY} ]]; then
  export TMP_DIRECTORY="${TMP_DIR}"
fi

if [[ ! -d emop ]]; then
  git clone https://github.com/SoftEngResearch/emop
  cd emop
  bash scripts/install-starts.sh
else
  cd emop
fi

if [[ ${JAVAMOP_OR_TINYMOP} == "tinymop" ]]; then
  sed -i '/writer.println("<aspect name=\\"mop." + spec + "\\"\/>");/a\\t\twriter.println("<aspect name=\\"mop." + spec + "BaseAspect\\"/>");' emop-core/src/main/java/edu/cornell/emop/util/Util.java
  sed -i 's|// Write body|writer.println("<aspect name=\\"mop.ThreadAspect\\"/>");|g' emop-core/src/main/java/edu/cornell/emop/util/Util.java
  sed -i 's|.filter(spec -> spec.contains("Aspect"))|.filter(spec -> !spec.contains("DoMonitoring") \&\& !spec.contains("NoMonitoring") \&\& !spec.contains("Aspect"))|' emop-core/src/main/java/edu/cornell/emop/util/Util.java
  
  pushd emop-maven-plugin/src/main/resources/weaved-specs/ &> /dev/null
  for file in *_*; do
    filename="${file%.*}"
    extension="${file##*.}"
    new_filename="${filename//_/}"
    new_filename="${new_filename//MonitorAspect/}"
    new_name="${new_filename}.${extension}"
    sed -i "s/$filename/$new_filename/g" "$file"
    mv "$file" "$new_name"
  done
  
  # Remove raw specs
  comm -23 <(ls | cut -d '.' -f 1 | sort) <(ls ${TINYMOP_SPEC} | sed "s/_//g" | cut -d '.' -f 1 | sort) | xargs -I {} rm {}.aj
  popd &> /dev/null
else
  pushd emop-maven-plugin/src/main/resources/weaved-specs/ &> /dev/null
  comm -23 <(ls | sed "s/MonitorAspect.aj//g" | sort) <(ls ${TINYMOP_SPEC} | cut -d '.' -f 1 | sort) | xargs -I {} rm {}MonitorAspect.aj
  popd &> /dev/null
fi

mvn clean install -Dcheckstyle.skip -Djava.io.tmpdir=${TMP_DIRECTORY}

git checkout -- emop-core
git checkout -- emop-maven-plugin
git clean -f
