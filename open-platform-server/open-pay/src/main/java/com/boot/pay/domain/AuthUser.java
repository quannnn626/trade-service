package com.boot.pay.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 认证用户表（归属 auth 体系）
 * @TableName auth_user
 */
@TableName(value ="auth_user")
@Data
public class AuthUser {
    /**
     * 用户ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 用户编号
     */
    private String userNo;

    /**
     * 登录账号
     */
    private String username;

    /**
     * 加密密码
     */
    private String password;

    /**
     * 展示昵称
     */
    private String nickname;

    /**
     * 头像URL
     */
    private String avatar;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 最后登录时间
     */
    private Date lastLoginTime;

    /**
     * 状态 1正常 0禁用
     */
    private Integer status;

    /**
     *
     */
    private Date createTime;

    /**
     *
     */
    private Date updateTime;
}
