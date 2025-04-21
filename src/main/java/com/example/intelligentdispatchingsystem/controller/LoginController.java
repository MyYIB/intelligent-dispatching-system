package com.example.intelligentdispatchingsystem.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.intelligentdispatchingsystem.common.ServerResponse;
import com.example.intelligentdispatchingsystem.entity.role.Employee;
import com.example.intelligentdispatchingsystem.entity.role.User;
import com.example.intelligentdispatchingsystem.mapper.UserMapper;
import com.example.intelligentdispatchingsystem.service.IEmployeeService;
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
    private IEmployeeService employeeService;
    @PostMapping( "/login")
    public ServerResponse<Object> login(@RequestBody User reqUser) {
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
                    if(user.getRole().equals("technician")){

                        QueryWrapper<Employee> employeeQuery=new QueryWrapper<>();
                        employeeQuery.eq("user_id",user.getUserId());
                        Employee employee=employeeService.getOne(employeeQuery);
                        user.setEmployeeId(employee.getEmployeeId());
                        System.out.println(employee.getEmployeeId());
                    }
                    loginVO.setUser(user);
                    return ServerResponse.createSuccess(loginVO);
                }else {
                    return ServerResponse.createError("密码错误");
                }
            }else {
                return ServerResponse.createError("用户账号错误");
            }
        }else {
            return ServerResponse.createError("未找到该用户");
        }
    }
    @PostMapping("/register")
    public ServerResponse<Object> register(@RequestBody User reqUser) {
        if (userService.save(reqUser)) {
            return ServerResponse.createBySuccessMsg("注册成功");
        }else {
            return ServerResponse.createError("注册失败");
        }
    }
}
