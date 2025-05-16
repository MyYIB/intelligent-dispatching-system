package com.example.intelligentdispatchingsystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.intelligentdispatchingsystem.entity.info.FeedbackRecords;
import com.example.intelligentdispatchingsystem.mapper.FeedbackRecordsMapper;
import com.example.intelligentdispatchingsystem.service.IFeedbackRecordsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author lm
 * @since 2025-05-12
 */
@Service
public class FeedbackRecordsServiceImpl extends ServiceImpl<FeedbackRecordsMapper, FeedbackRecords> implements IFeedbackRecordsService {

    @Override
    public FeedbackRecords getFeedbackByOrderId(Integer orderId) {
        QueryWrapper<FeedbackRecords> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("order_id", orderId);
        return baseMapper.selectOne(queryWrapper);
    }
}
