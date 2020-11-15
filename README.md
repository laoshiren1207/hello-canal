

# 微服务解决方案 Canal 

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

### 3.1 Canal 准备 -- 数据库

首先准备一个`mysql`数据库，并且在 `mysqld`添加如下

~~~ini
[mysqld]
log-bin=D:\mysql-5.7.32\mysql-bin
binlog-format=ROW
server-id=123454
~~~
或者使用`docker`的方式，不过得修改容器内文件`/etc/mysql/mysql.conf.d/mysqld.cnf`

~~~shell
## 进入容器
docker exec -it mysql-canal /bin/bash 
## 添加如下
log-bin=/var/lib/mysql/mysql-bin
binlog-format=ROW
server-id=123454
## 重启容器
docker restart mysql-canal
~~~

~~~sql
show variables like 'log_%';
~~~

![](https://laoshiren.oss-cn-shanghai.aliyuncs.com/f317cdf0-90e5-4df3-a5f9-01aaae515f45.png)

~~~sql
show binary logs;
~~~

![](https://laoshiren.oss-cn-shanghai.aliyuncs.com/1ec0327d-1a38-4dee-a860-d8ab547952ab.png)

### 3.2 Canal准备 -- 服务端

下载`canal`服务端`https://github.com/alibaba/canal/releases`

修改配置文件`conf/example/instance.properties`

~~~properties
# 主数据库地址
canal.instance.master.address = 127.0.0.1:3306
# mysql binary log
canal.instance.master.journal.name = mysql-bin.000001
# 偏移量 show BINARY logs;
canal.instance.master.position = 154

# username/password
# 在MySQL服务器授权的账号密码
canal.instance.dbUsername = root
canal.instance.dbPassword = 123456

# table regex
# 监听所有表，也可以指定表用,分割
canal.instance.filter.regex = .*\\..*
~~~

修改 `conf/canal.properties`

~~~properties

~~~

### 3.3 Canal业务 -- Java

在`GitHub`上有他的`Example`示例这里不多赘述，[点击此处跳转示例](https://github.com/alibaba/canal/wiki/ClientExample)。

### 3.4 Canal业务 -- SpringBoot

这里是我本人写的一个示例，将`mysql`的数据同步到`ES`上（本来中间应该加一层`MQ` ，但是自己的阿里云服务器内存不够用了，所以省略）。

~~~shell
+--------+       +--------+        +--------+        +----------+
|  mysql |  ---> | Canal  |  --->  |   MQ   |  --->  | es/redis |
+--------+       +--------+        +--------+        +----------+
~~~

第一部搭建一个`ES`，可以参考我之前的博客[微服务解决方案 -- 高效搜索 Elastic Search 7.6.2 (上)](https://blog.csdn.net/weixin_42126468/article/details/107288069)，里面有用`docker`的方式搭建一个`elastic search`。

然后引入依赖，分别是`Canal`的依赖和`ES`的依赖

~~~xml
<!-- ali canal -->
<properties
    <ali.canal.version>1.1.4</ali.canal.version>
	<elasticsearch.version>7.6.2</elasticsearch.version>
</properties>
<!-- alibaba canal -->
<dependency>
    <groupId>com.alibaba.otter</groupId>
    <artifactId>canal.client</artifactId>
    <version>${ali.canal.version}</version>
</dependency>

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-elasticsearch</artifactId>
</dependency>

<dependency>
    <groupId>org.elasticsearch.client</groupId>
    <artifactId>elasticsearch-rest-high-level-client</artifactId>
</dependency>
~~~

配置文件

~~~yaml
spring:
  application:
    name: canal-example

server:
  port: 19000
## canal 配置
canal:
  hostname: 127.0.0.1
  port: 11111
  destination: example
## es 配置
es:
  hostname: 127.0.0.1
  port: 9200
  scheme: http
~~~

配置2个配置类

~~~java
package com.laoshiren.hello.canal.es.configure;

import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.client.impl.SimpleCanalConnector;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetSocketAddress;

/**
 * ProjectName:     hello-canal
 * Package:         com.laoshiren.hello.canal.es.configure
 * ClassName:       CanalConfiguration
 * Author:          laoshiren
 * Git:             15207034473@163.com
 * Description:
 * Date:            2020/10/21 14:05
 * Version:         1.0.0
 */
@Configuration
@Slf4j
public class CanalConfiguration {

    /**
     * canal 服务地址
     */
    @Value(value = "${canal.hostname}")
    private String hostName;

    /**
     * canal 端口
     */
    @Value(value = "${canal.port}")
    private Integer port;

    /**
     * canal 目标
     */
    @Value(value = "${canal.destination}")
    private String destination;

    /**
     * canal 连接器
     * @return  canalConnector
     */
    @Bean("canalConnector")
    public CanalConnector initCanalConnector(){
        log.info("-- canal init --");
        InetSocketAddress address = new InetSocketAddress(hostName, port);
        // canalConnector
        log.info("-- canal params -- {} -- {} --",hostName,port);
        SimpleCanalConnector canalConnector = new SimpleCanalConnector(address, "", "", destination);
        canalConnector.setSoTimeout(60 * 1000);
        canalConnector.setIdleTimeout(-1);
        log.info("-- canal finish --");
        return canalConnector;
    }

}
~~~

~~~java
package com.laoshiren.hello.canal.es.configure;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ProjectName:     hello-canal
 * Package:         com.laoshiren.hello.canal.es.configure
 * ClassName:       ElasticSearchClientConfiguration
 * Author:          laoshiren
 * Git:             15207034473@163.com
 * Description:
 * Date:            2020/10/23 14:09
 * Version:         1.0.0
 */
@Configuration
@Slf4j
public class ElasticSearchClientConfiguration {

    @Value("${es.hostname}")
    private String hostname;
    @Value("${es.port}")
    private int port;
    @Value("${es.scheme}")
    private String scheme;

    @Bean
    public RestHighLevelClient restHighLevelClient(){
        log.info("-- es init --");
        log.info("-- es params -- {} -- {} -- {} --",hostname,port,scheme);
        log.info("-- es finish --");
        return new RestHighLevelClient(
                RestClient.builder(new HttpHost(hostname,port,scheme))
                        .setRequestConfigCallback(requestConfigBuilder -> {
                            requestConfigBuilder.setConnectTimeout(-1);
                            requestConfigBuilder.setSocketTimeout(30000);
                            requestConfigBuilder.setConnectionRequestTimeout(30000);
                            return requestConfigBuilder;
                        })
        );
    }
}
~~~

设计同步的对象

~~~java
package com.laoshiren.hello.canal.es.domain;

import com.alibaba.otter.canal.protocol.CanalEntry;
import com.google.protobuf.InvalidProtocolBufferException;
import com.laoshiren.hello.canal.common.utils.ColumnToPropertyUtils;
import com.laoshiren.hello.canal.common.utils.JsonUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ProjectName:     hello-canal
 * Package:         com.laoshiren.hello.canal.es.domain
 * ClassName:       EntryDto
 * Author:          laoshiren
 * Git:             15207034473@163.com
 * Description:
 * Date:            2020/10/21 14:32
 * Version:         1.0.0
 */
@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class EntryDto {

    /**
     * id
     */
    private String id;

    /**
     * 数据库名
     */
    private String schemaName;

    /**
     * 表明
     */
    private String tableName;

    /**
     * 操作类型
     */
    private CanalEntry.EventType eventType;

    /**
     * 实际数据
     */
    private String data;

    /**
     * entry 2 Object
     * @param entry CanalEntry.Entry
     */
    public EntryDto(CanalEntry.Entry entry){
        // 操作数据库名
        this.schemaName = entry.getHeader().getSchemaName();
        // 操作表名
        this.tableName = entry.getHeader().getTableName();
        try {
            CanalEntry.RowChange rowChange = CanalEntry.RowChange.parseFrom(entry.getStoreValue());
            // 操作类型
            CanalEntry.EventType eventType = rowChange.getEventType();
            this.eventType = eventType;
            // 实际数据
            List<CanalEntry.RowData> rowDataList = rowChange.getRowDatasList();
            Map<String,Object> dataMap = new HashMap<>();
            for (CanalEntry.RowData rowData : rowDataList) {
                // 获取数据
                List<CanalEntry.Column> columns = rowData.getAfterColumnsList();
                if (eventType.equals(CanalEntry.EventType.DELETE)) {
                    // 如果是删除获取之前的数据
                    columns = rowData.getBeforeColumnsList();
                }
                for (CanalEntry.Column column : columns) {
                    // 主键
                    if (column.getIsKey()) {
                        this.id = column.getValue();
                    }
                    // 转换json
                    dataMap.put(ColumnToPropertyUtils.columnToProperty2(column.getName()),column.getValue());
                }
            }
            this.data = JsonUtils.obj2json(dataMap);
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    /**
     * 索引名
     * @return  String  dbName_tableName
     */
    public String getIndexName(){
        return this.schemaName +
                "_" +
                this.tableName;
    }

}
~~~

`Canal`操作

~~~java
/**
 * List<Entry> 转换成对象
 *
 * @param entries entry
 * @return List EntryDto
 */
public List<EntryDto> mappingEntry(List<CanalEntry.Entry> entries) {
    return entries.stream()
            .filter(it -> {
                //开启/关闭事务的实体类型，跳过
                return it.getEntryType() != CanalEntry.EntryType.TRANSACTIONBEGIN &&
                        it.getEntryType() != CanalEntry.EntryType.TRANSACTIONEND;
            })
            .map(EntryDto::new)
            .collect(Collectors.toList());
}

/**
 * 交给es处理
 * @param list entryList
 */
public void elasticHandler(List<EntryDto> list){
    list.forEach( it-> elasticService.documentHandler(it));
}
~~~

`ES`操作

~~~java
@Resource
private RestHighLevelClient restHighLevelClient;

@Override
public void documentHandler(EntryDto entryDto) {
    log.info("---- index ---- {} ----- {}",entryDto.getIndexName(), JsonUtils.obj2json(entryDto));
    switch (entryDto.getEventType()) {
        case UPDATE: documentUpdate(entryDto);break;
        case INSERT: documentCreate(entryDto);break;
        case DELETE: documentDelete(entryDto);break;
        default: break;
    }
}

// 部分代码省略
~~~

定时任务去向`Canal`的服务拉去数据

~~~java
boolean initFlag = false;

@Scheduled(cron = "*/2 * * * * ?")
public void canalHandler(){
    boolean init = init();
    if (init) {
        Message message = connector.getWithoutAck(100);
        long batchId = message.getId();
        int size = message.getEntries().size();
        if (batchId == -1 || size == 0) {
            log.info("listen ...... ");
        } else {
            // 转换成EntryDto ，上面设计好的数据对象 
            List<EntryDto> list = canalService.mappingEntry(message.getEntries());
            // 发送到ES上
            canalService.elasticHandler(list);
        }
        connector.ack(batchId); // 提交确认
    }

}

private boolean init(){
    if (!initFlag) {
        connector.connect();
        connector.subscribe(".*\\..*");
        connector.rollback();
        initFlag = !initFlag;
        log.info(" --- init --- init canal job finished");
    }
    return initFlag;
}
~~~

当然最好是中间有一层`MQ`让这个客户端一个一个消费。

## 4 测试

插入一个数据`ES`上就可以看到数据了。


![](https://laoshiren.oss-cn-shanghai.aliyuncs.com/223101ae-639c-449b-bb4a-9163078c631e.png)

![](https://laoshiren.oss-cn-shanghai.aliyuncs.com/55d96b8c-8da9-44a8-b2f9-891eec161fdb.png)

![](https://laoshiren.oss-cn-shanghai.aliyuncs.com/5b153fbb-f537-4448-9ba6-69f0840de744.png)

