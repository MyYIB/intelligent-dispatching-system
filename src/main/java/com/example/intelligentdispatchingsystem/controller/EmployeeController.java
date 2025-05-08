package com.example.intelligentdispatchingsystem.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.intelligentdispatchingsystem.common.ServerResponse;
import com.example.intelligentdispatchingsystem.entity.role.Employee;
import com.example.intelligentdispatchingsystem.entity.info.Skills;
import com.example.intelligentdispatchingsystem.entity.role.User;
import com.example.intelligentdispatchingsystem.service.IEmployeeService;
import com.example.intelligentdispatchingsystem.service.ISkillsService;
import com.example.intelligentdispatchingsystem.service.IUserService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author lm
 * @since 2025-03-13
 */
@RestController
@RequestMapping("/employee")
public class EmployeeController {

    @Resource
    IEmployeeService employeeService;
    @Resource
    ISkillsService skillsService;
    @Resource
    IUserService userService;

    /***
     * 分页查询,输入页数与每页的个数
     * @param pageNum
     * @param pageSize
     * @return
     */
    @GetMapping("/page")
    public Map<String, Object> selectPage(@RequestParam int pageNum,
                                          @RequestParam int pageSize,
                                          @RequestParam String username,
                                          @RequestParam String phone
    ) {
        Page<Employee> page = new Page<>(pageNum, pageSize);
        QueryWrapper<Employee> query = new QueryWrapper<>();
        query.like("name", username).like("phone", phone);
        IPage<Employee> employeeIPage = employeeService.page(page, query);
        List<Employee> records = employeeIPage.getRecords();
        // 为每个员工添加地址信息
        for (Employee employee : records) {
            String address = employeeService.getEmployeeAddress(employee.getEmployeeId());
            employee.setAddress(address);
        }
        Map<String, Object> res = new HashMap<>();
        res.put("data", records);
        res.put("total", employeeIPage.getTotal());
        return res;
    }

    @GetMapping("/getSkillList")
    public ServerResponse<Object> getSkillList() {
        List<Skills> skills = skillsService.list();

        if (skills == null || skills.isEmpty()) {
            return ServerResponse.createError("查询技能集失败");
        } else {
            return ServerResponse.createSuccess(skills);
        }
    }

    @GetMapping("/getEmployeeSkills")
    public ServerResponse<Object> getEmployeeSkills(@RequestParam int employeeId) {
        List<Skills> skills = employeeService.getSkillsByEmployeeId(employeeId);
        if (skills == null || skills.isEmpty()) {
            return ServerResponse.createError("未查询到该员工的技能信息");
        }
        return ServerResponse.createSuccess(skills);
    }

    @DeleteMapping("/delete")
    public ServerResponse<Object> deleteOne(@RequestParam int id) {
        if (employeeService.removeById(id)) {
            return ServerResponse.createBySuccessMsg("删除成功");

        } else {
            return ServerResponse.createError("删除失败");
        }
    }

    @DeleteMapping("/delBatch")
    public ServerResponse<Object> delBatch(@RequestBody List<Integer> ids) {
        if (employeeService.removeByIds(ids)) {
            return ServerResponse.createBySuccessMsg("批量删除成功");
        } else {
            return ServerResponse.createError("批量删除失败");
        }
    }

    // 添加用户
    @PostMapping("/addNewEmployee")
    public ServerResponse<Object> addUser(@RequestBody Employee employee) {
        try {
            // 1. 创建用户
            User user = new User();
            user.setUsername(employee.getName());
            user.setPhone(employee.getPhone());
            user.setEmail(employee.getEmail());
            user.setAddress(employee.getAddress());
            // 设置用户账号为手机号
            user.setAccount(user.getPhone());
            // 设置默认密码和角色
            user.setPassword("123456");
            user.setRole("technician");
            // 保存用户
            if (!userService.save(user)) {
                return ServerResponse.createError("创建用户失败");
            }
            // 2. 设置员工的用户ID并保存
            employee.setUserId(user.getUserId());
            employee.setLevelPoint(0.0);
            // 保存员工
            boolean employeeSaved = employeeService.save(employee);
            if (!employeeSaved) {
                // 如果员工创建失败，回滚用户创建
                userService.removeById(user.getUserId());
                return ServerResponse.createError("创建员工失败");
            }

            // 3. 处理员工技能关联
            List<Integer> skillIds = employee.getSkills(); // 假设Employee类中添加了skillIds字段
            if (skillIds != null && !skillIds.isEmpty()) {
                boolean skillsAdded = employeeService.addEmployeeSkills(employee.getEmployeeId(), skillIds);
                if (!skillsAdded) {
                    return ServerResponse.createError("员工创建成功，但添加技能失败");
                }
            }

            return ServerResponse.createBySuccessMsg("新增员工成功");
        } catch (Exception e) {
            e.printStackTrace();
            return ServerResponse.createError("新增员工失败: " + e.getMessage());
        }
    }

