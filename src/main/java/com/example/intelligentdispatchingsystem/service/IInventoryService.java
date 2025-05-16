package com.example.intelligentdispatchingsystem.service;

import com.example.intelligentdispatchingsystem.entity.info.Inventory;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author lm
 * @since 2025-03-13
 */
public interface IInventoryService extends IService<Inventory> {
    /**
     * 减少库存
     * @param itemId 备料ID
     * @param quantity 减少数量
     * @return 是否成功
     */
    boolean reduceInventory(Integer itemId, Integer quantity);
    /**
     * 获取库存统计信息
     * @return 统计信息
     */
    Map<String, Object> getInventoryStatistics();
}
