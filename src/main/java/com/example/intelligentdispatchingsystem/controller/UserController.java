package com.example.intelligentdispatchingsystem.controller;

import com.example.intelligentdispatchingsystem.entity.User;
import com.example.intelligentdispatchingsystem.service.IUserService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;

import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author lm
 * @since 2025-03-13
 */
@RestController
@RequestMapping("/user")
public class UserController {
    @Resource
    private IUserService userService;

    // 查询所有用户
    @GetMapping("/all")
    public List<User> getAllUsers() {
        return userService.list();
    }

    // 添加用户
    @PostMapping("/add")
    public boolean addUser(@RequestBody User user) {
        return userService.save(user);
    }
}
