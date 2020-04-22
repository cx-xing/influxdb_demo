package com.muscleape.influxdb.entity;

/**
 * @author C__xing
 * @version 1.0
 * @date 2020/4/22
 */
public class Page {

    private Integer pageSize;
    private Integer pageNum;

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public Integer getPageNum() {
        return pageNum;
    }

    public void setPageNum(Integer pageNum) {
        this.pageNum = pageNum;
    }
}
