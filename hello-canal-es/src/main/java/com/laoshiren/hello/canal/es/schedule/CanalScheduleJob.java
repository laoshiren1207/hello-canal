package com.laoshiren.hello.canal.es.schedule;

import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.protocol.Message;
import com.laoshiren.hello.canal.es.service.canal.CanalService;
import com.laoshiren.hello.canal.es.domain.EntryDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * ProjectName:     hello-canal
 * Package:         com.laoshiren.hello.canal.es.schedule
 * ClassName:       CanalJob
 * Author:          laoshiren
 * Git:             xiangdehua@pharmakeyring.com
 * Description:
 * Date:            2020/11/6 16:10
 * Version:         1.0.0
 */
@Component
@Slf4j
public class CanalScheduleJob {

    @Resource(name = "canalConnector")
    private CanalConnector connector;
    @Resource
    private CanalService canalService;

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
                List<EntryDto> list = canalService.mappingEntry(message.getEntries());
                // 发送到MQ上 此步省略
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
}
