package com.laoshiren.hello.canal.es.service.canal.impl;

import com.alibaba.otter.canal.protocol.CanalEntry;
import com.laoshiren.hello.canal.es.service.canal.CanalService;
import com.laoshiren.hello.canal.es.domain.EntryDto;
import com.laoshiren.hello.canal.es.service.elastic.ElasticService;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

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
@Service("canalService")
@Slf4j
public class CanalServiceImpl implements CanalService {

    @Resource (name = "elasticService")
    private ElasticService elasticService;

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


}
