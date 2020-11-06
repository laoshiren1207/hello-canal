package com.laoshiren.hello.canal.es.service.elastic.impl;

import com.laoshiren.hello.canal.common.utils.JsonUtils;
import com.laoshiren.hello.canal.es.domain.EntryDto;
import com.laoshiren.hello.canal.es.service.elastic.ElasticService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;

/**
 * ProjectName:     hello-canal
 * Package:         com.laoshiren.hello.canal.es.service.impl
 * ClassName:       ElasticServiceImpl
 * Author:          laoshiren
 * Git:             xiangdehua@pharmakeyring.com
 * Description:
 * Date:            2020/11/6 13:24
 * Version:         1.0.0
 */
@Service(value = "elasticService")
@Slf4j
public class ElasticServiceImpl implements ElasticService {

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

    /**
     * 创建文档
     */
    private void documentCreate(EntryDto entryDto){
        try {
            indexCreate(entryDto);
            boolean exist = documentExist(entryDto.getIndexName(), entryDto.getId());
            // 不存在插入
            if (!exist) {
                IndexRequest request = new IndexRequest(entryDto.getIndexName());
                request.id(entryDto.getId())
                        .timeout(TimeValue.timeValueSeconds(5));
                // 对象转换json
                request.source(entryDto.getData(), XContentType.JSON);
                restHighLevelClient.index(request, RequestOptions.DEFAULT);
            } else {
                // 更新文档
                documentUpdate(entryDto);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 删除文档
     */
    private void documentDelete(EntryDto entryDto){
        try {
            indexCreate(entryDto);
            boolean exist = documentExist(entryDto.getIndexName(), entryDto.getId());
            if (exist) {
                DeleteRequest request = new DeleteRequest(entryDto.getIndexName(),entryDto.getId());
                request.timeout(TimeValue.timeValueSeconds(5));
                restHighLevelClient.delete(request,RequestOptions.DEFAULT);
            }
        } catch (IOException e) { e.printStackTrace();}
    }

    /**
     * 更新文档
     */
    private void documentUpdate(EntryDto entryDto){
        try {
            UpdateRequest request = new UpdateRequest(entryDto.getIndexName(),entryDto.getId());
            request.timeout(TimeValue.timeValueSeconds(5));
            request.doc(entryDto.getData(),XContentType.JSON);
            restHighLevelClient.update(request, RequestOptions.DEFAULT);
        } catch (IOException ignore) {}
    }

    /**
     * 创建索引
     */
    private void indexCreate(EntryDto entryDto) throws IOException{
        boolean exist = indexExist(entryDto.getIndexName());
        if (! exist) {
            CreateIndexRequest request = new CreateIndexRequest(entryDto.getIndexName());
            restHighLevelClient.indices()
                    .create(request, RequestOptions.DEFAULT);
        }
    }

    /**
     * 查看文档存不存在
     *
     * @return  boolean
     */
    private boolean documentExist(String indexName,String id) throws IOException{
        GetRequest request = new GetRequest(indexName,id);
        return restHighLevelClient.exists(request, RequestOptions.DEFAULT);
    }

    /**
     * 查看文档存不存在
     *
     * @return  boolean
     */
    private boolean indexExist(String index) throws IOException{
        GetIndexRequest request = new GetIndexRequest(index);
        return restHighLevelClient.indices()
                .exists(request, RequestOptions.DEFAULT);
    }
}
