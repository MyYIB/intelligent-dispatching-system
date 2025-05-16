package com.example.intelligentdispatchingsystem.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.intelligentdispatchingsystem.entity.info.EmployeeSkills;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.intelligentdispatchingsystem.entity.info.Skills;

import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author lm
 * @since 2025-05-08
 */
public interface IEmployeeSkillsService extends IService<EmployeeSkills> {
    List<Integer> getSkillIdsByEmployeeId(Integer employeeId);

}
