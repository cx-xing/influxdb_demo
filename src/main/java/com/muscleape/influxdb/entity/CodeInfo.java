package com.muscleape.influxdb.entity;

/**
 * @author C__xing
 * @version 1.0
 * @date 2020/4/22
 */
public class CodeInfo {

    private long id;
    private String name;
    private String code;
    private String descr;
    private String descrE;
    private String createdBy;
    private long createdAt;

    public void setId(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public void setDescr(String descr) {
        this.descr = descr;
    }

    public String getDescr() {
        return descr;
    }

    public void setDescrE(String descrE) {
        this.descrE = descrE;
    }

    public String getDescrE() {
        return descrE;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getCreatedAt() {
        return createdAt;
    }
}