    // 更新员工
    @PostMapping("/updateEmployee")
    public ServerResponse<Object> updateEmployee(@RequestBody Employee employee) {
        try {
            if (employee.getEmployeeId() == null) {
                return ServerResponse.createError("员工ID不能为空");
            }

            // 1. 获取员工信息
            Employee existingEmployee = employeeService.getById(employee.getEmployeeId());
            if (existingEmployee == null) {
                return ServerResponse.createError("员工不存在");
            }

            // 2. 更新用户信息
            User user = userService.getById(existingEmployee.getUserId());
            if (user == null) {
                return ServerResponse.createError("关联用户不存在");
            }

            user.setUsername(employee.getName());
            user.setPhone(employee.getPhone());
            user.setEmail(employee.getEmail());
            user.setAddress(employee.getAddress());
            user.setAccount(employee.getPhone()); // 更新账号

            boolean userUpdated = userService.updateById(user);
            if (!userUpdated) {
                return ServerResponse.createError("更新用户信息失败");
            }

            // 3. 更新员工信息
            employee.setUserId(existingEmployee.getUserId()); // 确保用户ID不变

            boolean employeeUpdated = employeeService.updateById(employee);
            if (!employeeUpdated) {
                return ServerResponse.createError("更新员工信息失败");
            }

            // 4. 更新员工技能关联
            List<Integer> skillIds = employee.getSkills(); // 假设Employee类中添加了skillIds字段
            if (skillIds != null) {
                // 先删除原有技能关联
                employeeService.removeEmployeeSkills(employee.getEmployeeId());
                // 添加新的技能关联
                if (!skillIds.isEmpty()) {
                    boolean skillsAdded = employeeService.addEmployeeSkills(employee.getEmployeeId(), skillIds);
                    if (!skillsAdded) {
                        return ServerResponse.createError("员工更新成功，但更新技能失败");
                    }
                }
            }

            return ServerResponse.createBySuccessMsg("更新员工成功");
        } catch (Exception e) {
            e.printStackTrace();
            return ServerResponse.createError("更新员工失败: " + e.getMessage());
        }
    }

    /**
     * 获取所有可用的技术员
     */
    @GetMapping("/available")
    public ServerResponse<List<Employee>> getAvailableEmployees() {
        QueryWrapper<Employee> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", "available");
        queryWrapper.apply("current_workload < max_workload");
        ; // 工作负载未达到最大值
        queryWrapper.orderByAsc("current_workload"); // 按工作负载升序排序

        List<Employee> employees = employeeService.list(queryWrapper);
        //获取技术员技能
//        for (Employee employee: employees) {
//            employee.setSkills(getEmployeeSkills(employee.getEmployeeId()));
//        }
        if (employees != null && !employees.isEmpty()) {
            return ServerResponse.createSuccess(employees);
        } else {
            return ServerResponse.createError("暂无可用技术员");
        }
    }

    /**
     * 更新员工位置和状态
     */
    @PostMapping("/updateLocation")
    public ServerResponse<Object> updateEmployeeLocation(@RequestBody Map<String, Object> params) {
        Integer employeeId = (Integer) params.get("employeeId");
        String location = (String) params.get("location");
        Double locationLatitude = Double.parseDouble(params.get("locationLatitude").toString());
        Double locationLongitude = Double.parseDouble(params.get("locationLongitude").toString());
        String status = (String) params.get("status");

        // 参数校验
        if (employeeId == null || location == null || locationLatitude == null ||
                locationLongitude == null || status == null) {
            return ServerResponse.createError("参数不完整");
        }

        // 检查员工是否存在
        Employee employee = employeeService.getById(employeeId);
        if (employee == null) {
            return ServerResponse.createError("员工不存在");
        }

        // 更新员工位置和状态
        employee.setLocation(location);
        employee.setLocationLatitude(locationLatitude);
        employee.setLocationLongitude(locationLongitude);
        employee.setStatus(status);

        boolean updated = employeeService.updateById(employee);

        if (updated) {
            return ServerResponse.createSuccess(employee);
        } else {
            return ServerResponse.createError("更新失败");
        }
    }

    /**
     * 获取员工信息
     */
    @GetMapping("/info")
    public ServerResponse<Employee> getEmployeeInfo(@RequestParam Integer employeeId) {
        if (employeeId == null) {
            return ServerResponse.createError("员工ID不能为空");
        }

        Employee employee = employeeService.getById(employeeId);
        if (employee == null) {
            return ServerResponse.createError("员工不存在");
        }

        return ServerResponse.createSuccess(employee);
    }
}
