package com.example.intelligentdispatchingsystem.entity.info;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 
 * </p>
 *
 * @author lm
 * @since 2025-05-11
 */
@Getter
@Setter
@TableName("task_assignments")
public class TaskAssignments implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "assignment_id", type = IdType.AUTO)
    private Integer assignmentId;

    private Integer orderId;

    private Integer employeeId;

    private Double matchingScore;

    private LocalDateTime assignedAt;
}
