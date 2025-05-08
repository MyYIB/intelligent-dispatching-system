package com.example.intelligentdispatchingsystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.intelligentdispatchingsystem.entity.info.InventoryUse;
import com.example.intelligentdispatchingsystem.mapper.InventoryUseMapper;
import com.example.intelligentdispatchingsystem.service.IInventoryService;
import com.example.intelligentdispatchingsystem.service.IInventoryUseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 备料使用服务实现类
 * </p>
 *
 * @author lm
 * @since 2025-03-13
 */
@Service
public class InventoryUseServiceImpl extends ServiceImpl<InventoryUseMapper, InventoryUse> implements IInventoryUseService {

    @Autowired
    private IInventoryService inventoryService;

    @Override
    public List<Map<String, Object>> getInventoryUseByOrderId(Integer orderId) {
        return baseMapper.getInventoryUseByOrderId(orderId);
    }

    @Override
    @Transactional
    public boolean batchAddInventoryUse(Integer orderId, List<InventoryUse> inventoryUseList) {
        if (inventoryUseList == null || inventoryUseList.isEmpty()) {
            return true;
        }
        
        for (InventoryUse inventoryUse : inventoryUseList) {
            // 设置工单ID
            inventoryUse.setOrderId(orderId);
            
            // 减少库存
            boolean reduceSuccess = inventoryService.reduceInventory(
                    inventoryUse.getInventoryId(), inventoryUse.getNum());
            
            if (!reduceSuccess) {
                throw new RuntimeException("库存不足，无法完成操作");
            }
            
            // 保存使用记录
            boolean saveSuccess = this.save(inventoryUse);
            if (!saveSuccess) {
                throw new RuntimeException("保存备料使用记录失败");
            }
        }
        
        return true;
    }
    
    @Override
    public boolean hasInventoryUsage(Integer inventoryId) {
        LambdaQueryWrapper<InventoryUse> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(InventoryUse::getInventoryId, inventoryId);
        return this.count(queryWrapper) > 0;
    }

    @Override
    public List<Map<String, Object>> getInventoryUsageRecords(Integer inventoryId) {
        return baseMapper.getInventoryUsageRecords(inventoryId);
    }
}
