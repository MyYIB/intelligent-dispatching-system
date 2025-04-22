package com.example.intelligentdispatchingsystem.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.intelligentdispatchingsystem.common.ServerResponse;
import com.example.intelligentdispatchingsystem.entity.info.WorkOrders;
import com.example.intelligentdispatchingsystem.entity.role.Employee;
import com.example.intelligentdispatchingsystem.entity.role.User;
import com.example.intelligentdispatchingsystem.service.IWorkOrdersService;
import com.example.intelligentdispatchingsystem.service.IUserService;
import com.example.intelligentdispatchingsystem.service.IEmployeeService;
import com.example.intelligentdispatchingsystem.entity.info.DispatchRecords;
import com.example.intelligentdispatchingsystem.service.IDispatchRecordsService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/order")
public class OrderController {
    @Resource
    public IWorkOrdersService workOrdersService;
    
    @Resource
    public IDispatchRecordsService dispatchRecordsService;
    
    @Resource
    public IUserService userService;
    
    @Resource
    public IEmployeeService employeeService;

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
        
        // 检查员工是否存在且可用
        Employee employee = employeeService.getById(employeeId);
        if (employee == null) {
            return ServerResponse.createError("技术员不存在");
        }
        
        if (!"available".equals(employee.getStatus())) {
            return ServerResponse.createError("该技术员当前不可用");
        }
        
        if (employee.getCurrentWorkload() >= employee.getMaxWorkload()) {
            return ServerResponse.createError("该技术员工作负载已满");
        }
        
        // 开始事务处理
        boolean success = workOrdersService.assignOrderToEmployee(orderId, employeeId);
        
        if (success) {
            return ServerResponse.createBySuccessMsg("工单分配成功");
        } else {
            return ServerResponse.createError("工单分配失败");
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
    
    @GetMapping("/repair/page")
    public Map<String, Object> selectPage(@RequestParam int pageNum,
                                          @RequestParam int pageSize,
                                          @RequestParam(required = false) String customerName,
                                          @RequestParam(required = false) String employeeName,
                                          @RequestParam(required = false) String status
    ) {
        Page<WorkOrders> page = new Page<>(pageNum, pageSize);
        Map<String, Object> res = new HashMap<>();
        
        // 使用QueryWrapper构建查询条件
        QueryWrapper<WorkOrders> queryWrapper = new QueryWrapper<>();
        
        // 添加状态筛选条件
        if (status != null && !status.isEmpty() && !"all".equals(status)) {
            queryWrapper.eq("status", status);
        }
        
        // 按创建时间降序排序
        queryWrapper.orderByDesc("created_at");
        
        // 执行分页查询
        IPage<WorkOrders> orderPage = workOrdersService.page(page, queryWrapper);
        
        // 处理结果，转换为前端需要的格式
        List<Map<String, Object>> records = new ArrayList<>();
        for (WorkOrders order : orderPage.getRecords()) {
            Map<String, Object> map = new HashMap<>();
            map.put("orderId", order.getOrderId());
            map.put("description", order.getDescription());
            map.put("status", order.getStatus());
            map.put("address", order.getLocation());
            map.put("createdAt", order.getCreatedAt());
            map.put("resolveAt", order.getResolvedAt());
            map.put("repairType", order.getRepairType());
            // 获取用户信息
            if (order.getUserId() != null) {
                // 如果有用户名筛选条件，检查是否匹配
                User user = userService.getById(order.getUserId());
                if (user != null) {
                    if (customerName != null && !customerName.isEmpty() && 
                        !user.getUsername().contains(customerName)) {
                        continue; // 不匹配用户名条件，跳过此记录
                    }
                    map.put("customerName", user.getUsername());
                    map.put("customerPhone", user.getPhone());
                }
            }
            
            // 获取员工信息
            if (order.getAssignedEmployee() != null) {
                // 如果有员工名筛选条件，检查是否匹配
                Employee employee = employeeService.getById(order.getAssignedEmployee());
                if (employee != null) {
                    if (employeeName != null && !employeeName.isEmpty() && 
                        !employee.getName().contains(employeeName)) {
                        continue; // 不匹配员工名条件，跳过此记录
                    }
                    map.put("employeeName", employee.getName());
                }
            } else if (employeeName != null && !employeeName.isEmpty()) {
                continue; // 有员工名筛选但工单未分配员工，跳过
            }
            
            records.add(map);
        }
        
        res.put("data", records);
        res.put("total", records.size()); // 重新计算过滤后的总数
        res.put("status", 200);
        res.put("msg", "查询成功");
        return res;
    }
    
    // 更新工单状态
    @PostMapping("/updateStatus")
    public ServerResponse<Object> updateOrderStatus(@RequestBody Map<String, Object> params) {
        Integer orderId = (Integer) params.get("orderId");
        String status = (String) params.get("status");
        
        WorkOrders workOrder = workOrdersService.getById(orderId);
        if (workOrder == null) {
            return ServerResponse.createError("工单不存在");
        }
        
        workOrder.setStatus(status);
        
        // 如果状态是已完成，设置解决时间
        if ("completed".equals(status)) {
            workOrder.setResolvedAt(LocalDateTime.now());
        }
        
        if (workOrdersService.updateById(workOrder)) {
            return ServerResponse.createBySuccessMsg("工单状态更新成功");
        } else {
            return ServerResponse.createError("状态更新失败");
        }
    }
    
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
