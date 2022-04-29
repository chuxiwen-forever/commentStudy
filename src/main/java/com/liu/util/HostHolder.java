package com.liu.util;

import com.liu.entity.User;
import org.springframework.stereotype.Component;

/**
 * 持有用户信息，用于代替session对象
 */
@Component
public class HostHolder {
    private ThreadLocal<User> users = new ThreadLocal<>();

    //存入私有线程的客户
    public void setUser(User user){
        users.set(user);
    }
    //读取私有线程的用户
    public User getUser(){
        return users.get();
    }
    //清理ThreadLocal中的信息
    public void clear(){
        users.remove();
    }
}
