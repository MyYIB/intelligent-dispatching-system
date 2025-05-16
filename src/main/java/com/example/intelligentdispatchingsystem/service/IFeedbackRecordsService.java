package com.example.intelligentdispatchingsystem.service;

import com.example.intelligentdispatchingsystem.entity.info.FeedbackRecords;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author lm
 * @since 2025-05-12
 */
public interface IFeedbackRecordsService extends IService<FeedbackRecords> {
     FeedbackRecords getFeedbackByOrderId(Integer orderId);
}
