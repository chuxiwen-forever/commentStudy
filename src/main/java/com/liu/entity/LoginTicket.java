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
 * @since 2021-10-31
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class LoginTicket implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer userId;

    @TableField(fill = FieldFill.INSERT)
    private String ticket;

    /**
     * 0-有效;1-无效;
     */
    @TableField(fill = FieldFill.INSERT)
    private Integer status;

    /**
     * 过期时间
     */
    private Date expired;


}
