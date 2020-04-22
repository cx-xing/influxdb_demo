package com.muscleape.influxdb;

import com.muscleape.influxdb.entity.LogInfo;
import com.muscleape.influxdb.entity.Page;
import org.influxdb.InfluxDB;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RunWith(SpringRunner.class)
@SpringBootTest
public class InfluxdbDemoApplicationTests {

    @Resource
    private InfluxDBProperties influxDBProperties;
    @Resource
    InfluxDBConnect influxDBConnect;
    @Resource
    private InfluxDB influxDB;


    @Test
    public void contextLoads() {
        System.out.println("Test Start");
    }

    @Test
    public void testInsert() {
        Map<String, String> tagsMap = new HashMap<>();
        Map<String, Object> fieldsMap = new HashMap<>();
        System.out.println("influxDB start time :" + System.currentTimeMillis());
        int i = 0;
        for (; ; ) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            tagsMap.put("user_id", String.valueOf(i % 10));
            tagsMap.put("url", "http://www.baidu.com");
            tagsMap.put("service_method", "testInsert" + (i % 5));
            fieldsMap.put("count", i % 5);
            influxDBConnect.insert("usage", tagsMap, fieldsMap);
            i++;
        }
    }

    @Test
    public void testInsertPojo() {
        LogInfo logInfo = LogInfo.builder()
                .level("初级")
                .module("1")
                .deviceId("123455567787353422")
                .msg("成功")
                .build();
        Point point = Point.measurementByPOJO(logInfo.getClass())
                .addFieldsFromPOJO(logInfo)
                .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                .build();
        // 出于业务考量,设备可以设置不同的保存策略(策略名为固定前缀+设备ID)
//        influxDB.write(influxDBProperties.getDatabase(), influxDBProperties.getRetentionPolicy(), point);
        influxDBConnect.insertPOJO(point);
    }

    @Test
    public void testBatchInsertPojo() {

        BatchPoints batchPoints = BatchPoints.database("netscoutpoc")
                .retentionPolicy(influxDBProperties.getRetentionPolicy()).consistency(InfluxDB.ConsistencyLevel.ALL).build();

        LogInfo logInfo1 = LogInfo.builder()
                .level("初级")
                .module("1")
                .deviceId("123455567787353422")
                .msg("成功")
                .build();
        Point p1 = Point.measurementByPOJO(logInfo1.getClass())
                .addFieldsFromPOJO(logInfo1)
                .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                .build();


        LogInfo logInfo2 = LogInfo.builder()
                .level("中级")
                .module("2")
                .deviceId("222222222223455567787353422")
                .msg("成功")
                .build();
        Point p2 = Point.measurementByPOJO(logInfo2.getClass())
                .addFieldsFromPOJO(logInfo2)
                .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                .build();
        // 出于业务考量,设备可以设置不同的保存策略(策略名为固定前缀+设备ID)
//        influxDB.write(influxDBProperties.getDatabase(), influxDBProperties.getRetentionPolicy(), point);

        Collection<Point> points = Arrays.asList(p1, p2);
//        BatchPoints b = BatchPoints.builder().points(points).build();
        BatchPoints point = batchPoints.point(p1).point(p2);
//        List<Point> returned = b.getPoints();

        influxDBConnect.insertBatch(point);
    }

    @Test
    public void testQueryPojo() {
        Page page = new Page();
        // InfluxDB支持分页查询,因此可以设置分页查询条件
        String pageQuery = " LIMIT " + page.getPageSize() + " OFFSET " + ((page.getPageNum() - 1) * page.getPageSize());
        // 此处查询所有内容,如果
        String queryCmd = "SELECT * FROM "
                // 查询指定设备下的日志信息
                // 要指定从 RetentionPolicyName(保存策略前缀+设备ID).measurement(logInfo) 中查询指定数据)
                + influxDBProperties.getRetentionPolicy() + "SD1" + "." + "logInfo"
                // 添加查询条件(注意查询条件选择tag值,选择field数值会严重拖慢查询速度)
                //+ queryCondition
                // 查询结果需要按照时间排序
                + " ORDER BY time DESC"
                // 添加分页查询条件
                + pageQuery;
    }
}
