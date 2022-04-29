package com.liu.service;

import com.liu.entity.DiscussPost;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author liu
 * @since 2021-10-29
 */
public interface IDiscussPostService extends IService<DiscussPost> {
    List<DiscussPost> findDiscussPosts(int userId, int offset, int limit);
    int findDiscussPostRows(int userId);
    //添加文章
    int addDiscussPost(DiscussPost post);
}
