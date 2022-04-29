package com.liu.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.liu.util.CommunityUtil;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class MybatisPlusMetaObjectHandler implements MetaObjectHandler {
    @Override
    public void insertFill(MetaObject metaObject) {
        //user表
        this.strictInsertFill(metaObject,"type",Integer.class,0);
        this.strictInsertFill(metaObject,"status",Integer.class,0);
        this.strictInsertFill(metaObject,"activationCode",String.class,CommunityUtil.generateUUID());
        this.strictInsertFill(metaObject,"headerUrl",String.class,"https://images.nowcoder.com/head/813m.png");
        this.strictInsertFill(metaObject,"createTime",Date.class,new Date());

        //loginTicket表
        this.strictInsertFill(metaObject,"ticket",String.class,CommunityUtil.generateUUID());

        //DiscussPost表
        this.strictInsertFill(metaObject,"commentCount",Integer.class,0);
    }

    @Override
    public void updateFill(MetaObject metaObject) {

    }
}
