package com.example.intelligentdispatchingsystem.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.intelligentdispatchingsystem.entity.role.Employee;
import com.example.intelligentdispatchingsystem.entity.info.EquipmentMaintenance;
import com.example.intelligentdispatchingsystem.entity.info.Inventory;
import com.example.intelligentdispatchingsystem.entity.role.User;
import com.example.intelligentdispatchingsystem.entity.info.WorkOrders;
import com.example.intelligentdispatchingsystem.entity.info.FeedbackRecords;
import com.example.intelligentdispatchingsystem.service.IEmployeeService;
import com.example.intelligentdispatchingsystem.service.IEquipmentMaintenanceService;
import com.example.intelligentdispatchingsystem.service.IInventoryService;
import com.example.intelligentdispatchingsystem.service.IUserService;
import com.example.intelligentdispatchingsystem.service.IWorkOrdersService;
import com.example.intelligentdispatchingsystem.service.IFeedbackRecordsService;
import com.example.intelligentdispatchingsystem.common.ServerResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 数据可视化控制器
 */
@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    @Autowired
    private IWorkOrdersService workOrdersService;

    @Autowired
    private IUserService userService;

    @Autowired
    private IEmployeeService employeeService;

    @Autowired
    private IInventoryService inventoryService;

    @Autowired
    private IEquipmentMaintenanceService equipmentMaintenanceService;

    @Autowired
    private IFeedbackRecordsService feedbackRecordsService;

    /**
     * 获取统计数据
     */
    @GetMapping("/stats")
    public ServerResponse<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();

        // 获取工单总数
        int totalOrders = (int) workOrdersService.count();
        stats.put("totalOrders", totalOrders);

        // 获取待处理工单数
        QueryWrapper<WorkOrders> pendingOrdersQuery = new QueryWrapper<>();
        pendingOrdersQuery.in("status", Arrays.asList("pending", "assigned", "in_progress"));
        int pendingOrders = (int) workOrdersService.count(pendingOrdersQuery);
        stats.put("pendingOrders", pendingOrders);

        // 获取已完成工单数
        QueryWrapper<WorkOrders> completedOrdersQuery = new QueryWrapper<>();
        completedOrdersQuery.in("status", Arrays.asList("completed", "closed"));
        int completedOrders = (int) workOrdersService.count(completedOrdersQuery);
        stats.put("completedOrders", completedOrders);

        // 获取用户总数
        int totalUsers = (int) userService.count();
        stats.put("totalUsers", totalUsers);

        // 获取员工总数
        int totalEmployees = (int) employeeService.count();
        stats.put("totalEmployees", totalEmployees);

        // 获取库存总量
        int totalInventory = 0;
        List<Inventory> inventoryList = inventoryService.list();
        for (Inventory item : inventoryList) {
            totalInventory += item.getQuantity();
        }
        stats.put("totalInventory", totalInventory);

        // 获取低库存物品数量
        int lowStockItems = 0;
        // 定义低库存阈值
        final int LOW_STOCK_THRESHOLD = 10;
        for (Inventory item : inventoryList) {
            // 使用固定阈值判断低库存
            if (item.getQuantity() < LOW_STOCK_THRESHOLD) {
                lowStockItems++;
            }
        }
        stats.put("lowStockItems", lowStockItems);

        // 获取平均满意度
        QueryWrapper<FeedbackRecords> feedbackQuery = new QueryWrapper<>();
        feedbackQuery.isNotNull("satisfaction_score");
        List<FeedbackRecords> feedbackList = feedbackRecordsService.list(feedbackQuery);
        double averageSatisfaction = 0;
        if (!feedbackList.isEmpty()) {
            double totalScore = 0;
            for (FeedbackRecords feedback : feedbackList) {
                totalScore += feedback.getSatisfactionScore();
            }
            averageSatisfaction = totalScore / feedbackList.size();
        }
        stats.put("averageSatisfaction", averageSatisfaction);

        // 获取待处理维护数量
        QueryWrapper<EquipmentMaintenance> maintenanceQuery = new QueryWrapper<>();
        maintenanceQuery.in("status", Arrays.asList("pending", "in_progress"));
        int pendingMaintenance = (int) equipmentMaintenanceService.count(maintenanceQuery);
        stats.put("pendingMaintenance", pendingMaintenance);

        return ServerResponse.createSuccess(stats);
    }

    /**
     * 获取工单类型分布
     */
    @GetMapping("/orderTypeDistribution")
    public ServerResponse<List<Map<String, Object>>> getOrderTypeDistribution() {
        List<Map<String, Object>> result = new ArrayList<>();
        
        // 获取所有工单
        List<WorkOrders> ordersList = workOrdersService.list();
        
        // 统计各类型工单数量
        Map<String, Integer> typeCountMap = new HashMap<>();
        for (WorkOrders order : ordersList) {
            String orderType = order.getOrderType();
            typeCountMap.put(orderType, typeCountMap.getOrDefault(orderType, 0) + 1);
        }
        
        // 转换为前端需要的格式
        for (Map.Entry<String, Integer> entry : typeCountMap.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            item.put("name", getOrderTypeName(entry.getKey()));
            item.put("value", entry.getValue());
            result.add(item);
        }
        
        return ServerResponse.createSuccess(result);
    }

    /**
     * 获取工单状态分布
     */
    @GetMapping("/orderStatusDistribution")
    public ServerResponse<List<Map<String, Object>>> getOrderStatusDistribution() {
        List<Map<String, Object>> result = new ArrayList<>();
        
        // 获取所有工单
        List<WorkOrders> ordersList = workOrdersService.list();
        
        // 统计各状态工单数量
        Map<String, Integer> statusCountMap = new HashMap<>();
        for (WorkOrders order : ordersList) {
            String status = order.getStatus();
            statusCountMap.put(status, statusCountMap.getOrDefault(status, 0) + 1);
        }
        
        // 转换为前端需要的格式
        for (Map.Entry<String, Integer> entry : statusCountMap.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            item.put("name", getStatusName(entry.getKey()));
            item.put("value", entry.getValue());
            result.add(item);
        }
        
        return ServerResponse.createSuccess(result);
    }

    /**
     * 获取满意度分布
     */
    @GetMapping("/satisfactionDistribution")
    public ServerResponse<List<Map<String, Object>>> getSatisfactionDistribution() {
        List<Map<String, Object>> result = new ArrayList<>();
        
        // 获取所有有评分的回访记录
        QueryWrapper<FeedbackRecords> feedbackQuery = new QueryWrapper<>();
        feedbackQuery.isNotNull("satisfaction_score");
        List<FeedbackRecords> feedbackList = feedbackRecordsService.list(feedbackQuery);
        
        // 统计各评分数量
        Map<Integer, Integer> scoreCountMap = new HashMap<>();
        for (FeedbackRecords feedback : feedbackList) {
            Integer score = feedback.getSatisfactionScore();
            scoreCountMap.put(score, scoreCountMap.getOrDefault(score, 0) + 1);
        }
        
        // 转换为前端需要的格式
        for (int i = 1; i <= 5; i++) {
            Map<String, Object> item = new HashMap<>();
            item.put("score", i);
            item.put("count", scoreCountMap.getOrDefault(i, 0));
            result.add(item);
        }
        
        return ServerResponse.createSuccess(result);
    }

    /**
     * 获取员工工作负载
     */
    @GetMapping("/employeeWorkload")
    public ServerResponse<List<Map<String, Object>>> getEmployeeWorkload() {
        List<Map<String, Object>> result = new ArrayList<>();
        
        // 获取所有员工
        List<Employee> employeeList = employeeService.list();
        
        // 获取所有进行中的工单
        QueryWrapper<WorkOrders> orderQuery = new QueryWrapper<>();
        orderQuery.in("status", Arrays.asList("assigned", "in_progress"));
        List<WorkOrders> activeOrdersList = workOrdersService.list(orderQuery);
        
        // 统计每个员工的工作负载
        Map<Integer, Integer> employeeWorkloadMap = new HashMap<>();
        for (WorkOrders order : activeOrdersList) {
            Integer employeeId = order.getAssignedEmployee();
            if (employeeId != null) {
                employeeWorkloadMap.put(employeeId, employeeWorkloadMap.getOrDefault(employeeId, 0) + 1);
            }
        }
        
        // 转换为前端需要的格式
        for (Employee employee : employeeList) {
            Map<String, Object> item = new HashMap<>();
            item.put("name", employee.getName());
            item.put("current", employeeWorkloadMap.getOrDefault(employee.getEmployeeId(), 0));
            item.put("max", employee.getMaxWorkload() != null ? employee.getMaxWorkload() : 10);
            result.add(item);
        }
        
        return ServerResponse.createSuccess(result);
    }

    /**
     * 获取工单类型名称
     */
    private String getOrderTypeName(String orderType) {
        switch (orderType) {
            case "installation":
                return "安装";
            case "repair":
                return "维修";
            case "upgrade":
                return "升级";
            case "consultation":
                return "咨询";
            case "complaint":
                return "投诉";
            default:
                return orderType;
        }
    }

    /**
     * 获取工单状态名称
     */
    private String getStatusName(String status) {
        switch (status) {
            case "pending":
                return "待处理";
            case "assigned":
                return "已分配";
            case "in_progress":
                return "处理中";
            case "completed":
                return "已完成";
            case "closed":
                return "已关闭";
            case "cancelled":
                return "已取消";
            default:
                return status;
        }
    }
}