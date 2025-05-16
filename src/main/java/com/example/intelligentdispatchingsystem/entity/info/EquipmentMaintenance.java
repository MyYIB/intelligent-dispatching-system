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
@TableName("equipment_maintenance")
public class EquipmentMaintenance implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "maintenance_id", type = IdType.AUTO)
    private Integer maintenanceId;

    private String equipmentName;

    private LocalDateTime maintenanceDate;

    private String details;

    private Integer employeeId;

    private String status;
}
