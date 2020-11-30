package com.mrbeard.project.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @author hubin
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Permission {
    private Long id;

    private String permisisonName;

    private String permissionValue;

    private String path;

    private Long children;

    private Date createDate;

    private Date updateDate;

}