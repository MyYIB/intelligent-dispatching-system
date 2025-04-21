package com.example.intelligentdispatchingsystem.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.intelligentdispatchingsystem.common.ServerResponse;
import com.example.intelligentdispatchingsystem.entity.info.WorkOrders;
import com.example.intelligentdispatchingsystem.entity.role.User;
import com.example.intelligentdispatchingsystem.service.IWorkOrdersService;
import com.example.intelligentdispatchingsystem.entity.info.DispatchRecords;
import com.example.intelligentdispatchingsystem.service.IDispatchRecordsService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/order")
public class OrderController {
    @Resource
    public IWorkOrdersService workOrdersService;
    
    @Resource
    public IDispatchRecordsService dispatchRecordsService;

    @PostMapping("/create")
    public ServerResponse<Object> create(@RequestBody WorkOrders workOrders) {
        workOrders.setPriority("med");
        if(workOrdersService.save(workOrders)){
            return ServerResponse.createBySuccessMsg("创建成功");
        }else{
            return ServerResponse.createError("创建失败");
        }
    }
    
    @GetMapping("/user/list")
    public ServerResponse<List<WorkOrders>> getUserOrders(@RequestParam Integer userId) {
        List<WorkOrders> list = workOrdersService.getOrdersByUserId(userId);
        if (list != null && !list.isEmpty()) {
            return ServerResponse.createSuccess(list);
        } else {
            return ServerResponse.createBySuccessMsg("暂无数据");
        }
    }

    @GetMapping("/detail")
    public ServerResponse<WorkOrders> getOrderDetail(@RequestParam Integer orderId) {
        QueryWrapper<WorkOrders> query=new QueryWrapper<>();
        query.eq("order_id", orderId);
        WorkOrders workOrders = workOrdersService.getOne(query);
        if (workOrders != null ) {
            return ServerResponse.createSuccess(workOrders);
        } else {
            return ServerResponse.createBySuccessMsg("暂无该订单数据");
        }
    }
    
    // 获取员工的工单列表
    @GetMapping("/employee/list")
    public ServerResponse<List<WorkOrders>> getEmployeeOrders(@RequestParam Integer employeeId) {
        List<WorkOrders> list = workOrdersService.getOrdersByEmployeeId(employeeId);
        if (list != null && !list.isEmpty()) {
            return ServerResponse.createSuccess(list);
        } else {
            return ServerResponse.createBySuccessMsg("暂无数据");
        }
    }
    
    // 分配工单给员工
    @PostMapping("/assign")
    public ServerResponse<Object> assignOrder(@RequestBody Map<String, Object> params) {
        Integer orderId = (Integer) params.get("orderId");
        Integer employeeId = (Integer) params.get("employeeId");
        
        // 更新工单状态
        WorkOrders workOrder = workOrdersService.getById(orderId);
        if (workOrder == null) {
            return ServerResponse.createError("工单不存在");
        }
        
        workOrder.setStatus("assigned");
        workOrder.setAssignedEmployee(employeeId);
        
        // 创建派单记录
        DispatchRecords dispatchRecord = new DispatchRecords();
        dispatchRecord.setOrderId(orderId);
        dispatchRecord.setEmployeeId(employeeId);
        dispatchRecord.setDispatchTime(LocalDateTime.now());
        
        // 保存更改
        boolean updateResult = workOrdersService.updateById(workOrder);
        boolean saveResult = dispatchRecordsService.save(dispatchRecord);
        
        if (updateResult && saveResult) {
            return ServerResponse.createBySuccessMsg("工单分配成功");
        } else {
            return ServerResponse.createError("工单分配失败");
        }
    }
    
    // 更新工单状态
    @PostMapping("/update-status")
    public ServerResponse<Object> updateOrderStatus(@RequestBody Map<String, Object> params) {
        Integer orderId = (Integer) params.get("orderId");
        String status = (String) params.get("status");
        
        WorkOrders workOrder = workOrdersService.getById(orderId);
        if (workOrder == null) {
            return ServerResponse.createError("工单不存在");
        }
        
        workOrder.setStatus(status);
        
        // 如果状态是已完成，更新解决时间和派单记录的完成时间
        if ("completed".equals(status)) {
            LocalDateTime now = LocalDateTime.now();
            workOrder.setResolvedAt(now);
            
            // 更新派单记录的完成时间
            QueryWrapper<DispatchRecords> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("order_id", orderId);
            DispatchRecords dispatchRecord = dispatchRecordsService.getOne(queryWrapper);
            if (dispatchRecord != null) {
                dispatchRecord.setCompletionTime(now);
                dispatchRecordsService.updateById(dispatchRecord);
            }
        }
        
        if (workOrdersService.updateById(workOrder)) {
            return ServerResponse.createBySuccessMsg("状态更新成功");
        } else {
            return ServerResponse.createError("状态更新失败");
        }
    }
    
    // 取消工单
    @PostMapping("/cancel")
    public ServerResponse<Object> cancelOrder(@RequestParam Integer orderId) {
        WorkOrders workOrder = workOrdersService.getById(orderId);
        if (workOrder == null) {
            return ServerResponse.createError("工单不存在");
        }
        
        // 只有待处理状态的工单可以取消
        if (!"pending".equals(workOrder.getStatus())) {
            return ServerResponse.createError("只有待处理的工单可以取消");
        }
        
        workOrder.setStatus("closed");
        
        if (workOrdersService.updateById(workOrder)) {
            return ServerResponse.createBySuccessMsg("工单已取消");
        } else {
            return ServerResponse.createError("取消失败");
        }
    }
    
    // 获取所有待处理的工单（用于管理员分配）
    @GetMapping("/pending")
    public ServerResponse<List<WorkOrders>> getPendingOrders() {
        QueryWrapper<WorkOrders> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", "pending");
        queryWrapper.orderByDesc("priority");  // 按优先级降序排列
        
        List<WorkOrders> list = workOrdersService.list(queryWrapper);
        if (list != null && !list.isEmpty()) {
            return ServerResponse.createSuccess(list);
        } else {
            return ServerResponse.createBySuccessMsg("暂无待处理工单");
        }
    }
}
