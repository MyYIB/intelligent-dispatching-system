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
@TableName("dispatch_records")
public class DispatchRecords implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "dispatch_id", type = IdType.AUTO)
    private Integer dispatchId;

    private Integer orderId;

    private Integer employeeId;

    private LocalDateTime dispatchTime;

    private LocalDateTime completionTime;
}
