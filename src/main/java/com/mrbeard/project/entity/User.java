package com.mrbeard.project.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * @author hubin
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