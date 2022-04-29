package com.liu.controller;

import com.liu.entity.DiscussPost;
import com.liu.entity.Page;
import com.liu.entity.User;
import com.liu.service.IDiscussPostService;
import com.liu.service.ILikeService;
import com.liu.service.IMessageService;
import com.liu.service.IUserService;
import com.liu.util.CommunityConstant;
import com.liu.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class HomeController implements CommunityConstant {
    @Autowired
    private IDiscussPostService discussPostService;
    @Autowired
    private IUserService userService;
    @Autowired
    private ILikeService likeService;
    @Autowired
    private IMessageService messageService;
    @Autowired
    private HostHolder hostHolder;

    @GetMapping("/index")
    public String getIndexPage(Model model , Page page){
        page.setRows(discussPostService.findDiscussPostRows(0));
        page.setPath("/index");
        List<DiscussPost> list = discussPostService.findDiscussPosts(0, page.getOffset(), page.getLimit());
        List<Map<String,Object>> discussPosts = new ArrayList<>();
        if (list!=null){
            for (DiscussPost post: list) {
                Map<String , Object> map = new HashMap<>();
                map.put("post",post);
                User user = userService.findUserById(post.getUserId());
                map.put("user",user);

                //获取点赞数量
                long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, post.getId());
                map.put("likeCount",likeCount);
                discussPosts.add(map);
            }
        }
        if (hostHolder.getUser() != null){
            int unreadCount = messageService.findLetterUnreadCount(hostHolder.getUser().getId(), null);
            model.addAttribute("unreadCount",unreadCount);
        }

        model.addAttribute("discussPosts",discussPosts);
        model.addAttribute("page",page);
        return "/index";
    }

    @GetMapping("/error")
    public String getErrorPage(){
        return "/error/500";
    }
}
