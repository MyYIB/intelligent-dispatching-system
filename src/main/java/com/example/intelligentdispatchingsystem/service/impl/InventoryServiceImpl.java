package com.example.intelligentdispatchingsystem.service.impl;

import com.example.intelligentdispatchingsystem.entity.info.Inventory;
import com.example.intelligentdispatchingsystem.mapper.InventoryMapper;
import com.example.intelligentdispatchingsystem.service.IInventoryService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author lm
 * @since 2025-03-13
 */
@Service
public class InventoryServiceImpl extends ServiceImpl<InventoryMapper, Inventory> implements IInventoryService {

}
