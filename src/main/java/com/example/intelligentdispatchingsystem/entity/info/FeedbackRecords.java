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
 * @since 2025-05-12
 */
@Getter
@Setter
@TableName("feedback_records")
public class FeedbackRecords implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "feedback_id", type = IdType.AUTO)
    private Integer feedbackId;

    private Integer orderId;

    private Integer employeeId;

    private String feedbackState;

    private String feedbackText;

    private Integer satisfactionScore;

    private LocalDateTime feedbackTime;

    private LocalDateTime needTime;
}
