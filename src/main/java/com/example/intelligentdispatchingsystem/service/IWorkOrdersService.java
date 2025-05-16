package com.example.intelligentdispatchingsystem.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.intelligentdispatchingsystem.entity.info.WorkOrders;
import com.example.intelligentdispatchingsystem.entity.role.User;

import java.util.List;

public interface IWorkOrdersService extends IService<WorkOrders> {
    
    // 根据用户ID获取工单列表
    List<WorkOrders> getOrdersByUserId(Integer userId);
    
    // 根据员工ID获取工单列表
    List<WorkOrders> getOrdersByEmployeeId(Integer employeeId);
    // 分配工单给员工
    boolean assignOrderToEmployee(Integer orderId, Integer employeeId);
    // 根据工单号获取用户
    User getUserByOrderId(Integer orderId);
    // 添加智能派单方法
    Integer intelligentAssignOrder(WorkOrders workOrder);
}
