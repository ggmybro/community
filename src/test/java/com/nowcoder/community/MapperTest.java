package com.nowcoder.community;

import com.nowcoder.community.dao.DiscussPostMapper;
import com.nowcoder.community.dao.UserMapper;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.util.Date;
import java.util.List;

@ContextConfiguration(classes = CommunityApplication.class)
@SpringBootTest
public class MapperTest {

    @Autowired
    private UserMapper userMapper;

    @Test
    public void testSelectUser(){
        User user = userMapper.selectById(101);
        System.out.println(user);

        user = userMapper.selectByName("liubei");
        System.out.println(user);

        user = userMapper.selectByEmail("nowcoder101@sina.com");
        System.out.println(user);
    }

    @Test
    public void testInsertUser(){
        User user = new User();
        user.setUsername("wzf");
        user.setPassword("wzf19990126");
        user.setSalt("abc");
        user.setEmail("wzf@qq.com");
        user.setHeaderUrl("http://www.nowcoder.com/100.png");
        user.setCreateTime(new Date());
        int rows = userMapper.insertUser(user);
        System.out.println(rows);
        System.out.println(user.getId());
    }

    @Test
    public void testUpdate(){
        int i = userMapper.updateHeader(150, "http://www.nowcoder.com/101.png");
        int i1 = userMapper.updatePassword(150, "password");
        int i2 = userMapper.updateStatus(150, 1);
        User user = userMapper.selectByName("wzf");
        System.out.println(i + "," + i1 + "," + i2);
        System.out.println(user);
    }

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Test
    public void testDiscussPost(){
        List<DiscussPost> discussPosts = discussPostMapper.selectDiscussPosts(0, 0, 10);
        for(DiscussPost discussPost : discussPosts){
            System.out.println(discussPost);
        }
        int i = discussPostMapper.selectDiscussPostRows(0);
        System.out.println(i);
    }
}
