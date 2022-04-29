package com.liu.mapper;

import com.liu.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author liu
 * @since 2021-10-29
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

}
