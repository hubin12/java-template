package com.mrbeard.project.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 角色权限
 *
 * @author hubin
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RolePermission {
    private Long id;

    private Long roleId;

    private Long permissionId;
}