java -Dserver.port=10000 -Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=8000 -jar target/hasentinel-common-sample-1.0.0-SNAPSHOT.jar
