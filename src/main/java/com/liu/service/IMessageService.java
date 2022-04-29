package com.liu.service;

import com.liu.entity.Message;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author liu
 * @since 2021-11-02
 */
public interface IMessageService extends IService<Message> {

    List<Message> findConversation(int userId,int offset,int limit);

    int findConversationCount(int userId);

    List<Message> findLetters(String conversationId ,int offset,int limit);

    int findLetterCount(String conversationId);

    int findLetterUnreadCount(int userId,String conversationId);

    int addMessage(Message message);

    int readMessage(List<Integer> ids);
}
