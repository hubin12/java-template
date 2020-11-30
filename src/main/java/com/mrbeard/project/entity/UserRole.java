package com.mrbeard.project.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户角色表
 *
 * @author hubin
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRole {
    private Long id;

    private Long userId;

    private Long roleId;
}