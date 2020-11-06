package com.laoshiren.hello.canal.es.domain;

import com.alibaba.otter.canal.protocol.CanalEntry;
import com.google.protobuf.InvalidProtocolBufferException;
import com.laoshiren.hello.canal.common.utils.ColumnToPropertyUtils;
import com.laoshiren.hello.canal.common.utils.JsonUtils;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ProjectName:     hello-canal
 * Package:         com.laoshiren.hello.canal.es.domain
 * ClassName:       EntryDto
 * Author:          laoshiren
 * Git:             xiangdehua@pharmakeyring.com
 * Description:
 * Date:            2020/10/21 14:32
 * Version:         1.0.0
 */
@Data
@Accessors(chain = true)
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

    public EntryDto(){

    }

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
        StringBuilder builder = new StringBuilder();
        return builder
                .append(this.schemaName)
                .append("_")
                .append(this.tableName)
                .toString();
    }

}
