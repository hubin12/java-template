package com.mrbeard.project.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * User实体类
 *
 * @author hubin
 * @date 2020-12-1
 *
 */
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class User {
    private Long id;

    private String userName;

    private String nickName;

    private String avatar;

    private String password;

    private String phone;

    private Integer root;

    private String email;

    private Date loginDate;

    private Date createDate;

    private Date updateDate;
}