package com.nowcoder.community.controller;

import com.nowcoder.community.service.AlphaService;
import com.nowcoder.community.util.CommunityUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.websocket.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/alpha")
public class AlphaController {

    @RequestMapping("/hello")
    @ResponseBody
    public String sayHello(){
        return "Hello Spring Boot.";
    }

    @Autowired
    private AlphaService alphaService;

    @RequestMapping("/data")
    @ResponseBody
    public String getData(){
        return alphaService.find();
    }


    //底层MVC处理方式
    @RequestMapping("/http")
    public void http(HttpServletRequest request, HttpServletResponse response){
        //获取请求数据
        System.out.println(request.getMethod());
        System.out.println(request.getServletPath()); //ServletPath与ContextPath的区别？
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()){
            String headerName = headerNames.nextElement();
            String header = request.getHeader(headerName);
            System.out.println(headerName + ":" + header);
        }
        System.out.println(request.getParameter("code"));
        System.out.println(request.getParameter("name"));
        //返回响应数据
        response.setContentType("text/html;charset=utf-8");
        try (PrintWriter writer = response.getWriter()) {
            writer.write("<h1>牛客网</h1>");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    //框架MVC处理方式

    //服务器处理get请求
    //get请求处理方式一：/students?current=1&limit=20
    @RequestMapping(path = "/students", method = RequestMethod.GET)
    @ResponseBody
    public String getStudents(
            @RequestParam(name = "current", required = false, defaultValue = "1") int current,
            @RequestParam(name = "limit", required = false, defaultValue = "10") int limit
    ){
        System.out.println(current);
        System.out.println(limit);
        return "some students";
    }

    //get请求处理方式二：/student/666
    @RequestMapping(path = "/student/{id}", method = RequestMethod.GET)
    @ResponseBody
    public String getStudent(@PathVariable("id") int id){
        System.out.println(id);
        return "student 666";
    }

    //post请求处理方式
    @RequestMapping(path = "/student", method = RequestMethod.POST)
    @ResponseBody
    public String subStudent(String name, int age){
        System.out.println(name + ":" + age);
        return "提交成功";
    }

    //响应HTML数据

    //方式一
    @RequestMapping(path = "/teacher", method = RequestMethod.GET)
    public ModelAndView getTeacher(){
        ModelAndView mav = new ModelAndView();
        mav.addObject("name","张");
        mav.addObject("age", 35);
        mav.setViewName("demo/view");
        return mav;
    }

    //方式二
    //该方法较为常用，调用该方法时，框架将一个自动构建好的Model对象作为参数传入方法，该方法返回String为路由，指向该Model要使用的View
    @RequestMapping(path = "/school", method = RequestMethod.GET)
    public  String getSchool(Model model){
        model.addAttribute("name", "清华大学");
        model.addAttribute("age","1");
        return "/demo/view";
    }

    //响应json数据（异步请求）
    //Java对象 -> json字符串 -> JS对象
    @RequestMapping(path = "/emp", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> getEmp(){
        Map<String, Object> map = new HashMap<>();
        map.put("name","wzf");
        map.put("age", 24);
        return map;
    }

    //服务器设置一个Cookie，返回给浏览器
    @RequestMapping(path = "/cookie/set", method = RequestMethod.GET)
    @ResponseBody
    public String setCookie(HttpServletResponse response){
        //创建cookie
        Cookie cookie = new Cookie("code", CommunityUtil.generateUUID());
        //设置Cookie生效范围
        cookie.setPath("/community/alpha");
        //设置Cookie的生存时间
        cookie.setMaxAge(60 * 10);
        response.addCookie(cookie);
        return "set Cookie";
    }

    //服务器从浏览器获取访问请求中的Cookie
    @RequestMapping(path = "/cookie/get", method = RequestMethod.GET)
    @ResponseBody
    public String getCookie(@CookieValue("code") String code){
        System.out.println(code);
        return "get Cookie";
    }

    //服务器为当前会话建立一个Session
    @RequestMapping(path = "/session/set", method = RequestMethod.GET)
    @ResponseBody
    public String setCookie(HttpSession session){

        session.setAttribute("id", 1);
        session.setAttribute("name", "Test");
        return "set Session";
    }

    //Session保存在服务器端，因此直接从Session中取值即可
    @RequestMapping(path = "/session/get", method = RequestMethod.GET)
    @ResponseBody
    public String getCookie(HttpSession session){
        System.out.println(session.getAttribute("id"));
        System.out.println(session.getAttribute("name"));
        return "get Session";
    }
}
