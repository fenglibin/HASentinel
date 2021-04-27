#java -Dserver.port=8080 -Dcsp.sentinel.dashboard.server=localhost:8080 -Dproject.name=sentinel-dashboard -Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=8000 -jar target/sentinel-dashboard.jar
#应用失联检测参数，当应用失联的时间大于配置的值（单位为毫秒，最小值为300000），会自动从控制台中将该应用去掉
#-Dsentinel.dashboard.autoRemoveMachineMillis=300000
java -Dserver.port=20440  -Dcsp.sentinel.dashboard.server=localhost:20440 -Dproject.name=sentinel-dashboard -Dsentinel.dashboard.autoRemoveMachineMillis=60000 -Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=8000 -jar target/sentinel-dashboard.jar
