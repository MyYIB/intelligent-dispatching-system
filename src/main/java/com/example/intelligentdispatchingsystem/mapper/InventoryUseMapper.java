package com.example.intelligentdispatchingsystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.intelligentdispatchingsystem.entity.info.InventoryUse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 备料使用Mapper接口
 * </p>
 *
 * @author lm
 * @since 2025-03-13
 */
@Mapper
public interface InventoryUseMapper extends BaseMapper<InventoryUse> {

    /**
     * 根据工单ID获取备料使用记录
     */
    @Select("SELECT iu.inventory_use_id, iu.inventory_id, iu.order_id, iu.num, " +
            "i.item_name, i.quantity as current_quantity " +
            "FROM inventory_use iu " +
            "LEFT JOIN inventory i ON iu.inventory_id = i.item_id " +
            "WHERE iu.order_id = #{orderId}")
    List<Map<String, Object>> getInventoryUseByOrderId(@Param("orderId") Integer orderId);

    /**
     * 获取物料使用记录
     */
    @Select("SELECT iu.inventory_use_id, iu.inventory_id, iu.order_id, iu.num, " +
            "iu.created_at as use_time, " +
            "wo.status as order_status, wo.description as order_description, " +
            "wo.order_type, wo.location, " +
            "e.name as employee_name, e.phone as employee_phone " +
            "FROM inventory_use iu " +
            "LEFT JOIN work_orders wo ON iu.order_id = wo.order_id " +
            "LEFT JOIN employee e ON wo.assigned_employee = e.employee_id " +
            "WHERE iu.inventory_id = #{inventoryId} " +
            "ORDER BY iu.created_at DESC")
    List<Map<String, Object>> getInventoryUsageRecords(@Param("inventoryId") Integer inventoryId);
}
