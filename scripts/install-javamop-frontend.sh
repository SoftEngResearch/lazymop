# first we install javaparser, which the javamop frontend depends on

echo "STAGE 1: install javaparser"
(
    cd /tmp
    git clone https://github.com/javaparser/javaparser.git
    (
        cd javaparser
        git checkout javaparser-parent-3.23.1
        sed -i 's/public final int hashCode/public int hashCode/' javaparser-core/src/main/java/com/github/javaparser/ast/Node.java
        mvn install -DskipTests -DskipITs
    )
    rm -rf /tmp/javaparser
)  &> gol-javaparser

grep "BUILD" gol-javaparser

# then we install the javamop frontend from our tracemop directory,
# which contains the most recent version of JavaMOP code

echo "STAGE 2: install javamop frontend"
(
    if [ ! -d "tracemop" ]; then
        git clone https://github.com/SoftEngResearch/tracemop
    fi

    echo "STAGE 2.1: install tracemop root"
    (
        cd tracemop
        git checkout 3de76a81606fce70e893ab305320b310a614438a
        mvn install -DskipTests -DskipITs -Dit.skip
    )&> gol-tracemop

    grep "BUILD" gol-tracemop
    echo "STAGE 2.1: DONE"
    echo "STAGE 2.2: install javamop jar"
    (
        cd tracemop/javamop
        git pull
        mvn package -DskipTests
        javamop_jar=$(find target -maxdepth 1 -name "javamop*.jar")
        mvn install:install-file -Dfile=${javamop_jar} -DgroupId="javamop" -DartifactId="javamop" -Dversion="5.0-SNAPSHOT" -Dpackaging="jar"
    )&> gol-javamop

    grep "BUILD" gol-javamop
    echo "STAGE 2.2: DONE"
    echo "STAGE 2.3: install rv-monitor jar"
    (
        cd tracemop/rv-monitor
        mvn package -DskipTests
        rvmonitor_jar=$(find target -maxdepth 1 -name "rv-monitor*.jar" | grep -v "tests")
        mvn install:install-file -Dfile=${rvmonitor_jar} -DgroupId="com.runtimeverification.rvmonitor" -DartifactId="rv-monitor" -Dversion="5.0-SNAPSHOT" -Dpackaging="jar"
    )&> gol-rvmonitor
)
grep "BUILD" gol-rvmonitor
echo "STAGE 2.3: DONE"
echo ================
cat gol-rvmonitor

echo "STAGE 3: install latest stateless4j"
(
    if [ ! -d "stateless4j" ]; then
        git clone https://github.com/stateless4j/stateless4j
    fi
    (
        cd stateless4j
        sed -i "s|<\/project.build.sourceEncoding>|<\/project.build.sourceEncoding><maven.compiler.source>8<\/maven.compiler.source><maven.compiler.target>8<\/maven.compiler.target>|" pom.xml
        sed -i -n '
/<release>11<\/release>/{n
        n
        x
        d
       }
x
1d
p
${x
  p
}
' pom.xml

        mvn clean -DskipTests -Djacoco.skip install
    )&> gol-stateless
)
grep "BUILD" gol-stateless
echo "STAGE 3: DONE"
echo ================
cat gol-stateless
