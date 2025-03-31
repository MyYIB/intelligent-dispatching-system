package com.example.intelligentdispatchingsystem.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.intelligentdispatchingsystem.common.R;
import com.example.intelligentdispatchingsystem.entity.User;
import com.example.intelligentdispatchingsystem.mapper.UserMapper;
import com.example.intelligentdispatchingsystem.service.IUserService;
import com.example.intelligentdispatchingsystem.vo.LoginVO;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/login")
public class LoginController {
    @Resource
    private IUserService userService;
    @Resource
    private UserMapper userMapper;
    @PostMapping( "/login")
    public R<Object> login(@RequestBody User reqUser) {
        QueryWrapper<User> query=new QueryWrapper<>();
        query.eq("account",reqUser.getAccount());
        User user = userService.getOne(query);
        if (user != null) {
            if (user.getAccount().equals(reqUser.getAccount())){
                if (user.getPassword().equals(reqUser.getPassword())){
                    LoginVO loginVO=new LoginVO();
                    loginVO.setId(user.getUserId());
                    //这里token直接用一个uuid
                    //使用jwt的情况下，会生成一个jwt token,jwt token里会包含用户的信息
                    loginVO.setToken(UUID.randomUUID().toString());
                    loginVO.setUser(user);
                    R r = new R();
                    r.setCode(200);
                    r.setData(loginVO);
                    return r;
                }else {
                    return R.error("用户密码错误");
                }
            }else {
                return R.error("用户账号错误");
            }
        }else {
            return R.error("未找到该用户");
        }
    }

}
