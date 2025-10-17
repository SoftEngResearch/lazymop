SCRIPT_DIR=$(cd $(dirname $0) && pwd)
FOR_IMM=$1
ENABLE_ON_DEMAND_SYNC=$2
ENABLE_INT_ENCODING=$3

echo "===== Settings ====="
echo "FOR_IMM: ${FOR_IMM}"
echo "ENABLE_ON_DEMAND_SYNC: ${ENABLE_ON_DEMAND_SYNC}"
echo "ENABLE_INT_ENCODING: ${ENABLE_INT_ENCODING}"

rm -rf out output && mkdir out output

mvn clean test-compile -Dcheckstyle.skip
if [[ $? -ne 0 ]]; then
    echo "UNABLE TO COMPILE"
    exit 1
fi

if [[ ${ENABLE_ON_DEMAND_SYNC} == "false" ]]; then
    mvn -pl spec-parser exec:java -Dexec.mainClass="edu.lazymop.tinymop.specparser.Main" -Dexec.args="output props false"
    status=$?
else
    mvn -pl spec-parser exec:java -Dexec.mainClass="edu.lazymop.tinymop.specparser.Main" -Dexec.args="output props"
    status=$?
fi

if [[ $? -ne 0 ]]; then
    echo "UNABLE TO GENERATE CLASSES"
    exit 1
fi

specs_files=()
specs=()
pushd output
for file in $(ls); do
    specs_files+=("${file}")
    if [[ ${file} == *Monitor.java ]]; then
        # Move to monitoring-engine's monitors directory
        mv ${SCRIPT_DIR}/output/${file} ${SCRIPT_DIR}/monitoring-engine/src/main/java/edu/lazymop/tinymop/monitoring/monitors/${file}
    elif [[ ${file} == *.aj && ${file} != *MonitorAspect.aj ]]; then
        # Move to out
        mv ${SCRIPT_DIR}/output/${file} ${SCRIPT_DIR}/out/${file}
        specs+=("${file}")
    elif [[ ${file} == *MonitorManager.java ]]; then
        # move to monitorsmanager
        mv ${SCRIPT_DIR}/output/${file} ${SCRIPT_DIR}/monitoring-engine/src/main/java/edu/lazymop/tinymop/monitoring/monitorsmanager/${file}
    elif [[ ${file} == *.java ]]; then
        mv ${SCRIPT_DIR}/output/${file} ${SCRIPT_DIR}/monitoring-engine/src/main/java/edu/lazymop/tinymop/monitoring/slicing/algod/${file}
    fi
done
popd

if [[ ${ENABLE_INT_ENCODING} == "false" ]]; then
    sed -i "s/obj.node.event > 0/obj.node.event.length() > 0/g" ./monitoring-engine/src/main/java/edu/lazymop/tinymop/monitoring/util/SpecializedSlicingAlgorithmUtil.java
    sed -i "s/int result = obj.node.event;//g" ./monitoring-engine/src/main/java/edu/lazymop/tinymop/monitoring/util/SpecializedSlicingAlgorithmUtil.java
    sed -i "s/int eventName = result & 15;//g" ./monitoring-engine/src/main/java/edu/lazymop/tinymop/monitoring/util/SpecializedSlicingAlgorithmUtil.java
    sed -i "s/int location = result >> 4;//g" ./monitoring-engine/src/main/java/edu/lazymop/tinymop/monitoring/util/SpecializedSlicingAlgorithmUtil.java
    sed -i "s/String event = \"E\" + eventName + \"~\" + location;/String event = obj.node.event;/g" ./monitoring-engine/src/main/java/edu/lazymop/tinymop/monitoring/util/SpecializedSlicingAlgorithmUtil.java
    sed -i "s/int result = obj.node.event;//g" ./monitoring-engine/src/main/java/edu/lazymop/tinymop/monitoring/util/SpecializedSlicingAlgorithmUtil.java
    sed -i "s/monitorManager.locationsInChangedClasses\[location\]/monitorManager.locationsInChangedClasses[Integer.parseInt(event.substring(event.indexOf('~') + 1))]/g" ./monitoring-engine/src/main/java/edu/lazymop/tinymop/monitoring/util/SpecializedSlicingAlgorithmUtil.java

    sed -i "s/int event/String event/g" ./monitoring-engine/src/main/java/edu/lazymop/tinymop/monitoring/datastructure/Trie.java
    sed -i "s/Integer event/String event/g" ./monitoring-engine/src/main/java/edu/lazymop/tinymop/monitoring/datastructure/Trie.java
    sed -i "s/Integer, Node/String, Node/g" ./monitoring-engine/src/main/java/edu/lazymop/tinymop/monitoring/datastructure/Trie.java
    sed -i "s/IntIntHashMap childrenKeys = new IntIntHashMap/HashMap<String, Integer> childrenKeys = new HashMap<>/g" ./monitoring-engine/src/main/java/edu/lazymop/tinymop/monitoring/datastructure/Trie.java
    sed -i "s/new Node(0);/new Node(\"\");/g" ./monitoring-engine/src/main/java/edu/lazymop/tinymop/monitoring/datastructure/Trie.java

    grep -rl "int event" ./monitoring-engine/src/main/java/edu/lazymop/tinymop/monitoring/slicing/algod | xargs sed -i "s/int event/String event/g"
    grep -rl "int eID" ./monitoring-engine/src/main/java/edu/lazymop/tinymop/monitoring/slicing/algod | xargs sed -i "s/int eID/String eID/g"
    grep -rl "event << 4" ./monitoring-engine/src/main/java/edu/lazymop/tinymop/monitoring/monitorsmanager | xargs sed -i -E "s/event << 4 \| ([0-9]+)/\"E\1~\" + event/g"

    rm ./monitoring-engine/src/test/java/edu/lazymop/tinymop/monitoring/TrieTest.java
    rm ./monitoring-engine/src/test/java/edu/lazymop/tinymop/monitoring/SpecializedSlicingAlgorithmUtilTest.java
