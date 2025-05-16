package com.example.intelligentdispatchingsystem.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.intelligentdispatchingsystem.entity.info.Inventory;
import com.example.intelligentdispatchingsystem.mapper.InventoryMapper;
import com.example.intelligentdispatchingsystem.service.IInventoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 库存服务实现类
 * </p>
 *
 * @author lm
 * @since 2025-03-13
 */
@Service
public class InventoryServiceImpl extends ServiceImpl<InventoryMapper, Inventory> implements IInventoryService {

    @Override
    @Transactional
    public boolean reduceInventory(Integer itemId, Integer quantity) {
        Inventory inventory = this.getById(itemId);
        if (inventory == null || inventory.getQuantity() < quantity) {
            return false;
        }
        
        inventory.setQuantity(inventory.getQuantity() - quantity);
        inventory.setLastUpdated(LocalDateTime.now());
        return this.updateById(inventory);
    }

    @Override
    public Map<String, Object> getInventoryStatistics() {
        Map<String, Object> statistics = new HashMap<>();
        
        // 获取总库存数量
        int totalQuantity = baseMapper.getTotalQuantity();
        
        // 获取库存不足的物料数量（小于10个）
        int lowStockCount = baseMapper.getLowStockCount(10);
        
        // 获取最近一周使用最多的物料
        List<Map<String, Object>> topUsedItems = baseMapper.getTopUsedItems(7, 5);
        
        statistics.put("totalQuantity", totalQuantity);
        statistics.put("lowStockCount", lowStockCount);
        statistics.put("topUsedItems", topUsedItems);
        
        return statistics;
    }
}
