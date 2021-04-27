#基础镜像，如果本地没有，会从远程仓库拉取。
FROM openjdk:8-jdk-alpine

#镜像的制作人
MAINTAINER 56553655@163.com 

#工作目录
WORKDIR /data/hasentinel-dashboard

#在容器中创建挂载点，可以多个
VOLUME ["/data/hasentinel-dashboard","/data/hasentinel-dashboard/logs"]

#声明了容器应该打开的端口并没有实际上将它打开
EXPOSE 20440
EXPOSE 20441

#定义参数
ARG JAR_FILE

#拷贝本地文件到镜像中
COPY sentinel-dashboard.jar /data/hasentinel-dashboard/sentinel-dashboard.jar

#指定容器启动时要执行的命令，但如果存在CMD指令，CMD中的参数会被附加到ENTRYPOINT指令的后面
ENTRYPOINT ["java","-Dserver.port=20440","-Dcsp.sentinel.api.port=20441","-Dcsp.sentinel.dashboard.server=localhost:20440","-Xms1024m","-Xmx1024m","-Xmn256m","-XX:PermSize=128m","-Xss256k","-XX:+DisableExplicitGC","-XX:+UseConcMarkSweepGC","-XX:-UseGCOverheadLimit","-verbose:gc","-Xloggc:/data/hasentinel-dashboard/logs/jvm-gc.log","-XX:+PrintGCDetails","-XX:+HeapDumpOnOutOfMemoryError","-XX:HeapDumpPath=/data/hasentinel-dashboard/logs/sentinel-dashboare-dump.bin","-XX:+CMSParallelRemarkEnabled","-XX:LargePageSizeInBytes=128m","-XX:+UseCMSInitiatingOccupancyOnly","-XX:CMSInitiatingOccupancyFraction=70","-Djava.awt.headless=true","-Djava.net.preferIPv4Stack=true","-Dsentinel.dashboard.autoRemoveMachineMillis=60000","-Dproject.name=sentinel-dashboard","-jar","sentinel-dashboard.jar"]
