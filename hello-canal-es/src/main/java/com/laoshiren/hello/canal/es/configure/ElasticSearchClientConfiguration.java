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
 * Git:             xiangdehua@pharmakeyring.com
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
