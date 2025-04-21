package com.example.intelligentdispatchingsystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.intelligentdispatchingsystem.entity.info.WorkOrders;
import com.example.intelligentdispatchingsystem.mapper.WorkOrdersMapper;
import com.example.intelligentdispatchingsystem.service.IWorkOrdersService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author lm
 * @since 2025-04-21
 */
@Service
public class WorkOrdersServiceImpl extends ServiceImpl<WorkOrdersMapper, WorkOrders> implements IWorkOrdersService {
    @Resource
    private WorkOrdersMapper workOrdersMapper;

    @Override
    public List<WorkOrders> getOrdersByUserId(Integer userId) {
        QueryWrapper<WorkOrders> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        return workOrdersMapper.selectList(wrapper);
    }
}
