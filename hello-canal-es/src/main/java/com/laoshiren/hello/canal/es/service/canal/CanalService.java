package com.laoshiren.hello.canal.es.service.canal;

import com.alibaba.otter.canal.protocol.CanalEntry;
import com.laoshiren.hello.canal.es.domain.EntryDto;

import java.util.List;

/**
 * ProjectName:     hello-canal
 * Package:         com.laoshiren.hello.canal.es.canal
 * ClassName:       CanalService
 * Author:          laoshiren
 * Git:             xiangdehua@pharmakeyring.com
 * Description:
 * Date:            2020/11/6 16:13
 * Version:         1.0.0
 */
public interface CanalService {

    /**
     * List<Entry> 转换成对象
     *
     * @param entries entry
     * @return List EntryDto
     */
    List<EntryDto> mappingEntry(List<CanalEntry.Entry> entries);

    /**
     * 交给es处理
     * @param list entryList
     */
    void elasticHandler(List<EntryDto> list);
}
