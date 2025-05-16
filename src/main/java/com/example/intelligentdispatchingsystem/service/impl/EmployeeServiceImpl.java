package com.example.intelligentdispatchingsystem.service.impl;

import com.example.intelligentdispatchingsystem.common.ServerResponse;
import com.example.intelligentdispatchingsystem.entity.info.Skills;
import com.example.intelligentdispatchingsystem.entity.role.Employee;
import com.example.intelligentdispatchingsystem.mapper.EmployeeMapper;
import com.example.intelligentdispatchingsystem.service.IEmployeeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author lm
 * @since 2025-03-13
 */
@Service
public class EmployeeServiceImpl extends ServiceImpl<EmployeeMapper, Employee> implements IEmployeeService {
    @Autowired
    private EmployeeMapper employeeMapper;

    public List<Skills> getSkillsByEmployeeId(Integer employeeId) {
        return employeeMapper.getSkillsByEmployeeId(employeeId);
    }

    public String getEmployeeAddress(Integer employeeId) {
        return employeeMapper.getEmployeeAddress(employeeId);
    }

    @Override
    public boolean addEmployeeSkills(Integer employeeId, List<Integer> skillIds) {
        try {
            for (Integer skillId : skillIds) {
                employeeMapper.addEmployeeSkill(employeeId, skillId);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean removeEmployeeSkills(Integer employeeId) {
        try {
            employeeMapper.removeEmployeeSkills(employeeId);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Transactional
    @Override
    public boolean plusEmployeeLevelPoints(Integer employeeId, Double levelPoints) {
        Employee employee = this.getById(employeeId);
        if (employee == null) {
            return false;
        }
        Double nowLevelPoints = employee.getLevelPoint();
        nowLevelPoints += levelPoints;
        employee.setLevelPoint(nowLevelPoints);
        int level = 1;
        if (nowLevelPoints >= 100){
            level = (int) (nowLevelPoints /100);
        }
        employee.setSkillLevel(level);
        return this.updateById(employee);
    }

    @Override
    public boolean completeWork(Integer employeeId) {
        Employee employee = this.getById(employeeId);
        if (employee == null) {
            return false;
        }
        employee.setCurrentWorkload(employee.getCurrentWorkload() - 1);
        return this.updateById(employee);
    }


}
