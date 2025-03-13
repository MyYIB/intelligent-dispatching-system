package com.example.intelligentdispatchingsystem.service.impl;

import com.example.intelligentdispatchingsystem.entity.Employee;
import com.example.intelligentdispatchingsystem.mapper.EmployeeMapper;
import com.example.intelligentdispatchingsystem.service.IEmployeeService;
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
public class EmployeeServiceImpl extends ServiceImpl<EmployeeMapper, Employee> implements IEmployeeService {

}
