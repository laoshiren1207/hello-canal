package com.laoshiren.hello.canal.es.service.elastic;

import com.laoshiren.hello.canal.es.domain.EntryDto;

/**
 * ProjectName:     hello-canal
 * Package:         com.laoshiren.hello.canal.es.service
 * ClassName:       ElasticService
 * Author:          laoshiren
 * Git:             xiangdehua@pharmakeyring.com
 * Description:
 * Date:            2020/11/6 13:24
 * Version:         1.0.0
 */
public interface ElasticService {

    /**
     *
     * @param entryDto entry
     */
    void documentHandler(EntryDto entryDto);

}
