package com.example.intelligentdispatchingsystem.service;

import com.example.intelligentdispatchingsystem.entity.info.DispatchRecords;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.intelligentdispatchingsystem.entity.role.Employee;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author lm
 * @since 2025-04-21
 */
public interface IDispatchRecordsService extends IService<DispatchRecords> {
    Employee getDispatchEmployeesByOrderId(Integer orderId);
}
