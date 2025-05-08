package com.example.intelligentdispatchingsystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.intelligentdispatchingsystem.entity.info.DispatchRecords;
import com.example.intelligentdispatchingsystem.entity.info.WorkOrders;
import com.example.intelligentdispatchingsystem.entity.role.Employee;
import com.example.intelligentdispatchingsystem.entity.role.User;
import com.example.intelligentdispatchingsystem.mapper.WorkOrdersMapper;
import com.example.intelligentdispatchingsystem.service.*;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class WorkOrdersServiceImpl extends ServiceImpl<WorkOrdersMapper, WorkOrders> implements IWorkOrdersService {
    @Resource
    private IEmployeeService employeeService;

    @Resource
    private IDispatchRecordsService dispatchRecordsService;

    @Resource
    private IUserService userService;

    @Resource
    private IEmployeeSkillsService employeeSkillsService;

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

    @Override
    public User getUserByOrderId(Integer orderId) {
        WorkOrders workOrder = this.getById(orderId);
        return userService.getById(workOrder.getUserId());
    }
    
    @Override
    public Integer intelligentAssignOrder(WorkOrders workOrder) {
        // 1. 获取所有可用的技术员
        QueryWrapper<Employee> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", "available");
        queryWrapper.apply("current_workload < max_workload"); // 工作负载未满
        List<Employee> availableEmployees = employeeService.list(queryWrapper);
        
        if (availableEmployees.isEmpty()) {
            return null; // 没有可用技术员
        }
        
        // 2. 计算每个技术员的综合评分
        Map<Integer, Double> employeeScores = new HashMap<>();
        
        for (Employee employee : availableEmployees) {
            double score = calculateEmployeeScore(employee, workOrder);
            employeeScores.put(employee.getEmployeeId(), score);
            System.out.println(score);
        }
        
        // 3. 选择评分最高的技术员
        return employeeScores.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }
    
    /**
     * 计算技术员与工单的匹配评分
     * @param employee 技术员
     * @param workOrder 工单
     * @return 综合评分
     */
    private double calculateEmployeeScore(Employee employee, WorkOrders workOrder) {
        double score = 0.0;
        
        // 1. 距离评分 (0-40分) - 距离越近分数越高
        double distanceScore = calculateDistanceScore(employee, workOrder);
        
        // 2. 工作负载评分 (0-30分) - 负载越低分数越高
        double workloadScore = calculateWorkloadScore(employee);
        
        // 3. 技能匹配评分 (0-20分) - 技能匹配度越高分数越高
        double skillScore = calculateSkillScore(employee, workOrder);
        
        // 4. 历史绩效评分 (0-10分) - 历史绩效越好分数越高
        double performanceScore = calculatePerformanceScore(employee);
        
        // 综合评分
        score = distanceScore + workloadScore + skillScore + performanceScore;
        
        return score;
    }
    
    /**
     * 计算距离评分
     */
    private double calculateDistanceScore(Employee employee, WorkOrders workOrder) {
        // 如果位置信息不完整，返回默认分数
        if (employee.getLocationLatitude() == null || employee.getLocationLongitude() == null ||
            workOrder.getLocationLatitude() == null || workOrder.getLocationLongitude() == null) {
            return 20.0; // 默认中等分数
        }
        
        // 计算两点之间的距离（使用哈弗辛公式计算球面距离）
        double distance = calculateDistance(
            employee.getLocationLatitude(), employee.getLocationLongitude(),
            workOrder.getLocationLatitude(), workOrder.getLocationLongitude()
        );
        
        // 距离评分：距离越近，分数越高
        // 假设10公里内为最佳，超过50公里为最差
        if (distance <= 1) {
            return 40.0; // 1公里内，满分
        } else if (distance <= 5) {
            return 35.0; // 1-5公里
        } else if (distance <= 10) {
            return 30.0; // 5-10公里
        } else if (distance <= 20) {
            return 25.0; // 10-20公里
        } else if (distance <= 30) {
            return 20.0; // 20-30公里
        } else if (distance <= 40) {
            return 15.0; // 30-40公里
        } else if (distance <= 50) {
            return 10.0; // 40-50公里
        } else {
            return 5.0;  // 50公里以上
        }
    }
    
    /**
     * 计算两点之间的距离（哈弗辛公式）
     */
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        // 地球半径（单位：公里）
        final double EARTH_RADIUS = 6371.0;
        
        // 将经纬度转换为弧度
        double radLat1 = Math.toRadians(lat1);
        double radLon1 = Math.toRadians(lon1);
        double radLat2 = Math.toRadians(lat2);
        double radLon2 = Math.toRadians(lon2);
        
        // 计算差值
        double deltaLat = radLat2 - radLat1;
        double deltaLon = radLon2 - radLon1;
        
        // 应用哈弗辛公式
        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) +
                   Math.cos(radLat1) * Math.cos(radLat2) *
                   Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        // 计算距离
        return EARTH_RADIUS * c;
    }
    
    /**
     * 计算工作负载评分
     */
    private double calculateWorkloadScore(Employee employee) {
        // 工作负载比例 = 当前负载 / 最大负载
        double workloadRatio = (double) employee.getCurrentWorkload() / employee.getMaxWorkload();
        
        // 工作负载评分：负载越低，分数越高
        if (workloadRatio <= 0.2) {
            return 30.0; // 负载低于20%，满分
        } else if (workloadRatio <= 0.4) {
            return 25.0; // 负载20%-40%
        } else if (workloadRatio <= 0.6) {
            return 20.0; // 负载40%-60%
        } else if (workloadRatio <= 0.8) {
            return 15.0; // 负载60%-80%
        } else {
            return 10.0; // 负载80%以上
        }
    }
    
    /**
     * 计算技能匹配评分
     */
    private double calculateSkillScore(Employee employee, WorkOrders workOrder) {
        // 获取工单所需技能（这里假设根据工单类型或描述判断）
        String orderType = workOrder.getOrderType();
        String repairType = String.valueOf(workOrder.getRepairType());
        
        // 获取技术员的技能列表
        List<Integer> employeeSkills = employeeSkillsService.getSkillIdsByEmployeeId(employee.getEmployeeId());
        
        // 根据工单类型和维修类型判断所需技能
        // 这里需要根据实际业务逻辑调整
        int matchedSkills = 0;
        int requiredSkills = 0;
        
        // 简化版：根据技术员等级评分
        int skillLevel = employee.getSkillLevel();
        
        // 技能等级评分：等级越高，分数越高
        if (skillLevel >= 5) {
            return 20.0; // 高级技术员
        } else if (skillLevel >= 3) {
            return 15.0; // 中级技术员
        } else {
            return 10.0; // 初级技术员
        }
    }
    
    /**
     * 计算历史绩效评分
     */
    private double calculatePerformanceScore(Employee employee) {
        // 这里可以根据历史完成工单的质量、速度等因素评分
        // 简化版：根据员工等级经验评分
        double levelPoint = employee.getLevelPoint();
        
        // 绩效评分：经验值越高，分数越高
        if (levelPoint >= 90) {
            return 10.0; // 优秀
        } else if (levelPoint >= 80) {
            return 8.0;  // 良好
        } else if (levelPoint >= 70) {
            return 6.0;  // 一般
        } else if (levelPoint >= 60) {
            return 4.0;  // 及格
        } else {
            return 2.0;  // 较差
        }
    }
    

}
