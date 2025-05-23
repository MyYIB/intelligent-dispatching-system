package com.example.intelligentdispatchingsystem.service;

import com.example.intelligentdispatchingsystem.common.ServerResponse;
import com.example.intelligentdispatchingsystem.entity.info.Skills;
import com.example.intelligentdispatchingsystem.entity.role.Employee;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author lm
 * @since 2025-03-13
 */
public interface IEmployeeService extends IService<Employee> {
    List<Skills> getSkillsByEmployeeId(Integer employeeId);
    String getEmployeeAddress(Integer employeeId);
    boolean addEmployeeSkills(Integer employeeId, List<Integer> skillIds);
    boolean removeEmployeeSkills(Integer employeeId);
    boolean plusEmployeeLevelPoints(Integer employeeId, Double level);
    boolean completeWork(Integer employeeId);
}
