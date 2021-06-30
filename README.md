HASentinel是基于阿里[Sentinel](http://https://github.com/alibaba/Sentinel)做的高可用、高可扩展改造的项目，主要改造点：

１、将配置改造为存储到Zookeeper中；

２、将应用上报的Metrics持久化存储到Influxdb集群中；

３、Metrics由Sentinel控制台主动获取，修改为由应用端主动上报；

４、优化Sentinel控制台的操作，将原来只能够针对应用的各个节点的配置操作，优化为针对应用本身的配置操作，然后应用到应用集群的所有节点；

５、增加Grafana做为报表展示，支持展示长时间的Metrics报表，并增加支持自动Influxdb集群数据路由的中间层ShardingInfluxdb；

HASentinel的架构图

![输入图片说明](https://images.gitee.com/uploads/images/2021/0624/154346_bd406341_306225.png "Sentinel限流设计_New (2).png")

文档比较多还没有搬过来，详细架构文档及使用文档参看链接：[https://note.youdao.com/s/ZlizPHkV](http://)
