package com.example.intelligentdispatchingsystem.entity.info;

import com.baomidou.mybatisplus.annotation.IdType;
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
 * @since 2025-04-18
 */
@Getter
@Setter
@TableName("repar_type")
public class ReparType implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "repar_type_id", type = IdType.AUTO)
    private Integer reparTypeId;

    private String reparTypeName;
}
