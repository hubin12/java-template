package com.mrbeard.project.entity;

public class Role {
    private Long id;

    private String roleName;

    private String roleValue;

    private String description;

    public Role(Long id, String roleName, String roleValue, String description) {
        this.id = id;
        this.roleName = roleName;
        this.roleValue = roleValue;
        this.description = description;
    }

    public Role() {
        super();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName == null ? null : roleName.trim();
    }

    public String getRoleValue() {
        return roleValue;
    }

    public void setRoleValue(String roleValue) {
        this.roleValue = roleValue == null ? null : roleValue.trim();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description == null ? null : description.trim();
    }
}