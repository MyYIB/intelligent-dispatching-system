package com.example.intelligentdispatchingsystem.mapper;

import com.example.intelligentdispatchingsystem.entity.info.Skills;
import com.example.intelligentdispatchingsystem.entity.role.Employee;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.*;

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

    @Select("SELECT u.address " +
            "FROM employee e " +
            "JOIN user u ON e.user_id = u.user_id " +
            "WHERE e.employee_id = #{employeeId}")
    String getEmployeeAddress(@Param("employeeId") Integer employeeId);
    // 在EmployeeMapper接口中添加以下方法
    @Insert("INSERT INTO employee_skills (employee_id, skill_id) VALUES (#{employeeId}, #{skillId})")
    int addEmployeeSkill(@Param("employeeId") Integer employeeId, @Param("skillId") Integer skillId);

    @Delete("DELETE FROM employee_skills WHERE employee_id = #{employeeId}")
    int removeEmployeeSkills(@Param("employeeId") Integer employeeId);
}
