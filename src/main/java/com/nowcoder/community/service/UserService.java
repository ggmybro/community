package com.nowcoder.community.service;

import com.nowcoder.community.dao.LoginTicketMapper;
import com.nowcoder.community.dao.UserMapper;
import com.nowcoder.community.entity.LoginTicket;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.MailClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class UserService implements CommunityConstant {

    @Autowired
    private MailClient mailClient;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private LoginTicketMapper loginTicketMapper;

    public User findUserById(int userId){
        return userMapper.selectById(userId);
    }

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private TemplateEngine templateEngine;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    public Map<String, Object> register(User user){
        HashMap<String, Object> map = new HashMap<>();
        //参数空值处理
        if(user == null){
            throw new IllegalArgumentException("参数不能为空");
        }
        if(StringUtils.isBlank(user.getUsername())){
            map.put("usernameMsg", "用户名不能为空");
            return map;
        }
        if(StringUtils.isBlank(user.getPassword())){
            map.put("passwordMsg", "密码不能为空");
        }
        if(StringUtils.isBlank(user.getEmail())){
            map.put("usermailMsg", "邮箱不能为空");
            return map;
        }

        //验证账号
        if(userMapper.selectByName(user.getUsername()) != null){
            map.put("usernameMsg", "该用户名已被注册");
            return map;
        }
        //验证邮箱
        if(userMapper.selectByEmail(user.getEmail()) != null){
            map.put("usermailMsg", "该邮箱已被注册");
            return map;
        }

        //验证通过，将该用户账号保存至数据库
        String salt = CommunityUtil.generateUUID().substring(0, 6);
        user.setSalt(salt);
        user.setPassword(CommunityUtil.md5(user.getPassword() + salt));
        user.setStatus(0);
        user.setType(0);
        user.setActivationCode(CommunityUtil.generateUUID());
        user.setCreateTime(new Date());
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png", new Random().nextInt(1000)));
        userMapper.insertUser(user);

        //激活邮件
        //通过模板引擎TemplateEngine调用process方法，输入模板路径来处理Context，返回生成的Html的路径
        Context context = new Context();
        context.setVariable("usermail", user.getEmail());
        // http://localhost:8080/community/activation/${userId}/activationCode
        String url = domain + contextPath + "/activation/" + user.getId() + "/" + user.getActivationCode();
        context.setVariable("url", url);
        String content = templateEngine.process("/mail/activation", context);
        mailClient.senMail(user.getEmail(), "账号激活", content);

        return map;
    }

    public int activation(int userId, String code){

        User user = userMapper.selectById(userId);
        if(user.getStatus() == 1){
            return ACTIVATION_REPEAT;
        } else if (code.equals(user.getActivationCode())) {
            userMapper.updateStatus(userId, 1);
            return ACTIVATION_SUCCESS;
        }else{
            return ACTIVATION_FAILED;
        }
    }

    //用户登录
    public Map<String, Object> login(String username, String password, int expiredSeconds){
        Map<String, Object> map = new HashMap<>();

        //空值处理
        if(StringUtils.isBlank(username)){
            map.put("usernameMsg", "用户名不能为空！");
            return map;
        }
        if(StringUtils.isBlank(password)){
            map.put("passwordMsg", "密码不能为空！");
            return map;
        }
        //账号验证
        User user = userMapper.selectByName(username);
        if(user == null){
            map.put("usernameMsg", "账号不存在！");
            return map;
        }
        if(user.getStatus() == 0){
            map.put("usernameMsg", "账号未激活！");
            return map;
        }
        if(!user.getPassword().equals(CommunityUtil.md5(password + user.getSalt()))){
            map.put("passwordMsg", "密码错误！");
            return map;
        }
        //账号通过验证，为账号设置登录凭证
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getId());
        loginTicket.setStatus(0);
        loginTicket.setTicket(CommunityUtil.generateUUID());
        loginTicket.setExpired(new Date(System.currentTimeMillis() + expiredSeconds * 1000));
        loginTicketMapper.insertLoginTicket(loginTicket);
        map.put("ticket", loginTicket.getTicket());
        return map;
    }

    //用户退出
    public void logout(String ticket){
        int i = loginTicketMapper.updateStatus(ticket, 1);
    }

    //重置用户密码
    public Map<String, Object> resetPassowrd(String usermail, String password){
        HashMap<String, Object> map = new HashMap<>();
        //空值处理
        if(StringUtils.isBlank(usermail)){
            map.put("usermailMsg", "邮箱不能为空！");
            return map;
        }
        if(StringUtils.isBlank(password)){
            map.put("passwordMsg", "密码不能为空！");
            return map;
        }
        User user = userMapper.selectByEmail(usermail);
        if(user == null){
            map.put("usermailMsg", "该邮箱未注册！");
            return map;
        }
        password = CommunityUtil.md5(password + user.getSalt());
        userMapper.updatePassword(user.getId(), password);
        map.put("user", user);
        return map;
    }

    //验证用户邮箱状态
    public int verifyUserMail(String usermail){
        User user = userMapper.selectByEmail(usermail);
        if(user == null){
            return MAIL_UNREGISTERED;
        }
        if(user.getStatus() != 1){
            return MAIL_UNACTIVATED;
        }
        return MAIL_ACTIVATED;
    }

    //根据凭证名ticket获取凭证LoginTicket
    public LoginTicket findLoginTicket(String ticket){
        return loginTicketMapper.selectByTicket(ticket);
    }

    //上传头像后更新数据库中用户头像路径
    public void updateHeaderUrl(int userId, String headerUrl){
        userMapper.updateHeader(userId, headerUrl);
    }
}
