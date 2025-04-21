package com.example.intelligentdispatchingsystem.service;

import com.example.intelligentdispatchingsystem.entity.info.WorkOrders;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author lm
 * @since 2025-04-21
 */
public interface IWorkOrdersService extends IService<WorkOrders> {
    List<WorkOrders> getOrdersByUserId(Integer userId);
}
