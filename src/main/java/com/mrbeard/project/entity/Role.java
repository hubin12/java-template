package com.mrbeard.project.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 角色
 * @author data
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Role {
    private Long id;

    private String roleName;

    private String roleValue;

    private String description;
}