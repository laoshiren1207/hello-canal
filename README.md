# Canal 

## 1. 什么是Canal

**canal [kə'næl]**，译意为水道/管道/沟渠，主要用途是基于`MySQL`数据库增量日志解析，提供增量数据订阅和消费。

![](https://laoshiren.oss-cn-shanghai.aliyuncs.com/3d98ebff-4f20-48f7-962a-04593b53f1e1.png)

基于日志增量订阅和消费的业务包括

- 数据库镜像
- 数据库实时备份
- 索引构建和实时维护(拆分异构索引、倒排索引等)
- 业务`cache`刷新
- 带业务逻辑的增量数据处理

当前的`canal`支持源端`MySQL`版本包括 5.1.x , 5.5.x , 5.6.x , 5.7.x , 8.0.x

## 2. 为什么使用Canal

![](https://laoshiren.oss-cn-shanghai.aliyuncs.com/4405a508-e84f-497f-b259-7c61a518791b.jpg)

### 2.1 MySQL主备复制原理

- MySQL master 将数据变更写入二进制日志( binary log, 其中记录叫做二进制日志事件binary log events，可以通过 show binlog events 进行查看)
- MySQL slave 将 master 的 binary log events 拷贝到它的中继日志(relay log)
- MySQL slave 重放 relay log 中事件，将数据变更反映它自己的数据

### 2.2 canal 工作原理

- canal 模拟 MySQL slave 的交互协议，伪装自己为 MySQL slave ，向 MySQL master 发送dump 协议
- MySQL master 收到 dump 请求，开始推送 binary log 给 slave (即 canal )
- canal 解析 binary log 对象(原始为 byte 流)

### 2.3 使用Canal的场景

由第一张图可知，我们可以向非`Mysql`的数据库进行数据的同步，比如增量同步到`ES`，`Redis`，`Oracle`等任意地方。这样，我们就不需要自己手动同步到其他数据库里。

##  3. 如何使用Canal

### 3.1 Canal 准备 -- 数据源



### 3.2 Canal准备 -- 服务端

### 3.3 Canal业务 -- Java

### 3.4 Canal业务 -- SpringBoot

## 4 测试