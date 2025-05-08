package com.example.intelligentdispatchingsystem.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.intelligentdispatchingsystem.common.ServerResponse;
import com.example.intelligentdispatchingsystem.entity.info.Inventory;
import com.example.intelligentdispatchingsystem.entity.info.InventoryUse;
import com.example.intelligentdispatchingsystem.service.IInventoryService;
import com.example.intelligentdispatchingsystem.service.IInventoryUseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * <p>
 *  库存管理前端控制器
 * </p>
 *
 * @author lm
 * @since 2025-03-13
 */
@RestController
@RequestMapping("/inventory")
public class InventoryController {

    @Autowired
    private IInventoryService inventoryService;
    
    @Autowired
    private IInventoryUseService inventoryUseService;

    /**
     * 获取所有备料列表（支持分页和搜索）
     */
    @GetMapping("/list")
    public ServerResponse<Object> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String itemName) {
        
        try {
            // 创建分页对象
            Page<Inventory> page = new Page<>(pageNum, pageSize);
            
            // 创建查询条件
            LambdaQueryWrapper<Inventory> queryWrapper = new LambdaQueryWrapper<>();
            
            // 如果有搜索条件，添加模糊查询
            if (StringUtils.hasText(itemName)) {
                queryWrapper.like(Inventory::getItemName, itemName);
            }
            
            // 按ID排序
            queryWrapper.orderByAsc(Inventory::getItemId);
            
            // 执行分页查询
            IPage<Inventory> pageResult = inventoryService.page(page, queryWrapper);
            
            // 返回结果
            return ServerResponse.createSuccess(pageResult);
        } catch (Exception e) {
            e.printStackTrace();
            return ServerResponse.createError("获取备料列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据ID获取备料详情
     */
    @GetMapping("/detail")
    public ServerResponse<Inventory> detail(@RequestParam Integer itemId) {
        try {
            Inventory inventory = inventoryService.getById(itemId);
            if (inventory != null) {
                return ServerResponse.createSuccess(inventory);
            } else {
                return ServerResponse.createError("备料不存在");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ServerResponse.createError("获取备料详情失败: " + e.getMessage());
        }
    }
    
    /**
     * 添加备料
     */
    @PostMapping("/add")
    public ServerResponse<Object> add(@RequestBody Inventory inventory) {
        try {
            // 设置创建时间
            inventory.setLastUpdated(LocalDateTime.now());
            
            boolean success = inventoryService.save(inventory);
            if (success) {
                return ServerResponse.createBySuccessMsg("添加备料成功");
            } else {
                return ServerResponse.createError("添加备料失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ServerResponse.createError("添加备料失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新备料
     */
    @PostMapping("/update")
    public ServerResponse<Object> update(@RequestBody Inventory inventory) {
        try {
            // 设置更新时间
            inventory.setLastUpdated(LocalDateTime.now());
            
            boolean success = inventoryService.updateById(inventory);
            if (success) {
                return ServerResponse.createBySuccessMsg("更新备料成功");
            } else {
                return ServerResponse.createError("更新备料失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ServerResponse.createError("更新备料失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除备料
     */
    @DeleteMapping("/delete")
    public ServerResponse<Object> delete(@RequestParam Integer itemId) {
        try {
            // 检查是否有关联的使用记录
            boolean hasUsage = inventoryUseService.hasInventoryUsage(itemId);
            if (hasUsage) {
                return ServerResponse.createError("该备料已被使用，无法删除");
            }
            
            boolean success = inventoryService.removeById(itemId);
            if (success) {
                return ServerResponse.createBySuccessMsg("删除备料成功");
            } else {
                return ServerResponse.createError("删除备料失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ServerResponse.createError("删除备料失败: " + e.getMessage());
        }
    }
    
    /**
     * 批量删除备料
     */
    @DeleteMapping("/delBatch")
    public ServerResponse<Object> delBatch(@RequestBody List<Integer> ids) {
        try {
            // 检查是否有关联的使用记录
            for (Integer id : ids) {
                boolean hasUsage = inventoryUseService.hasInventoryUsage(id);
                if (hasUsage) {
                    return ServerResponse.createError("ID为" + id + "的备料已被使用，无法删除");
                }
            }
            
            boolean success = inventoryService.removeByIds(ids);
            if (success) {
                return ServerResponse.createBySuccessMsg("批量删除成功");
            } else {
                return ServerResponse.createError("批量删除失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ServerResponse.createError("批量删除失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据工单ID获取已使用的备料
     */
    @GetMapping("/getUse/{orderId}")
    public ServerResponse<List<Map<String, Object>>> getInventoryUseByOrderId(@PathVariable Integer orderId) {
        try {
            List<Map<String, Object>> useList = inventoryUseService.getInventoryUseByOrderId(orderId);
            return ServerResponse.createSuccess(useList);
        } catch (Exception e) {
            e.printStackTrace();
            return ServerResponse.createError("获取备料使用记录失败: " + e.getMessage());
        }
    }
    
    /**
     * 添加备料使用记录
     */
    @PostMapping("/use")
    public ServerResponse<Object> addInventoryUse(@RequestBody InventoryUse inventoryUse) {
        try {
            // 参数校验
            if (inventoryUse == null || inventoryUse.getInventoryId() == null || 
                inventoryUse.getOrderId() == null || inventoryUse.getNum() == null) {
                return ServerResponse.createError("参数不完整");
            }
            
            boolean success = inventoryUseService.save(inventoryUse);
            if (success) {
                // 减少库存
                boolean reduceSuccess = inventoryService.reduceInventory(
                        inventoryUse.getInventoryId(), inventoryUse.getNum());
                
                if (reduceSuccess) {
                    return ServerResponse.createBySuccessMsg("添加备料使用记录成功");
                } else {
                    return ServerResponse.createError("库存不足，无法完成操作");
                }
            } else {
                return ServerResponse.createError("添加备料使用记录失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ServerResponse.createError("添加备料使用记录失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取备料使用统计
     */
    @GetMapping("/statistics")
    public ServerResponse<Map<String, Object>> getInventoryStatistics() {
        try {
            Map<String, Object> statistics = inventoryService.getInventoryStatistics();
            return ServerResponse.createSuccess(statistics);
        } catch (Exception e) {
            e.printStackTrace();
            return ServerResponse.createError("获取备料统计数据失败: " + e.getMessage());
        }
    }

    /**
     * 获取物料使用记录
     */
    @GetMapping("/usage/{inventoryId}")
    public ServerResponse<List<Map<String, Object>>> getInventoryUsage(@PathVariable Integer inventoryId) {
        try {
            List<Map<String, Object>> usageRecords = inventoryUseService.getInventoryUsageRecords(inventoryId);
            return ServerResponse.createSuccess(usageRecords);
        } catch (Exception e) {
            e.printStackTrace();
            return ServerResponse.createError("获取物料使用记录失败: " + e.getMessage());
        }
    }
}
