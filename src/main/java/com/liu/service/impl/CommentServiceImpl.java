package com.liu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.liu.entity.Comment;
import com.liu.entity.DiscussPost;
import com.liu.mapper.CommentMapper;
import com.liu.mapper.DiscussPostMapper;
import com.liu.service.ICommentService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.liu.util.CommunityConstant;
import com.liu.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author liu
 * @since 2021-11-01
 */
@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements ICommentService, CommunityConstant {

    @Autowired
    private CommentMapper commentMapper;
    @Autowired
    private SensitiveFilter sensitiveFilter;
    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Override
    public List<Comment> findCommentsByEntity(int entityType, int entityId, int offset, int limit) {
        return commentMapper.selectCommentsByEntity(entityType,entityId,offset,limit);
    }

    @Override
    public int findCommentCount(int entityType, int entityId) {
        return commentMapper.selectCountByEntity(entityType,entityId);
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED,propagation = Propagation.REQUIRED)
    public int addComment(Comment comment) {
        if (comment == null)
            throw new IllegalArgumentException("参数不能为空！");
        //转义评论中的html标签
        comment.setContent(HtmlUtils.htmlEscape(comment.getContent()));
        //过滤评论中的敏感词
        comment.setContent(sensitiveFilter.filter(comment.getContent()));
        int rows = commentMapper.insert(comment);
        //更新帖子评论数量
        if (comment.getEntityType() == ENTITY_TYPE_POST){
            QueryWrapper<Comment> eq = new QueryWrapper<>();
            eq.eq("entity_type",comment.getEntityType());
            eq.eq("entity_id",comment.getEntityId());
            Integer count = commentMapper.selectCount(eq);
            QueryWrapper<DiscussPost> eq2 =new QueryWrapper<>();
            eq2.eq("id",comment.getEntityId());
            DiscussPost discussPost = discussPostMapper.selectOne(eq2);
            discussPost.setCommentCount(count);
            discussPostMapper.updateById(discussPost);
        }
        return rows;
    }
}
