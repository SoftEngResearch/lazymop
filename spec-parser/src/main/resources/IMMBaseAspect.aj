package mop;

public aspect BaseAspect {
  pointcut notwithin() :
  !within(sun..*) &&
  !within(java..*) &&
  !within(javax..*) &&
  !within(javafx..*) &&
  !within(com.sun..*) &&
  !within(org.dacapo.harness..*) &&
  !within(net.sf.cglib..*) &&
  !within(mop..*) &&
  !within(org.h2..*) &&
  !within(org.sqlite..*) &&
  !within(javamoprt..*) &&
  !within(rvmonitorrt..*) &&
  !within(org.junit..*) &&
  !within(junit..*) &&
  !within(java.lang.Object) &&
  !within(com.runtimeverification..*) &&
  !within(org.apache.maven.surefire..*) &&
  !within(org.mockito..*) &&
  !within(org.powermock..*) &&
  !within(org.easymock..*) &&
  !within(com.mockrunner..*) &&
  !within(edu.lazymop..*) &&
  !within(com.github.oxo42.stateless4j..*) &&
  !within(org.apache.maven..*) &&
  !within(org.testng..*) &&
  !within(org.jmock..*) &&
  !withincode(* *.IMM_*(..));
}
