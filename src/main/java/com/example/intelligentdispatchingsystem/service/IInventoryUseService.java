package com.example.intelligentdispatchingsystem.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.intelligentdispatchingsystem.entity.info.InventoryUse;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 备料使用服务接口
 * </p>
 *
 * @author lm
 * @since 2025-03-13
 */
public interface IInventoryUseService extends IService<InventoryUse> {
    
    /**
     * 根据工单ID获取备料使用记录
     * @param orderId 工单ID
     * @return 备料使用记录列表
     */
    List<Map<String, Object>> getInventoryUseByOrderId(Integer orderId);
    
    /**
     * 批量添加备料使用记录
     * @param orderId 工单ID
     * @param inventoryUseList 备料使用记录列表
     * @return 是否成功
     */
    boolean batchAddInventoryUse(Integer orderId, List<InventoryUse> inventoryUseList);
    
    /**
     * 检查备料是否被使用
     * @param inventoryId 备料ID
     * @return 是否被使用
     */
    boolean hasInventoryUsage(Integer inventoryId);
    
    /**
     * 获取物料使用记录
     * @param inventoryId 物料ID
     * @return 使用记录列表
     */
    List<Map<String, Object>> getInventoryUsageRecords(Integer inventoryId);
}
