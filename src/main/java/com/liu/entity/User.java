package com.liu.entity;

import java.time.LocalDateTime;
import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 
 * </p>
 *
 * @author liu
 * @since 2021-10-29
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Integer id;

    private String username;

    private String password;

    private String salt;

    private String email;

    /**
     * 0-普通用户;1-管理员;2-版主
     */
    @TableField(fill = FieldFill.INSERT)
    private Integer type;

    /**
     * 0-未激活;1-已激活
     */
    @TableField(fill = FieldFill.INSERT)
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private String activationCode;

    @TableField(fill = FieldFill.INSERT)
    private String headerUrl;

    @TableField(fill = FieldFill.INSERT)
    private Date createTime;


}
