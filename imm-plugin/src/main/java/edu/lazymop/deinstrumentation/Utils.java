// CHECKSTYLE:OFF
package edu.lazymop.deinstrumentation;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.lazymop.FileUtil;
import org.objectweb.asm.Type;

public class Utils {
    public static String convertAsmSignatureToJava(String desc) {
        StringBuilder javaSignature = new StringBuilder();
        Type[] argumentTypes = Type.getArgumentTypes(desc);
        String returnType = Type.getReturnType(desc).getClassName();
        String[] temps = returnType.split("\\.");
        javaSignature.append(temps[temps.length - 1]);

        javaSignature.append("(");
        for (int i = 0; i < argumentTypes.length; i++) {
            String temp = argumentTypes[i].getClassName();
            temps = temp.split("\\.");
            javaSignature.append(temps[temps.length - 1]);
            if (i < argumentTypes.length - 1) {
                javaSignature.append(",");
            }
        }
        javaSignature.append(")");
        return javaSignature.toString();
    }

    public static String getSpecFile(String specName, List<String> methodsToExclude, boolean supportDeLoop,
                                     boolean supportDeSpec) {
        // convert com.abc.util.Foo$Bar.baz to withincode(* com.abc.util.Foo.Bar.IMM_baz_*(..))
        /*
        List<String> pointcuts = new ArrayList<>();
        if (methodsToExclude != null) {
            for (String excludedMethod : methodsToExclude) {
                String excludedMethodWithoutLine = excludedMethod.substring(0, excludedMethod.indexOf(':'));
                int dotIndex = excludedMethodWithoutLine.lastIndexOf('.');
                String fullyQP = excludedMethodWithoutLine.substring(0, dotIndex);
                String method = excludedMethodWithoutLine.substring(dotIndex + 1);
                pointcuts.add("withincode(* " + fullyQP + ".IMM_" + method + "_*(..))");
            }
        }
         */

        return "package mop;\n" + "\n"
                + "public aspect " + specName + "BaseAspect {\n"
                + "  pointcut notwithin() :\n"
                + "  !within(sun..*) &&\n"
                + "  !within(java..*) &&\n"
                + "  !within(javax..*) &&\n"
                + "  !within(javafx..*) &&\n"
                + "  !within(com.sun..*) &&\n"
                + "  !within(org.dacapo.harness..*) &&\n"
                + "  !within(net.sf.cglib..*) &&\n"
                + "  !within(mop..*) &&\n"
                + "  !within(org.h2..*) &&\n"
                + "  !within(org.sqlite..*) &&\n"
                + "  !within(javamoprt..*) &&\n"
                + "  !within(rvmonitorrt..*) &&\n"
                + "  !within(org.junit..*) &&\n"
                + "  !within(junit..*) &&\n"
                + "  !within(java.lang.Object) &&\n"
                + "  !within(com.runtimeverification..*) &&\n"
                + "  !within(org.apache.maven.surefire..*) &&\n"
                + "  !within(org.mockito..*) &&\n"
                + "  !within(org.powermock..*) &&\n"
                + "  !within(org.easymock..*) &&\n"
                + "  !within(com.mockrunner..*) &&\n"
                + "  !within(edu.lazymop..*) &&\n"
                + "  !within(com.github.oxo42.stateless4j..*) &&\n"
                + "  !within(org.apache.maven..*) &&\n"
                + "  !within(org.testng..*) &&\n"
                + "  !within(org.jmock..*) &&\n"
                + (supportDeLoop ? "  !withincode(@mop.NoMonitoringLoop * *(..)) &&\n" : "")
                + (supportDeSpec ? "  !withincode(@mop.NoMonitoringSpec * *(..)) &&\n" : "")
//                + (pointcuts.isEmpty() ? "!withincode(* *.IMM_*(..));" : "(!withincode(* *.IMM_*(..)) || "
//                + String.join(" || ", pointcuts) + ");\n")
                + (methodsToExclude == null ? "  !withincode(* *.IMM_*(..));\n" : "  (!withincode(* *.IMM_*(..)) || "
                + "withincode(@mop.DoMonitoring" + specName + " * *(..)));")
                + "}\n";
    }

    public static boolean compileBaseAspect(List<String> ajFiles) {
        List<String> ajcCommand = new ArrayList<>();
        ajcCommand.add("ajc");
        ajcCommand.addAll(ajFiles);

        ProcessBuilder builder = new ProcessBuilder(ajcCommand);
        builder.redirectErrorStream(true);

        List<String> output = new ArrayList<>();

        try {
            Process process = builder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                output.add(line);
            }

//            System.out.println(output);
            return process.waitFor() == 0;
        } catch (Exception exception) {
            exception.printStackTrace();
            return false;
        }
    }

    public static boolean updateBaseAspectInJar(File agentFile, String targetDirectory, Set<String> specNames) {
        Map<String, String> env = new HashMap<>();
        env.put("create", "true");
        URI jarFile = URI.create("jar:file:" + agentFile);
        try (FileSystem jarFS = FileSystems.newFileSystem(jarFile, env)) {
            for (String spec : specNames) {
                Path newFile = Paths.get(targetDirectory + File.separator + spec + "BaseAspect.class");
                Path pathInJarFile = jarFS.getPath("mop" + File.separator + spec + "BaseAspect.class");
                FileUtil.copy(newFile, pathInJarFile, false);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }
}
