package com.example.intelligentdispatchingsystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.intelligentdispatchingsystem.entity.info.EmployeeSkills;
import com.example.intelligentdispatchingsystem.mapper.EmployeeSkillsMapper;
import com.example.intelligentdispatchingsystem.service.IEmployeeSkillsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author lm
 * @since 2025-05-08
 */
@Service
public class EmployeeSkillsServiceImpl extends ServiceImpl<EmployeeSkillsMapper, EmployeeSkills> implements IEmployeeSkillsService {
    @Override
    public List<Integer> getSkillIdsByEmployeeId(Integer employeeId) {
        QueryWrapper<EmployeeSkills> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("employee_id", employeeId);

        return this.list(queryWrapper).stream()
                .map(EmployeeSkills::getSkillId)
                .collect(Collectors.toList());
    }
}
