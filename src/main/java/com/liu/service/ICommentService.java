package com.liu.service;

import com.liu.entity.Comment;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author liu
 * @since 2021-11-01
 */
public interface ICommentService extends IService<Comment> {

    List<Comment> findCommentsByEntity(int entityType,int entityId,int offset,int limit);

    int findCommentCount(int entityType,int entityId);

    //添加评论
    int addComment(Comment comment);
}
