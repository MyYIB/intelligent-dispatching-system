package com.example.intelligentdispatchingsystem.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.intelligentdispatchingsystem.entity.info.WorkOrders;

import java.util.List;

public interface IWorkOrdersService extends IService<WorkOrders> {
    
    // 根据用户ID获取工单列表
    List<WorkOrders> getOrdersByUserId(Integer userId);
    
    // 根据员工ID获取工单列表
    List<WorkOrders> getOrdersByEmployeeId(Integer employeeId);
}
