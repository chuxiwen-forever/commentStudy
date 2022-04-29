package com.liu.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.liu.annotation.LoginRequired;
import com.liu.entity.Comment;
import com.liu.entity.DiscussPost;
import com.liu.entity.Page;
import com.liu.entity.User;
import com.liu.mapper.DiscussPostMapper;
import com.liu.service.ICommentService;
import com.liu.service.IDiscussPostService;
import com.liu.service.ILikeService;
import com.liu.service.IUserService;
import com.liu.util.CommunityConstant;
import com.liu.util.CommunityUtil;
import com.liu.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author liu
 * @since 2021-10-29
 */
@Controller
@RequestMapping("/discuss")
public class DiscussPostController implements CommunityConstant {

    @Autowired
    private IDiscussPostService discussPostService;
    @Autowired
    private HostHolder hostHolder;
    @Autowired
    private IUserService userService;
    @Autowired
    private ICommentService commentService;
    @Autowired
    private ILikeService likeService;

    @PostMapping("/add")
    @ResponseBody
    @LoginRequired
    public String addDiscussPost(String title,String content){
        User user = hostHolder.getUser();
        if (user == null)
            return CommunityUtil.getJSONString(403,"你还没有登录");
        DiscussPost post = new DiscussPost();
        post.setUserId(user.getId());
        post.setTitle(title);
        post.setContent(content);
        discussPostService.addDiscussPost(post);
        return CommunityUtil.getJSONString(0,"发布成功!");
    }

    /**
     * 查询文章
     * @param discussPostId  文章id
     * @param model  视图层
     * @return  html界面
     */
    @GetMapping("/detail/{discussPostId}")
    public String getDiscussPost(@PathVariable("discussPostId") int discussPostId , Model model , Page page){
        QueryWrapper<DiscussPost> eq = new QueryWrapper<>();
        eq.eq("id",discussPostId);
        //查询文章信息
        DiscussPost post = discussPostService.getOne(eq);
        model.addAttribute("post",post);
        //根据文章作者id查询作者
        User user = userService.findUserById(post.getUserId());
        //点赞数量
        long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, discussPostId);
        model.addAttribute("likeCount",likeCount);
        //点赞状态
        int likeStatus = hostHolder.getUser() == null ? 0 : likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_POST, discussPostId);
        model.addAttribute("likeStatus",likeStatus);
        //评论分页信息
        page.setLimit(5);
        page.setPath("/discuss/detail/"+discussPostId);
        page.setRows(post.getCommentCount());

        List<Comment> comments = commentService.findCommentsByEntity(ENTITY_TYPE_POST, post.getId(), page.getOffset(), page.getLimit());
        List<Map<String,Object>> commentVOList = new ArrayList<>();
        //评论VO列表
        if (comments != null){
            for (Comment comment:comments) {
                //评论VO
                Map<String,Object> commentVO = new HashMap<>();
                //评论
                commentVO.put("comment",comment);
                //作者
                commentVO.put("user",userService.findUserById(comment.getUserId()));
                //点赞数量
                likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, comment.getId());
                commentVO.put("likeCount",likeCount);
                //点赞状态
                likeStatus = hostHolder.getUser() == null ? 0 : likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_COMMENT, comment.getId());
                commentVO.put("likeStatus",likeStatus);

                //回复列表
                List<Comment> replyList = commentService.findCommentsByEntity(ENTITY_TYPE_COMMENT, comment.getId(), 0, Integer.MAX_VALUE);
                //回复VO列表
                List<Map<String,Object>> replyVOList = new ArrayList<>();
                if (replyList != null){
                    for (Comment reply:replyList) {
                        Map<String,Object> replyVO =new HashMap<>();
                        //回复的回复
                        replyVO.put("reply",reply);
                        //回复的回复作者
                        replyVO.put("user",userService.findUserById(reply.getUserId()));
                        //回复目标
                        User target = reply.getTargetId() == 0 ? null : userService.findUserById(reply.getUserId());
                        replyVO.put("target",target);
                        //点赞数量
                        likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT,reply.getId());
                        replyVO.put("likeCount",likeCount);
                        //点赞状态
                        likeStatus = hostHolder.getUser() == null ? 0 : likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_COMMENT, reply.getId());
                        replyVO.put("likeStatus",likeStatus);

                        replyVOList.add(replyVO);
                    }
                }
                commentVO.put("replys",replyVOList);
                //回复数量
                int replyCount = commentService.findCommentCount(ENTITY_TYPE_COMMENT,comment.getId());
                commentVO.put("replyCount",replyCount);
                commentVOList.add(commentVO);
            }
        }

        model.addAttribute("user",user);
        model.addAttribute("page",page);
        model.addAttribute("comments",commentVOList);

        return "/site/discuss-detail";
    }
}
