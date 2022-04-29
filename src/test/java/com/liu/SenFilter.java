package com.liu;

import com.liu.mapper.DiscussPostMapper;
import com.liu.util.SensitiveFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class SenFilter {

    @Autowired
    private SensitiveFilter sensitiveFilter;


    @Test
    public void textSensitiveFilter(){

    }
}
