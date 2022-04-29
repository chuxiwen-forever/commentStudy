package com.liu.controller;


import com.liu.annotation.LoginRequired;
import com.liu.entity.User;
import com.liu.service.IFollowService;
import com.liu.service.ILikeService;
import com.liu.service.IUserService;
import com.liu.util.CommunityConstant;
import com.liu.util.CommunityUtil;
import com.liu.util.HostHolder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author liu
 * @since 2021-10-29
 */
@Controller
@RequestMapping("/user")
public class UserController implements CommunityConstant {
    @Value("${community.path.upload}")
    private String uploadPath;
    @Value("${community.path.domain}")
    private String domain;
    @Value("${server.servlet.context-path}")
    private String contextPath;
    @Autowired
    private IUserService userService;
    @Autowired
    private HostHolder hostHolder;
    @Autowired
    private ILikeService likeService;
    @Autowired
    private IFollowService followService;

    @LoginRequired
    @GetMapping("/setting")
    public String getSettingPage(){
        return "/site/setting";
    }


    //上传文件
    @LoginRequired
    @PostMapping("/upload")
    public String uploadHeader(MultipartFile headerImage, Model model){
        if (headerImage == null){
            model.addAttribute("error","您还没有选择头像!!");
            return "/site/setting";
        }
        String filename = headerImage.getOriginalFilename();
        String suffix = filename.substring(filename.lastIndexOf("."));
        if (StringUtils.isBlank(suffix)){
            model.addAttribute("error","文件格式不正确！");
            return "/site/setting";
        }
        //生成随机文件名
        filename = CommunityUtil.generateUUID()+suffix;
        //确定文件存放的路径
        File file = new File(uploadPath + "/" + filename);
        try {
            //存储文件
            headerImage.transferTo(file);
        } catch (IOException e) {
            e.getMessage();
            throw new RuntimeException("上传文件失败，服务器发生异常！",e);
        }
        //更新当前用户的头像路径
        User user = hostHolder.getUser();
        String headerUrl = domain + contextPath + "/user/header/" + filename;
        userService.updateHeader(user.getId(),headerUrl);
        return "redirect:/index";
    }

    @GetMapping("/header/{fileName}")
    public void getHeader(@PathVariable("fileName") String filename, HttpServletResponse response){
        //服务器存放路径
        filename = uploadPath + "/" +filename;
        //文件后缀
        String suffix = filename.substring(filename.lastIndexOf("."));
        System.err.println(suffix);
        //响应图片
        response.setContentType("image/"+suffix);
        try(OutputStream os = response.getOutputStream();
                FileInputStream fis = new FileInputStream(filename);) {
            byte[] buffer = new byte[1024];
            int len = 0;
            while ((len = fis.read(buffer)) != -1){
                os.write(buffer,0,len);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //个人主页
    @GetMapping("/profile/{userId}")
    public String getProfilePage(@PathVariable("userId") int userId,Model model){
        User user = userService.findUserById(userId);
        if (user == null){
            throw new RuntimeException("该用户不存在");
        }
        //用户
        model.addAttribute("user",user);
        //点赞数量
        long likeCount = likeService.findUserLikeCount(userId);
        model.addAttribute("likeCount",likeCount);
        //关注数量
        long followeeCount = followService.findFolloweeCount(userId, ENTITY_TYPE_USER);
        model.addAttribute("followeeCount",followeeCount);
        //粉丝数量
        long followerCount = followService.findFollowerCount(ENTITY_TYPE_USER, userId);
        model.addAttribute("followerCount",followerCount);
        //是否已经关注
        boolean hasFollowed = false;
        if (hostHolder.getUser() != null){
            hasFollowed = followService.hasFollowed(hostHolder.getUser().getId(),ENTITY_TYPE_USER,userId);
        }
        model.addAttribute("hasFollowed",hasFollowed);
        return "/site/profile";
    }
}
