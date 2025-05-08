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
 * @since 2025-05-07
 */
@Getter
@Setter
@TableName("inventory_use")
public class InventoryUse implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "inventory_use_id", type = IdType.AUTO)
    private Integer inventoryUseId;

    private Integer inventoryId;

    private Integer orderId;

    private Integer num;
}
