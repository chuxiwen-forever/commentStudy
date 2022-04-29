package com.liu.controller;


import com.liu.annotation.LoginRequired;
import com.liu.entity.Comment;
import com.liu.service.ICommentService;
import com.liu.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;


/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author liu
 * @since 2021-11-01
 */
@Controller
@RequestMapping("/comment")
public class CommentController {

    @Autowired
    private ICommentService commentService;
    @Autowired
    private HostHolder hostHolder;

    @PostMapping("/add/{discussPostId}")
    @LoginRequired
    public String addComment(@PathVariable("discussPostId") int discussPostId, Comment comment){
        comment.setUserId(hostHolder.getUser().getId());
        commentService.addComment(comment);
        return "redirect:/discuss/detail"+discussPostId;
    }


}
