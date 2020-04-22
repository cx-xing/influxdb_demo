package com.muscleape.influxdb;

import com.muscleape.influxdb.entity.CodeInfo;
import com.muscleape.influxdb.entity.LogInfo;
import junit.framework.Assert;
import org.influxdb.dto.Point;
import org.influxdb.dto.QueryResult;
import org.influxdb.dto.QueryResult.Result;
import org.influxdb.dto.QueryResult.Series;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.BeanWrapperImpl;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author C__xing
 * @version 1.0
 * @date 2020/4/22
 */
public class InfluxDBPOJOTest {

    private InfluxDBConnectTest influxDB;
    private String username = "admin";//用户名
    private String password = "admin";//密码
    private String openurl = "http://192.168.151.30:8086";//连接地址
    private String database = "netscoutpoc";//数据库
    private String measurement = "sys_code";

    @Before
    public void setUp() {
        //创建 连接
        influxDB = new InfluxDBConnectTest(username, password, openurl, database);

        influxDB.influxDbBuild();

        influxDB.createRetentionPolicy();

//        influxDB.deleteDB(database);
//        influxDB.createDB(database);
    }

    @Test
    public void testInsert() {//测试数据插入
        Map<String, String> tags = new HashMap<String, String>();
        Map<String, Object> fields = new HashMap<String, Object>();
        List<CodeInfo> list = new ArrayList<CodeInfo>();

        CodeInfo info1 = new CodeInfo();
        info1.setId(1L);
        info1.setName("BANKS");
        info1.setCode("ABC");
        info1.setDescr("中国农业银行");
        info1.setDescrE("ABC");
        info1.setCreatedBy("system");
        info1.setCreatedAt(new Date().getTime());

        CodeInfo info2 = new CodeInfo();
        info2.setId(2L);
        info2.setName("BANKS");
        info2.setCode("CCB");
        info2.setDescr("中国建设银行");
        info2.setDescrE("CCB");
        info2.setCreatedBy("system");
        info2.setCreatedAt(new Date().getTime());

        list.add(info1);
        list.add(info2);

        for (CodeInfo info : list) {

            tags.put("TAG_CODE", info.getCode());
            tags.put("TAG_NAME", info.getName());

            fields.put("ID", info.getId());
            fields.put("NAME", info.getName());
            fields.put("CODE", info.getCode());
            fields.put("DESCR", info.getDescr());
            fields.put("DESCR_E", info.getDescrE());
            fields.put("CREATED_BY", info.getCreatedBy());
            fields.put("CREATED_AT", info.getCreatedAt());

            influxDB.insert(measurement, tags, fields);
        }
    }

    @Test
    public void testInsertPojo() {
        LogInfo logInfo = LogInfo.builder()
                .level("初级")
                .module("1")
                .deviceId("aaaaaaaaaaaaaaaaaaa")
                .msg("成功")
                .build();
        Point point = Point.measurementByPOJO(logInfo.getClass())
                .addFieldsFromPOJO(logInfo)
                .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                .build();
        // 出于业务考量,设备可以设置不同的保存策略(策略名为固定前缀+设备ID)
//        influxDB.write(influxDBProperties.getDatabase(), influxDBProperties.getRetentionPolicy(), point);
        influxDB.insertPOJO(point);
    }

    @Test
    public void testQuery() {//测试数据查询
        String command = "select * from sys_code";
        QueryResult results = influxDB.query(command);

        if (results.getResults() == null) {
            return;
        }
        List<CodeInfo> lists = new ArrayList<CodeInfo>();
        for (Result result : results.getResults()) {

            List<Series> series = result.getSeries();
            for (Series serie : series) {
//                Map<String, String> tags = serie.getTags();
                List<List<Object>> values = serie.getValues();
                List<String> columns = serie.getColumns();

                lists.addAll(getQueryData(columns, values));
            }
        }

        Assert.assertTrue((!lists.isEmpty()));
        Assert.assertEquals(2, lists.size());
    }

    @Test
    public void testQueryWhere() {//tag 列名 区分大小写
        String command = "select * from sys_code where TAG_CODE='ABC'";
        QueryResult results = influxDB.query(command);

        if (results.getResults() == null) {
            return;
        }
        List<CodeInfo> lists = new ArrayList<CodeInfo>();
        for (Result result : results.getResults()) {

            List<Series> series = result.getSeries();
            for (Series serie : series) {
                List<List<Object>> values = serie.getValues();
                List<String> columns = serie.getColumns();

                lists.addAll(getQueryData(columns, values));
            }
        }

        Assert.assertTrue((!lists.isEmpty()));
        Assert.assertEquals(1, lists.size());

        CodeInfo info = lists.get(0);

        Assert.assertEquals(info.getCode(), "ABC");

    }

    @Test
    public void deletMeasurementData() {
        String command = "delete from sys_code where TAG_CODE='ABC'";
        String err = influxDB.deleteMeasurementData(command);
        Assert.assertNull(err);
    }

    /***整理列名、行数据***/
    private List<CodeInfo> getQueryData(List<String> columns, List<List<Object>> values) {
        List<CodeInfo> lists = new ArrayList<CodeInfo>();

        for (List<Object> list : values) {
            CodeInfo info = new CodeInfo();
            BeanWrapperImpl bean = new BeanWrapperImpl(info);
            for (int i = 0; i < list.size(); i++) {

                String propertyName = setColumns(columns.get(i));//字段名
                Object value = list.get(i);//相应字段值
                bean.setPropertyValue(propertyName, value);
            }

            lists.add(info);
        }

        return lists;
    }

    /***转义字段***/
    private String setColumns(String column) {
        String[] cols = column.split("_");
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < cols.length; i++) {
            String col = cols[i].toLowerCase();
            if (i != 0) {
                String start = col.substring(0, 1).toUpperCase();
                String end = col.substring(1).toLowerCase();
                col = start + end;
            }
            sb.append(col);
        }
        return sb.toString();
    }
}

