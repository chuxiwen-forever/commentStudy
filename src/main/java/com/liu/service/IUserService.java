package com.liu.service;

import com.liu.entity.LoginTicket;
import com.liu.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author liu
 * @since 2021-10-29
 */
public interface IUserService extends IService<User> {
    User findUserById(int id);
    //注册
    Map<String,Object> register(User user);
    //邮箱激活业务
    int activation(Integer id, String code);
    //登录
    Map<String,Object> login(String username,String password,int expiredSeconds);
    //退出
    void logout(String ticket);
    //获取LoginTicket
    LoginTicket findLoginTicket(String ticket);
    //更新头像
    int updateHeader(int userId , String headerUrl);
    //通过名字查用户
    User findUserByName(String name);
}
