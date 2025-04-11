package com.example.intelligentdispatchingsystem.entity.role;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import java.util.List;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 
 * </p>
 *
 * @author lm
 * @since 2025-03-13
 */
@Getter
@Setter
@TableName("employee")
public class Employee implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "employee_id", type = IdType.AUTO)
    private Integer employeeId;

    private Integer userId;

    private String name;

    private String phone;

    private String email;

    private String location;

    private Double locationLatitude;

    private Double locationLongitude;

    private Integer workload;

    private Integer skillLevel;

    private Double levelPoint;

    private Integer currentWorkload;

    private Integer maxWorkload;

    private String status;
    @TableField(exist = false)
    private List<Integer> skills;
    @TableField(exist = false)
    private String address;
}
