package com.example.intelligentdispatchingsystem.service.impl;

import com.example.intelligentdispatchingsystem.entity.role.User;
import com.example.intelligentdispatchingsystem.mapper.UserMapper;
import com.example.intelligentdispatchingsystem.service.IUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author lm
 * @since 2025-03-13
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

}
