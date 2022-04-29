package com.liu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.liu.entity.LoginTicket;
import com.liu.entity.User;
import com.liu.mapper.LoginTicketMapper;
import com.liu.mapper.UserMapper;
import com.liu.service.IUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.liu.util.CommunityConstant;
import com.liu.util.CommunityUtil;
import com.liu.util.MailClient;
import com.liu.util.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author liu
 * @since 2021-10-29
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService , CommunityConstant {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private MailClient mailClient;
    @Autowired
    private TemplateEngine templateEngine;
    @Value("${community.path.domain}")
    private String domain;  //域名
    @Value("${server.servlet.context-path}")
    private String contextPath;  //项目名
//    @Autowired
//    private LoginTicketMapper loginTicketMapper;
    @Autowired
    private RedisTemplate redisTemplate;


    @Override
    public User findUserById(int id) {
//        QueryWrapper<User> eq = new QueryWrapper<>();
//        eq.eq("id",id);
//        return userMapper.selectOne(eq);
        /**
         * 重构
         */
        //先看redis中有没有
        User user = getCache(id);
        if (user == null){
            user = initCache(id);
        }
        return user;
    }

    @Override
    public Map<String, Object> register(User user) {
        Map<String, Object> map = new HashMap<>();
        if (user == null){
            throw new IllegalArgumentException("参数不能为空");
        }
        if (StringUtils.isBlank(user.getUsername())){
            map.put("usernameMsg","账号不能为空!!!");
            return map;
        }
        if (StringUtils.isBlank(user.getPassword())){
            map.put("passwordMsg","密码不能为空!!!");
            return map;
        }
        if (StringUtils.isBlank(user.getEmail())){
            map.put("emailMsg","邮箱不能为空!!!");
            return map;
        }
        //验证账号
        QueryWrapper<User> eq = new QueryWrapper<>();
        eq.eq("username",user.getUsername());
        eq.eq("email",user.getEmail());
        User findElsePeopleButNameSame = userMapper.selectOne(eq);
        if (findElsePeopleButNameSame != null){
            map.put("usernameMsg","该账号已经存在!!");
            return map;
        }
        user.setSalt(CommunityUtil.generateUUID().substring(0,5));
        user.setPassword(CommunityUtil.md5(user.getPassword()+user.getSalt()));
        userMapper.insert(user);
        //激活邮件
        Context context = new Context();
        context.setVariable("email",user.getEmail());
        String url = domain + contextPath + "/activation/"+user.getId()+"/"+user.getActivationCode();
        context.setVariable("url",url);
        String content = templateEngine.process("/mail/activation", context);
        mailClient.sendMail(user.getEmail(),"激活账号",content);
        return map;
    }

    @Override
    public int activation(Integer id , String code){
        QueryWrapper<User> eq = new QueryWrapper<>();
        eq.eq("id",id);
        User user = userMapper.selectOne(eq);
        if (user.getStatus() == 1){
            return ACTIVATION_REPEAT;
        }else if (user.getActivationCode().equals(code)){
            user.setStatus(1);
            userMapper.updateById(user);
            clearCache(user.getId());
            return ACTIVATION_SUCCESS;
        }else {
            return ACTIVATION_FAILURE;
        }
    }

    @Override
    public Map<String, Object> login(String username, String password, int expiredSeconds) {
        Map<String, Object> map = new HashMap<>();
        //空值处理
        if (StringUtils.isBlank(username)){
            map.put("usernameMsg","账号不能为空!");
            return map;
        }
        if (StringUtils.isBlank(password)){
            map.put("passwordMsg","密码不能为空!");
            return map;
        }
        QueryWrapper<User> eq = new QueryWrapper<>();
        eq.eq("username",username);
        User user = userMapper.selectOne(eq);
        if (user == null ){
            map.put("usernameMsg","用户名不存在!!");
        }
        if (user.getStatus() == 0){
            map.put("usernameMsg","该账号未激活！！");
            return map;
        }
        //验证密码
       password = CommunityUtil.md5(password+user.getSalt());
        if (!user.getPassword().equals(password)){
            map.put("passwordMsg","密码错误!!");
            return map;
        }
        //生产登录凭证
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getId());
        loginTicket.setExpired(new Date(System.currentTimeMillis()+(expiredSeconds * 1000)));
//        loginTicketMapper.insert(loginTicket);
        String redisKey = RedisKeyUtil.getTicketKey(loginTicket.getTicket());
        redisTemplate.opsForValue().set(redisKey,loginTicket);

        map.put("ticket",loginTicket.getTicket());
        return map;
    }

    /**
     * 重构  使用redis
     * @param ticket
     */
    @Override
    public void logout(String ticket) {
//        QueryWrapper<LoginTicket> eq = new QueryWrapper<>();
//        eq.eq("ticket",ticket);
//        LoginTicket loginTicket = loginTicketMapper.selectOne(eq);
//        loginTicket.setStatus(1);
//        loginTicketMapper.updateById(loginTicket);
        String redisKey = RedisKeyUtil.getTicketKey(ticket);
        LoginTicket loginTicket = (LoginTicket) redisTemplate.opsForValue().get(redisKey);
        loginTicket.setStatus(1);
        redisTemplate.opsForValue().set(redisKey,loginTicket);
    }

    @Override
    public LoginTicket findLoginTicket(String ticket) {
//        QueryWrapper<LoginTicket> eq = new QueryWrapper<>();
//        eq.eq("ticket",ticket);
//        return loginTicketMapper.selectOne(eq);
        String redisKey = RedisKeyUtil.getTicketKey(ticket);
        return (LoginTicket) redisTemplate.opsForValue().get(redisKey);
    }

    @Override
    public int updateHeader(int userId, String headerUrl) {
        QueryWrapper<User> eq = new QueryWrapper<>();
        eq.eq("id",userId);
        User user = userMapper.selectOne(eq);
        user.setHeaderUrl(headerUrl);
        int rows = userMapper.updateById(user);
        clearCache(userId);
        return rows;
    }

    @Override
    public User findUserByName(String name) {
        QueryWrapper<User> eq = new QueryWrapper<>();
        eq.eq("username",name);
        return userMapper.selectOne(eq);
    }

    //1.优先从缓存中取值
    private User getCache(int userId){
        String redisKey = RedisKeyUtil.getUserKey(userId);
        return (User) redisTemplate.opsForValue().get(redisKey);
    }
    //2.取不到时初始化缓存数据
    private User initCache(int userId){
        QueryWrapper<User> eq = new QueryWrapper<>();
        eq.eq("id",userId);
        User user = userMapper.selectOne(eq);
        String redisKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.opsForValue().set(redisKey,user,3600, TimeUnit.SECONDS);
        return user;
    }
    //3。数据变更时清除缓存数据
    private void clearCache(int userId){
        String redisKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.delete(redisKey);
    }
}