fi

mvn clean install -DskipTests -Dcheckstyle.skip  # don't run checkstyle on generated code
if [[ $? -ne 0 ]]; then
    echo "UNABLE TO COMPILE CLASSES"
    exit 1
fi

# Restore
#for file in "${specs_files[@]}"; do
#   if [[ ${file} == *Monitor.java ]]; then
#       git restore ${SCRIPT_DIR}/monitoring-engine/src/main/java/edu/lazymop/tinymop/monitoring/monitors/${file} &> /dev/null
#   elif [[ ${file} == *.aj && ${file} != *MonitorAspect.aj ]]; then
#       git restore ${SCRIPT_DIR}/out/${file} &> /dev/null
#   elif [[ ${file} == *MonitorManager.java ]]; then
#       git restore ${SCRIPT_DIR}/monitoring-engine/src/main/java/edu/lazymop/tinymop/monitoring/monitorsmanager/${file} &> /dev/null
#   elif [[ ${file} == *.java ]]; then
#       git restore ${SCRIPT_DIR}/monitoring-engine/src/main/java/edu/lazymop/tinymop/monitoring/slicing/algod/${file} &> /dev/null
#   fi
#done

# rm -rf out

# mkdir out

# get the aspectj files that we need

# java -cp spec-parser/target/spec-parser-1.0-SNAPSHOT.jar edu.lazymop.tinymop.specparser.Main out three

# we need BaseAspect.aj

if [[ ${FOR_IMM} == "true" ]]; then
    filename="IMMBaseAspect.aj"
else
    filename="BaseAspect.aj"
fi

cp ./spec-parser/src/main/resources/${filename} out/BaseAspect.aj
cp ./spec-parser/src/main/resources/NoMonitoringLoop.java out/NoMonitoringLoop.java
cp ./spec-parser/src/main/resources/NoMonitoringSpec.java out/NoMonitoringSpec.java
if [[ ${ENABLE_ON_DEMAND_SYNC} != "false" ]]; then
    cp ./spec-parser/src/main/resources/ThreadAspect.aj out/ThreadAspect.aj
fi

#cp ./spec-parser/src/main/resources/TestNameAspect.aj out/TestNameAspect.aj
for spec in "${specs[@]}"; do
    spec=$(echo "${spec}" | cut -d '.' -f 1)
    echo "Adding BaseAspect for spec ${spec}"
    cp ./spec-parser/src/main/resources/${filename} out/${spec}BaseAspect.aj
    sed -i "s/BaseAspect/${spec}BaseAspect/g" out/${spec}BaseAspect.aj
    
    if [[ ${FOR_IMM} == "true" ]]; then
        cp ./spec-parser/src/main/resources/NoMonitoringSpec.java out/DoMonitoring${spec}.java
        sed -i "s/NoMonitoringSpec/DoMonitoring${spec}/g" out/DoMonitoring${spec}.java
    fi
