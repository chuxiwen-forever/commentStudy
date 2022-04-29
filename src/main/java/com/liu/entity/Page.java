package com.liu.entity;

import lombok.Data;

@Data
public class Page {
    //当前页码
    private Integer current = 1;
    //显示上限
    private Integer limit = 10;
    //数据总数(用于计算总页数)
    private Integer rows;
    //查询路径(用于复用分页链接)
    private String path;

    /**
     * 获取当前页的起始行
     *
     * current * limit - limit
     * @return
     */
    public Integer getOffset(){
        return (current - 1) * limit;
    }

    /**
     * 获得总页数
     *
     * @return
     */
    public Integer getTotal(){
        if (rows % limit ==0)
            return rows/limit;
        else
            return rows/limit + 1;
    }

    /**
     * 获取起始页码
     *
     * @return
     */
    public Integer getFrom(){
        int from = current - 2;
        return from < 1 ? 1 : from;
    }

    /**
     * 获取结束页码
     *
     * @return
     */
    public Integer getTo(){
        int to = current + 2;
        int total = getTotal();
        return to > total ? total : to;
    }
}
