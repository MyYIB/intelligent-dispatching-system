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
 * @since 2025-04-21
 */
@Getter
@Setter
@TableName("work_orders")
public class WorkOrders implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "order_id", type = IdType.AUTO)
    private Integer orderId;

    private Integer userId;

    private String orderType;

    private String description;

    private String status;

    private String location;

    private Double locationLatitude;

    private Double locationLongitude;

    private LocalDateTime createdAt;

    private Integer assignedEmployee;

    private LocalDateTime resolvedAt;

    private String priority;

    private LocalDateTime deadline;

    private Integer repairType;
}
