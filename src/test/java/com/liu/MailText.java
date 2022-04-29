package com.liu;

import com.liu.util.CommunityUtil;
import com.liu.util.MailClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class MailText {
    @Autowired
    private MailClient mailClient;

    @Test
    void send(){
        mailClient.sendMail("785572787@qq.com","first","send emil success!!");
    }
    @Test
    void md5(){
        String s = CommunityUtil.md5("lpj160918.");
        String s1 = CommunityUtil.md5("lpj160918.");
        System.out.println(s);
        System.out.println(s1);
    }
}
