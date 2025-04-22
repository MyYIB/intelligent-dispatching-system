package com.example.intelligentdispatchingsystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.intelligentdispatchingsystem.entity.info.DispatchRecords;
import com.example.intelligentdispatchingsystem.entity.info.WorkOrders;
import com.example.intelligentdispatchingsystem.entity.role.Employee;
import com.example.intelligentdispatchingsystem.mapper.WorkOrdersMapper;
import com.example.intelligentdispatchingsystem.service.IDispatchRecordsService;
import com.example.intelligentdispatchingsystem.service.IEmployeeService;
import com.example.intelligentdispatchingsystem.service.IWorkOrdersService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class WorkOrdersServiceImpl extends ServiceImpl<WorkOrdersMapper, WorkOrders> implements IWorkOrdersService {
    @Resource
    private IEmployeeService employeeService;

    @Resource
    private IDispatchRecordsService dispatchRecordsService;

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
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean assignOrderToEmployee(Integer orderId, Integer employeeId) {
        try {
            // 1. 更新工单状态
            WorkOrders workOrder = this.getById(orderId);
            workOrder.setStatus("assigned");
            workOrder.setAssignedEmployee(employeeId);
            boolean updateResult = this.updateById(workOrder);
            
            if (!updateResult) {
                return false;
            }
            
            // 2. 创建派单记录
            DispatchRecords dispatchRecord = new DispatchRecords();
            dispatchRecord.setOrderId(orderId);
            dispatchRecord.setEmployeeId(employeeId);
            dispatchRecord.setDispatchTime(LocalDateTime.now());
            boolean saveResult = dispatchRecordsService.save(dispatchRecord);
            
            if (!saveResult) {
                throw new RuntimeException("保存派单记录失败");
            }
            
            // 3. 更新员工工作负载
            Employee employee = employeeService.getById(employeeId);
            employee.setCurrentWorkload(employee.getCurrentWorkload() + 1);
            boolean updateEmployeeResult = employeeService.updateById(employee);
            
            if (!updateEmployeeResult) {
                throw new RuntimeException("更新员工工作负载失败");
            }
            
            return true;
        } catch (Exception e) {
            log.error("分配工单失败", e);
            throw new RuntimeException("分配工单失败: " + e.getMessage());
        }
    }
}
