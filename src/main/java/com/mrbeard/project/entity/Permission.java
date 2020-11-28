package com.mrbeard.project.entity;

import java.util.Date;

public class Permission {
    private Long id;

    private String permisisonName;

    private String permissionValue;

    private String path;

    private Long children;

    private Date createDate;

    private Date updateDate;

    public Permission(Long id, String permisisonName, String permissionValue, String path, Long children, Date createDate, Date updateDate) {
        this.id = id;
        this.permisisonName = permisisonName;
        this.permissionValue = permissionValue;
        this.path = path;
        this.children = children;
        this.createDate = createDate;
        this.updateDate = updateDate;
    }

    public Permission() {
        super();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPermisisonName() {
        return permisisonName;
    }

    public void setPermisisonName(String permisisonName) {
        this.permisisonName = permisisonName == null ? null : permisisonName.trim();
    }

    public String getPermissionValue() {
        return permissionValue;
    }

    public void setPermissionValue(String permissionValue) {
        this.permissionValue = permissionValue == null ? null : permissionValue.trim();
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path == null ? null : path.trim();
    }

    public Long getChildren() {
        return children;
    }

    public void setChildren(Long children) {
        this.children = children;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public Date getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }
}