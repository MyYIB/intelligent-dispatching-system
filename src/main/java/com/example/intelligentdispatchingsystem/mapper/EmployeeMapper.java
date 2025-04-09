package com.example.intelligentdispatchingsystem.mapper;

import com.example.intelligentdispatchingsystem.entity.info.Skills;
import com.example.intelligentdispatchingsystem.entity.role.Employee;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author lm
 * @since 2025-03-13
 */
@Mapper
public interface EmployeeMapper extends BaseMapper<Employee> {
    @Select("SELECT s.skill_id, s.skill_name " +
            "FROM skills s " +
            "JOIN employee_skills es ON s.skill_id = es.skill_id " +
            "WHERE es.employee_id = #{employeeId}")
    List<Skills> getSkillsByEmployeeId(@Param("employeeId") Integer employeeId);
}
