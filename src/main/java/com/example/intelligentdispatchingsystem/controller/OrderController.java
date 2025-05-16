package com.example.intelligentdispatchingsystem.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.intelligentdispatchingsystem.common.ServerResponse;
import com.example.intelligentdispatchingsystem.entity.info.EquipmentMaintenance;
import com.example.intelligentdispatchingsystem.entity.info.FeedbackRecords;
import com.example.intelligentdispatchingsystem.entity.info.WorkOrders;
import com.example.intelligentdispatchingsystem.entity.role.Employee;
import com.example.intelligentdispatchingsystem.entity.role.User;
import com.example.intelligentdispatchingsystem.service.*;
import com.example.intelligentdispatchingsystem.entity.info.DispatchRecords;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

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

    @Resource
    public IFeedbackRecordsService feedbackRecordsService;

    @Resource IEquipmentMaintenanceService equipmentMaintenanceService;

    @PostMapping("/create")
    public ServerResponse<Object> create(@RequestBody WorkOrders workOrders) {
        workOrders.setPriority("med");
        if(workOrdersService.save(workOrders)){
            // 创建工单成功后，尝试智能分配技术员
            ServerResponse<Object> assignResponse = autoAssignEmployee(workOrders.getOrderId());
            if(assignResponse.isSuccess()) {
                return ServerResponse.createBySuccessMsg("创建成功并已自动分配技术员");
            } else {
                return ServerResponse.createBySuccessMsg("创建成功，但未找到合适的技术员自动分配");
            }
        } else {
            return ServerResponse.createError("创建失败");
        }
    }
    
    // 智能派单方法
    private ServerResponse<Object> autoAssignEmployee(Integer orderId) {
        // 获取工单详情
        WorkOrders workOrder = workOrdersService.getById(orderId);
        if (workOrder == null) {
            return ServerResponse.createError("工单不存在");
        }
        
        // 调用智能派单服务
        Integer assignedEmployeeId = workOrdersService.intelligentAssignOrder(workOrder);
        
        if (assignedEmployeeId != null) {
            // 如果找到合适的技术员，执行分配
            boolean success = workOrdersService.assignOrderToEmployee(orderId, assignedEmployeeId);
            if (success) {
                System.out.println("1");
                return ServerResponse.createBySuccessMsg("自动分配技术员成功");
            } else {
                System.out.println("2");
                return ServerResponse.createError("自动分配技术员失败");
            }
        } else {
            System.out.println("3");
            return ServerResponse.createError("未找到合适的技术员");
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
        // 如果状态是已完成，设置解决时间，创建回访信息,增加员工经验
        if ("completed".equals(status)) {
            workOrder.setResolvedAt(LocalDateTime.now());
            FeedbackRecords feedbackRecords = new FeedbackRecords();
            feedbackRecords.setOrderId(orderId);
            feedbackRecords.setEmployeeId(workOrder.getAssignedEmployee());
            feedbackRecords.setNeedTime(LocalDateTime.now().plusDays(7));
            if(feedbackRecordsService.save(feedbackRecords)){
                System.out.println("创建回访信息");
            }
            if(employeeService.plusEmployeeLevelPoints(workOrder.getAssignedEmployee(),10.0)){
                System.out.println("增加员工经验");
            };
            if (employeeService.completeWork(workOrder.getAssignedEmployee())) {
                System.out.println("员工负载减一");
            }
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
    @GetMapping("/getDispatchEmployee")
    public ServerResponse<Employee> getDispatchEmployee(@RequestParam Integer orderId) {
        Employee employee = dispatchRecordsService.getDispatchEmployeesByOrderId(orderId);
        if(employee == null) {
            return ServerResponse.createError("未查询到分配员工");
        }else {
            return ServerResponse.createSuccess(employee);
        }
    }

    @GetMapping("/getUser")
    public ServerResponse<User> getUser(@RequestParam Integer orderId) {
        User user = workOrdersService.getUserByOrderId(orderId);
        if(user == null) {
            return ServerResponse.createError("未查询到工单用户信息");
        }else {
            return ServerResponse.createSuccess(user);
        }
    }

    // 获取工单回访信息
    @GetMapping("/getFeedback")
    public ServerResponse<FeedbackRecords> getFeedbackByOrderId(@RequestParam Integer orderId) {
        FeedbackRecords feedbackRecords = feedbackRecordsService.getFeedbackByOrderId(orderId);
        if(feedbackRecords == null) {
            return ServerResponse.createError("未查询到工单回访信息");
        }else {
            return ServerResponse.createSuccess(feedbackRecords);
        }
    }

    // 员工提交回访信息
    @PostMapping("/submitFeedback")
    public ServerResponse<Object> submitFeedback(@RequestBody Map<String, Object> params) {
        Integer orderId = (Integer) params.get("order_id");
        String feedbackState = (String) params.get("feedback_state");
        String feedbackContent = (String) params.get("feedback_content");
        LocalDateTime feedbackTime  = LocalDateTime.now();
        FeedbackRecords feedbackRecords = feedbackRecordsService.getFeedbackByOrderId(orderId);
        if(feedbackRecords == null) {
            return ServerResponse.createError("未查询到该工单回访信息");
        }
        feedbackRecords.setFeedbackState(feedbackState);
        feedbackRecords.setFeedbackTime(feedbackTime);
        feedbackRecords.setFeedbackText(feedbackContent);
        if (feedbackRecordsService.updateById(feedbackRecords)) {
            return ServerResponse.createBySuccessMsg("更新工单回访信息成功");
        }else {
            return ServerResponse.createError("更新回访信息失败");
        }
    }

    //用户提交回访评分
    @PostMapping("/submitFeedbackScore")
    public ServerResponse<Object> submitFeedbackScore(@RequestBody Map<String, Object> params) {
        Integer feedbackId = (Integer) params.get("feedback_id");
        Integer satisfactionScore = (Integer) params.get("satisfaction_score");
        FeedbackRecords feedbackRecord = feedbackRecordsService.getById(feedbackId);
        if(feedbackRecord == null) {
            return ServerResponse.createError("未查询到该工单回访信息");
        }
        feedbackRecord.setSatisfactionScore(satisfactionScore);
        feedbackRecord.setFeedbackState("completed");
        if(feedbackRecordsService.updateById(feedbackRecord)) {
            switch (feedbackRecord.getSatisfactionScore()){
                case 1:
                    if(employeeService.plusEmployeeLevelPoints(feedbackRecord.getEmployeeId(),1.0)){
                        System.out.println("增加员工经验1");
                    };
                    break;
                case 2:
                    if(employeeService.plusEmployeeLevelPoints(feedbackRecord.getEmployeeId(),2.0)){
                        System.out.println("增加员工经验2");
                    };
                    break;
                case 3:
                    if(employeeService.plusEmployeeLevelPoints(feedbackRecord.getEmployeeId(),3.0)){
                        System.out.println("增加员工经验2");
                    };
                    break;
                case 4:
                    if(employeeService.plusEmployeeLevelPoints(feedbackRecord.getEmployeeId(),4.0)){
                        System.out.println("增加员工经验4");
                    };
                    break;
                case 5:
                    if(employeeService.plusEmployeeLevelPoints(feedbackRecord.getEmployeeId(),5.0)){
                        System.out.println("增加员工经验5");
                    };
                    break;
                default:
                    break;
            }
            return ServerResponse.createBySuccessMsg("评分成功");
        }else {
            return ServerResponse.createError("评分失败");
        }
    }

    // 获取回访列表（分页）
    @GetMapping("/feedback/page")
    public Map<String, Object> getFeedbackPage(@RequestParam int pageNum,
                                               @RequestParam int pageSize,
                                               @RequestParam(required = false) String customerName,
                                               @RequestParam(required = false) String employeeName,
                                               @RequestParam(required = false) String status
    ) {
        Page<FeedbackRecords> page = new Page<>(pageNum, pageSize);
        Map<String, Object> res = new HashMap<>();

        // 使用QueryWrapper构建查询条件
        QueryWrapper<FeedbackRecords> queryWrapper = new QueryWrapper<>();

        // 添加状态筛选条件
        if (status != null && !status.isEmpty() && !"all".equals(status)) {
            queryWrapper.eq("feedback_state", status);
        }

        // 按待回访时间降序排序
        queryWrapper.orderByDesc("need_time");

        // 执行分页查询
        IPage<FeedbackRecords> feedbackPage = feedbackRecordsService.page(page, queryWrapper);

        // 处理结果，转换为前端需要的格式
        List<Map<String, Object>> records = new ArrayList<>();
        for (FeedbackRecords feedback : feedbackPage.getRecords()) {
            Map<String, Object> map = new HashMap<>();
            map.put("feedbackId", feedback.getFeedbackId());
            map.put("orderId", feedback.getOrderId());
            map.put("employeeId", feedback.getEmployeeId());
            map.put("feedbackState", feedback.getFeedbackState());
            map.put("feedbackText", feedback.getFeedbackText());
            map.put("satisfactionScore", feedback.getSatisfactionScore());
            map.put("feedbackTime", feedback.getFeedbackTime());
            map.put("needTime", feedback.getNeedTime());

            // 获取工单信息
            WorkOrders order = workOrdersService.getById(feedback.getOrderId());
            if (order != null) {
                map.put("orderDescription", order.getDescription());
                map.put("orderStatus", order.getStatus());
                map.put("orderType", order.getOrderType());
                map.put("orderCreatedAt", order.getCreatedAt());
                map.put("orderResolvedAt", order.getResolvedAt());

                // 获取用户信息
                if (order.getUserId() != null) {
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
            }

            // 获取员工信息
            if (feedback.getEmployeeId() != null) {
                Employee employee = employeeService.getById(feedback.getEmployeeId());
                if (employee != null) {
                    if (employeeName != null && !employeeName.isEmpty() &&
                            !employee.getName().contains(employeeName)) {
                        continue; // 不匹配员工名条件，跳过此记录
                    }
                    map.put("employeeName", employee.getName());
                    map.put("employeePhone", employee.getPhone());
                }
            } else if (employeeName != null && !employeeName.isEmpty()) {
                continue; // 有员工名筛选但回访未分配员工，跳过
            }

            records.add(map);
        }

        res.put("data", records);
        res.put("total", records.size()); // 重新计算过滤后的总数
        res.put("status", 200);
        res.put("msg", "查询成功");
        return res;
    }

    // 更新回访状态
    @PostMapping("/feedback/updateStatus")
    public ServerResponse<Object> updateFeedbackStatus(@RequestBody Map<String, Object> params) {
        Integer feedbackId = (Integer) params.get("feedbackId");
        String status = (String) params.get("status");

        FeedbackRecords feedback = feedbackRecordsService.getById(feedbackId);
        if (feedback == null) {
            return ServerResponse.createError("回访记录不存在");
        }

        feedback.setFeedbackState(status);

        // 如果状态是已完成回访，设置回访时间
        if ("unrated".equals(status)) {
            feedback.setFeedbackTime(LocalDateTime.now());
        }

        if (feedbackRecordsService.updateById(feedback)) {
            return ServerResponse.createBySuccessMsg("回访状态更新成功");
        } else {
            return ServerResponse.createError("状态更新失败");
        }
    }

    // 获取回访详情
    @GetMapping("/feedback/detail")
    public ServerResponse<Map<String, Object>> getFeedbackDetail(@RequestParam Integer feedbackId) {
        FeedbackRecords feedback = feedbackRecordsService.getById(feedbackId);
        if (feedback == null) {
            return ServerResponse.createError("回访记录不存在");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("feedbackId", feedback.getFeedbackId());
        result.put("orderId", feedback.getOrderId());
        result.put("employeeId", feedback.getEmployeeId());
        result.put("feedbackState", feedback.getFeedbackState());
        result.put("feedbackText", feedback.getFeedbackText());
        result.put("satisfactionScore", feedback.getSatisfactionScore());
        result.put("feedbackTime", feedback.getFeedbackTime());
        result.put("needTime", feedback.getNeedTime());

        // 获取工单信息
        WorkOrders order = workOrdersService.getById(feedback.getOrderId());
        if (order != null) {
            Map<String, Object> orderInfo = new HashMap<>();
            orderInfo.put("orderId", order.getOrderId());
            orderInfo.put("description", order.getDescription());
            orderInfo.put("status", order.getStatus());
            orderInfo.put("orderType", order.getOrderType());
            orderInfo.put("createdAt", order.getCreatedAt());
            orderInfo.put("resolvedAt", order.getResolvedAt());
            result.put("orderInfo", orderInfo);

            // 获取用户信息
            if (order.getUserId() != null) {
                User user = userService.getById(order.getUserId());
                if (user != null) {
                    result.put("customerName", user.getUsername());
                    result.put("customerPhone", user.getPhone());
                }
            }
        }

        // 获取员工信息
        if (feedback.getEmployeeId() != null) {
            Employee employee = employeeService.getById(feedback.getEmployeeId());
            if (employee != null) {
                result.put("employeeName", employee.getName());
                result.put("employeePhone", employee.getPhone());
            }
        }

        return ServerResponse.createSuccess(result);
    }

    // 提醒员工回访
    @PostMapping("/feedback/remind")
    public ServerResponse<Object> remindEmployee(@RequestBody Map<String, Object> params) {
        Integer feedbackId = (Integer) params.get("feedbackId");

        FeedbackRecords feedback = feedbackRecordsService.getById(feedbackId);
        if (feedback == null) {
            return ServerResponse.createError("回访记录不存在");
        }

        // 获取员工信息
        Employee employee = employeeService.getById(feedback.getEmployeeId());
        if (employee == null) {
            return ServerResponse.createError("员工不存在");
        }

        // 这里可以实现发送提醒的逻辑，比如发送短信、邮件或系统通知
        
        return ServerResponse.createBySuccessMsg("已成功提醒员工进行回访");
    }


    // 获取设备维护列表（分页）
    @GetMapping("/maintenance/page")
    public Map<String, Object> getMaintenancePage(@RequestParam int pageNum,
                                                  @RequestParam int pageSize,
                                                  @RequestParam(required = false) String equipmentName,
                                                  @RequestParam(required = false) String employeeName,
                                                  @RequestParam(required = false) String status
    ) {
        Page<EquipmentMaintenance> page = new Page<>(pageNum, pageSize);
        Map<String, Object> res = new HashMap<>();

        // 使用QueryWrapper构建查询条件
        QueryWrapper<EquipmentMaintenance> queryWrapper = new QueryWrapper<>();

        // 添加设备名称筛选条件
        if (equipmentName != null && !equipmentName.isEmpty()) {
            queryWrapper.like("equipment_name", equipmentName);
        }

        // 添加状态筛选条件
        if (status != null && !status.isEmpty() && !"all".equals(status)) {
            queryWrapper.eq("status", status);
        }

        // 按维护日期降序排序
        queryWrapper.orderByDesc("maintenance_date");

        // 执行分页查询
        IPage<EquipmentMaintenance> maintenancePage = equipmentMaintenanceService.page(page, queryWrapper);

        // 处理结果，转换为前端需要的格式
        List<Map<String, Object>> records = new ArrayList<>();
        for (EquipmentMaintenance maintenance : maintenancePage.getRecords()) {
            Map<String, Object> map = new HashMap<>();
            map.put("maintenanceId", maintenance.getMaintenanceId());
            map.put("equipmentName", maintenance.getEquipmentName());
            map.put("maintenanceDate", maintenance.getMaintenanceDate());
            map.put("details", maintenance.getDetails());
            map.put("employeeId", maintenance.getEmployeeId());
            map.put("status", maintenance.getStatus());

            // 获取员工信息
            if (maintenance.getEmployeeId() != null) {
                Employee employee = employeeService.getById(maintenance.getEmployeeId());
                if (employee != null) {
                    employee = employeeService.getById(maintenance.getEmployeeId());
                    if (employee != null && !employeeName.isEmpty() &&
                            !employee.getName().contains(employeeName)) {
                        continue; // 不匹配员工名条件，跳过此记录
                    }
                    map.put("employeeName", employee.getName());
                    map.put("employeePhone", employee.getPhone());
                }
            } else if (employeeName != null && !employeeName.isEmpty()) {
                continue; // 有员工名筛选但未分配员工，跳过
            }

            records.add(map);
        }

        res.put("data", records);
        res.put("total", records.size()); // 重新计算过滤后的总数
        res.put("status", 200);
        res.put("msg", "查询成功");
        return res;
    }

    // 创建设备维护记录
    @PostMapping("/maintenance/create")
    public ServerResponse<Object> createMaintenance(@RequestBody EquipmentMaintenance maintenance) {
        // 设置默认状态为待处理
        if (maintenance.getStatus() == null) {
            maintenance.setStatus("pending");
        }

        if (equipmentMaintenanceService.save(maintenance)) {
            return ServerResponse.createBySuccessMsg("设备维护记录创建成功");
        } else {
            return ServerResponse.createError("创建失败");
        }
    }

    // 更新设备维护记录
    @PostMapping("/maintenance/update")
    public ServerResponse<Object> updateMaintenance(@RequestBody EquipmentMaintenance maintenance) {
        if (maintenance.getMaintenanceId() == null) {
            return ServerResponse.createError("维护ID不能为空");
        }

        if (equipmentMaintenanceService.updateById(maintenance)) {
            return ServerResponse.createBySuccessMsg("设备维护记录更新成功");
        } else {
            return ServerResponse.createError("更新失败");
        }
    }

    // 删除设备维护记录
    @DeleteMapping("/maintenance/delete/{id}")
    public ServerResponse<Object> deleteMaintenance(@PathVariable Integer id) {
        if (equipmentMaintenanceService.removeById(id)) {
            return ServerResponse.createBySuccessMsg("设备维护记录删除成功");
        } else {
            return ServerResponse.createError("删除失败");
        }
    }

    // 批量删除设备维护记录
    @PostMapping("/maintenance/batchDelete")
    public ServerResponse<Object> batchDeleteMaintenance(@RequestBody List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return ServerResponse.createError("ID列表不能为空");
        }

        if (equipmentMaintenanceService.removeByIds(ids)) {
            return ServerResponse.createBySuccessMsg("批量删除成功");
        } else {
            return ServerResponse.createError("批量删除失败");
        }
    }

    // 更新设备维护状态
    @PostMapping("/maintenance/updateStatus")
    public ServerResponse<Object> updateMaintenanceStatus(@RequestBody Map<String, Object> params) {
        Integer maintenanceId = (Integer) params.get("maintenanceId");
        String status = (String) params.get("status");

        EquipmentMaintenance maintenance = equipmentMaintenanceService.getById(maintenanceId);
        if (maintenance == null) {
            return ServerResponse.createError("设备维护记录不存在");
        }

        maintenance.setStatus(status);

        if (equipmentMaintenanceService.updateById(maintenance)) {
            return ServerResponse.createBySuccessMsg("状态更新成功");
        } else {
            return ServerResponse.createError("状态更新失败");
        }
    }

    // 获取员工的设备维护列表
    @GetMapping("/maintenance/employee")
    public ServerResponse<List<Map<String, Object>>> getEmployeeMaintenance(@RequestParam Integer employeeId) {
        QueryWrapper<EquipmentMaintenance> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("employee_id", employeeId);
        queryWrapper.orderByAsc("maintenance_date");

        List<EquipmentMaintenance> maintenanceList = equipmentMaintenanceService.list(queryWrapper);
        if (maintenanceList == null || maintenanceList.isEmpty()) {
            return ServerResponse.createBySuccessMsg("暂无设备维护记录");
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (EquipmentMaintenance maintenance : maintenanceList) {
            Map<String, Object> map = new HashMap<>();
            map.put("maintenanceId", maintenance.getMaintenanceId());
            map.put("equipmentName", maintenance.getEquipmentName());
            map.put("maintenanceDate", maintenance.getMaintenanceDate());
            map.put("details", maintenance.getDetails());
            map.put("status", maintenance.getStatus());

            result.add(map);
        }

        return ServerResponse.createSuccess(result);
    }

}


