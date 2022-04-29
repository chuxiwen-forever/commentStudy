package com.liu.controller;

import com.google.code.kaptcha.Producer;
import com.liu.service.IUserService;
import com.liu.util.CommunityConstant;
import com.liu.util.CommunityUtil;
import com.liu.util.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Controller
public class LoginController implements CommunityConstant {

    @Autowired
    private Producer captchaProducer;
    @Autowired
    private IUserService userService;
    @Value("${server.servlet.context-path}")
    private String contextPath;
    @Autowired
    private RedisTemplate redisTemplate;

    @GetMapping("/login")
    public String getLoginPage(){
        return "/site/login";
    }

    //获取验证码
    @GetMapping("/captcha")
    public void getCaptcha(HttpServletResponse response /*, HttpSession session*/){
        //生成验证码
        String text = captchaProducer.createText();
        BufferedImage image = captchaProducer.createImage(text);
        //将验证码存入session
//        session.setAttribute("captcha",text);
        /**
         * 重构登录逻辑，使用redis
         */
        String captchaOwner = CommunityUtil.generateUUID();
        Cookie cookie = new Cookie("captchaOwner",captchaOwner);
        cookie.setMaxAge(60);
        cookie.setPath(contextPath);
        response.addCookie(cookie);
        String captchaKey = RedisKeyUtil.getCaptchaKey(captchaOwner);
        redisTemplate.opsForValue().set(captchaKey,text,60, TimeUnit.SECONDS);
        //将图片输出到浏览器
        response.setContentType("image/png");
        try {
            ServletOutputStream os = response.getOutputStream();
            ImageIO.write(image,"png",os);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @PostMapping("/login")
    public String login(String username, String password, String code, boolean rememberMe,
                        HttpServletResponse response/*, HttpSession session*/, Model model,@CookieValue("captchaOwner") String captchaOwner){
        //检查验证码
//        String captcha =(String) session.getAttribute("captcha");
        /**
         * 重构登录逻辑
         */
        String captcha = null;
        if (StringUtils.isNotBlank(captchaOwner)){
            String captchaKey = RedisKeyUtil.getCaptchaKey(captchaOwner);
            captcha = (String) redisTemplate.opsForValue().get(captchaKey);
        }
        if (StringUtils.isBlank(captcha) || StringUtils.isBlank(code) || !captcha.equalsIgnoreCase(code)){
            model.addAttribute("codeMsg","验证码不正确！！");
            return "/site/login";
        }
        //检查账号。密码
        int expiredSeconds = rememberMe ? REMEMBER_EXPIRED_SECONDS : DEFAULT_EXPIRED_SECONDS;
        Map<String, Object> map = userService.login(username, password, expiredSeconds);
        if (map.containsKey("ticket")){
            Cookie cookie = new Cookie("ticket", map.get("ticket").toString());
            cookie.setPath(contextPath);
            cookie.setMaxAge(expiredSeconds);
            response.addCookie(cookie);
            return "redirect:/index";
        }else {
            model.addAttribute("usernameMsg",map.get("usernameMsg"));
            model.addAttribute("passwordMsg",map.get("passwordMsg"));
            return "/site/login";
        }
    }
    @GetMapping("/logout")
    public String logout(@CookieValue("ticket") String ticket){
        userService.logout(ticket);
        return "redirect:/login";
    }
}
