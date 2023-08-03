package com.nowcoder.community.controller;

import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class HomeController {

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private UserService userService;

    @RequestMapping(path = "/index", method = RequestMethod.GET)
    public String getIndexpage(Model model, Page page){
        //方法调用前，SpringMVC会自动实例化参数Model和Page，并将Page注入Model。
        //所以，在thymeleaf中可以直接访问Page对象中的数据。
        //Page的总行数和路径由服务器传入
        page.setRows(discussPostService.findDiscussPostRows(0));
        page.setPath("/index");
        //Page的其他参数例如current、limit、from、to等由浏览器传入
        List<DiscussPost> discussPosts = discussPostService.findDiscussPosts(0, page.getOffset(), page.getLimit());
        List<Map<String, Object>> list = new ArrayList<>();
        if(discussPosts != null){
            for(DiscussPost discussPost : discussPosts){
                Map<String, Object> map = new HashMap<>();
                map.put("post", discussPost);
                User userById = userService.findUserById(discussPost.getUserid());
                map.put("user", userById);
                list.add(map);
            }
        }
        model.addAttribute("discussPosts", list);
        return "/index";
    }
}
