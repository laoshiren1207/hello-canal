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
 * Git:             xiangdehua@pharmakeyring.com
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