done

# compile the aspectj files
rm -rf out/compiled
mkdir -p out/compiled
ajc -source 1.8 -target 1.8 -d out/compiled -cp $CLASSPATH:monitoring-engine/target/monitoring-engine-1.0-SNAPSHOT.jar:spec-parser/target/spec-parser-1.0-SNAPSHOT.jar:out/compiled -sourceroots out
if [[ $? -ne 0 ]]; then
    echo "UNABLE TO COMPILE ASPECT FILES"
    exit 1
fi

AGENT_PREP="agent-prep"
rm -rf ${AGENT_PREP}
mkdir ${AGENT_PREP}
cp monitoring-engine/target/monitoring-engine-1.0-SNAPSHOT.jar ${AGENT_PREP}
cp spec-parser/target/spec-parser-1.0-SNAPSHOT.jar ${AGENT_PREP}
cp ~/aspectj1.8/lib/aspectjrt.jar ${AGENT_PREP}
cp ~/aspectj1.8/lib/aspectjweaver.jar ${AGENT_PREP}

# 2. copy over compiled aspects

cp -r out/compiled/mop ${AGENT_PREP}

(
    cd ${AGENT_PREP}
    for i in $(ls *.jar); do
        # 3. extract the jar
        jar xvf ${i} &> /dev/null
        # 4. remove the original jar
        rm ${i}
    done
    if [[ -n $(find . -name "*.RSA") ]]; then
        find . -name "*.RSA" | xargs rm
    fi
    if [[ -n $(find . -name "*.SF") ]]; then
        find . -name "*.SF" | xargs rm
    fi
    mkdir -p META-INF
    # 5. Create aop-ajc.xml in the right location
    AOP_FILE=META-INF/aop-ajc.xml
    echo '<aspectj>' > ${AOP_FILE}
    echo '<aspects>' >> ${AOP_FILE}
    for i in $(ls mop | grep -v ^BaseAspect | grep -v NoMonitoring | grep -v DoMonitoring); do
        echo '<aspect name="mop.'$(echo ${i} | cut -d. -f1)'"/>' >> ${AOP_FILE}
    done
    echo '</aspects>' >> ${AOP_FILE}
    echo '<weaver options="-nowarn -Xlint:ignore"></weaver>' >> ${AOP_FILE} # optional: -verbose -showWeaveInfo
    echo '</aspectj>' >> ${AOP_FILE}

    # 6. Create MANIFEST.MF
    cat > META-INF/MANIFEST.MF<<EOF
Manifest-Version: 1.0
Implementation-Title: org.aspectj.weaver
Premain-Class: org.aspectj.weaver.loadtime.Agent
Implementation-Version: DEVELOPMENT
Specification-Vendor: aspectj.org
Can-Redefine-Classes: true
Name: org/aspectj/weaver/
Specification-Title: AspectJ Weaver Classes
Specification-Version: DEVELOPMENT
Created-By: 1.8.0_202 (Oracle Corporation)
Implementation-Vendor: aspectj.org
EOF

)

# 7. create the agent jar

jar cmf ${AGENT_PREP}/META-INF/MANIFEST.MF agent.jar -C ${AGENT_PREP} . &> /dev/null
if [[ $? -ne 0 ]]; then
    echo "UNABLE TO BUILD AGENT"
    exit 1
fi

# 8. delete the agent prep directory

rm -rf ${AGENT_PREP}

# java -cp spec-parser/target/spec-parser-1.0-SNAPSHOT.jar:monitoring-engine/target/monitoring-engine-1.0-SNAPSHOT.jar edu.lazymop.tinymop.monitoring.Main

# 9. patch aspectj
pushd ${SCRIPT_DIR}/experiments &> /dev/null
javac Factory.java
rm -rf org/aspectj/runtime/reflect &&
mkdir -p org/aspectj/runtime/reflect &&
cp Factory.class org/aspectj/runtime/reflect
zip ${SCRIPT_DIR}/agent.jar org/aspectj/runtime/reflect/Factory.class
rm -rf Factory.class org
popd &> /dev/null
