package com.example.intelligentdispatchingsystem.entity.info;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 
 * </p>
 *
 * @author lm
 * @since 2025-05-08
 */
@Getter
@Setter
@TableName("employee_skills")
public class EmployeeSkills implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId("employee_id")
    private Integer employeeId;

    private Integer skillId;
}
