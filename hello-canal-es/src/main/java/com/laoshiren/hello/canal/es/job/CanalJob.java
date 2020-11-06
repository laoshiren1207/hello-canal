package com.laoshiren.hello.canal.es.job;

import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.alibaba.otter.canal.protocol.Message;
import com.laoshiren.hello.canal.es.domain.EntryDto;
import com.laoshiren.hello.canal.es.service.ElasticService;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ProjectName:     hello-canal
 * Package:         com.laoshiren.hello.canal.es.job
 * ClassName:       CanalJob
 * Author:          laoshiren
 * Git:             xiangdehua@pharmakeyring.com
 * Description:
 * Date:            2020/10/21 14:17
 * Version:         1.0.0
 */
@Component
@Slf4j
public class CanalJob implements InitializingBean {

    @Resource(name = "canalConnector")
    private CanalConnector connector;
    @Resource (name = "elasticService")
    private ElasticService elasticService;

    @Override
    public void afterPropertiesSet() {
        log.info("listen  .....");
        connector.connect();
        connector.subscribe(".*\\..*");
        connector.rollback();
        int i = 0;
        while (true) {
            // 获取指定数量的数据
            Message message = connector.getWithoutAck(100);
            long batchId = message.getId();
            int size = message.getEntries().size();
            if (batchId == -1 || size == 0) {
                if (i >= 10) {
                    log.info("listen ......");
                    i = 0;
                }
                try {
                    i++;
                    Thread.sleep(1000);
                } catch (InterruptedException ignored) {
                }
            } else {
                List<EntryDto> list = mappingEntry(message.getEntries());
                // 发送到MQ上 此步省略
                // 发送到ES上

            }
            connector.ack(batchId); // 提交确认
            // connector.rollback(batchId); // 处理失败, 回滚数据
        }
    }

    /**
     * List<Entry> 转换成对象
     *
     * @param entries entry
     * @return List EntryDto
     */
    private List<EntryDto> mappingEntry(List<CanalEntry.Entry> entries) {
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
    private void elasticHandler(List<EntryDto> list){
        list.forEach( it-> elasticService.documentHandler(it));
    }


}
