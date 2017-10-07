#!/bin/sh

CLASSPATH=./cy-bss-server.jar:./lib/cy-bss-core.jar:./lib/aopalliance-1.0.jar:./lib/commons-dbcp-1.4.jar:./lib/commons-logging-1.2.jar
CLASSPATH=$CLASSPATH:./lib/commons-pool-1.6.jar:./lib/logback-classic-1.1.2.jar:./lib/logback-core-1.1.2.jar:./lib/mysql-connector-java-5.1.34-bin.jar:./lib/slf4j-api-1.7.7.jar
CLASSPATH=$CLASSPATH:./lib/spring-aop-4.0.7.RELEASE.jar:./lib/spring-beans-4.0.7.RELEASE.jar:./lib/spring-boot-1.1.8.RELEASE.jar:./lib/spring-boot-autoconfigure-1.1.8.RELEASE.jar
CLASSPATH=$CLASSPATH:./lib/spring-context-4.0.7.RELEASE.jar:./lib/spring-core-4.0.7.RELEASE.jar:./lib/spring-expression-4.0.7.RELEASE.jar:./lib/spring-jdbc-4.0.7.RELEASE.jar
CLASSPATH=$CLASSPATH:./lib/spring-tx-4.0.7.RELEASE.jar:./lib/spring-web-4.0.7.RELEASE.jar

echo 'CLASSPATH='$CLASSPATH

java -classpath "$CLASSPATH" org.cysoft.bss.server.ServerLauncher server1
