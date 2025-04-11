package com.example.intelligentdispatchingsystem.service.impl;

import com.example.intelligentdispatchingsystem.entity.info.Skills;
import com.example.intelligentdispatchingsystem.entity.role.Employee;
import com.example.intelligentdispatchingsystem.mapper.EmployeeMapper;
import com.example.intelligentdispatchingsystem.service.IEmployeeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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
}
