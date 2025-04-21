package com.example.intelligentdispatchingsystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.intelligentdispatchingsystem.entity.info.WorkOrders;
import com.example.intelligentdispatchingsystem.mapper.WorkOrdersMapper;
import com.example.intelligentdispatchingsystem.service.IWorkOrdersService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WorkOrdersServiceImpl extends ServiceImpl<WorkOrdersMapper, WorkOrders> implements IWorkOrdersService {
    
    @Override
    public List<WorkOrders> getOrdersByUserId(Integer userId) {
        QueryWrapper<WorkOrders> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        queryWrapper.orderByDesc("created_at");  // 按创建时间降序排列
        return this.list(queryWrapper);
    }
    
    @Override
    public List<WorkOrders> getOrdersByEmployeeId(Integer employeeId) {
        QueryWrapper<WorkOrders> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("assigned_employee", employeeId);
        queryWrapper.orderByDesc("created_at");  // 按创建时间降序排列
        return this.list(queryWrapper);
    }
}
