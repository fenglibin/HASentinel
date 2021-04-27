#基础镜像，如果本地没有，会从远程仓库拉取。
FROM openjdk:8-jdk-alpine

#镜像的制作人
MAINTAINER 56553655@163.com 

#工作目录
WORKDIR /data/sharding-influxdb

#在容器中创建挂载点，可以多个
VOLUME ["/data/applogs/sharding-influxdb","/data/sharding-influxdb"]

#声明了容器应该打开的端口并没有实际上将它打开
EXPOSE 20430
EXPOSE 20431

#定义参数
ARG JAR_FILE

#拷贝本地文件到镜像中
COPY $JAR_FILE /data/sharding-influxdb/sharding-influxdb.jar

#指定容器启动时要执行的命令，但如果存在CMD指令，CMD中的参数会被附加到ENTRYPOINT指令的后面
ENTRYPOINT ["java","-Dcsp.sentinel.dashboard.server=sentinel.xxxxxx.com","-Xms512m","-Xmx1024m","-XX:+DisableExplicitGC","-XX:+UseConcMarkSweepGC","-XX:-UseGCOverheadLimit","-verbose:gc","-Xloggc:/data/applogs/sharding-influxdb/jvm-gc.log","-XX:+PrintGCDetails","-XX:+HeapDumpOnOutOfMemoryError","-XX:HeapDumpPath=/data/applogs/sharding-influxdb/sentinel-dashboare-dump.bin","-XX:+CMSParallelRemarkEnabled","-XX:LargePageSizeInBytes=128m","-XX:+UseCMSInitiatingOccupancyOnly","-XX:CMSInitiatingOccupancyFraction=70","-Djava.awt.headless=true","-Djava.net.preferIPv4Stack=true","-jar","sharding-influxdb.jar"]
