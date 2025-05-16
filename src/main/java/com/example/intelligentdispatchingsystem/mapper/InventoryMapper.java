package com.example.intelligentdispatchingsystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.intelligentdispatchingsystem.entity.info.Inventory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 库存Mapper接口
 * </p>
 *
 * @author lm
 * @since 2025-03-13
 */
@Mapper
public interface InventoryMapper extends BaseMapper<Inventory> {

    /**
     * 获取总库存数量
     */
    @Select("SELECT COALESCE(SUM(quantity), 0) FROM inventory")
    int getTotalQuantity();

    /**
     * 获取库存不足的物料数量
     */
    @Select("SELECT COUNT(*) FROM inventory WHERE quantity < #{threshold}")
    int getLowStockCount(@Param("threshold") int threshold);

    /**
     * 获取最近一段时间使用最多的物料
     */
    @Select("SELECT i.item_id, i.item_name, COALESCE(SUM(iu.num), 0) as total_used " +
            "FROM inventory i " +
            "LEFT JOIN inventory_use iu ON i.item_id = iu.inventory_id " +
            "WHERE iu.inventory_use_id IS NULL OR " +
            "iu.inventory_use_id IN (SELECT inventory_use_id FROM inventory_use WHERE DATE(created_at) >= DATE_SUB(CURDATE(), INTERVAL #{days} DAY)) " +
            "GROUP BY i.item_id, i.item_name " +
            "ORDER BY total_used DESC " +
            "LIMIT #{limit}")
    List<Map<String, Object>> getTopUsedItems(@Param("days") int days, @Param("limit") int limit);
}
