package com.aiproject.springbootinit.mapper;

import com.aiproject.springbootinit.model.entity.Post;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.Date;
import java.util.List;

/**
 * 帖子数据库操作
 *
 * @author <a href="https://github.com/lijiangyiayo">程序员鱼皮</a>
 * @from <a href="https://jiangyiayo.icu">编程导航知识星球</a>
 */
public interface PostMapper extends BaseMapper<Post> {

    /**
     * 查询帖子列表（包括已被删除的数据）
     */
    List<Post> listPostWithDelete(Date minUpdateTime);

}




