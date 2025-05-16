package com.example.intelligentdispatchingsystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.intelligentdispatchingsystem.entity.info.DispatchRecords;
import com.example.intelligentdispatchingsystem.entity.role.Employee;
import com.example.intelligentdispatchingsystem.mapper.DispatchRecordsMapper;
import com.example.intelligentdispatchingsystem.mapper.EmployeeMapper;
import com.example.intelligentdispatchingsystem.service.IDispatchRecordsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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
public class DispatchRecordsServiceImpl extends ServiceImpl<DispatchRecordsMapper, DispatchRecords> implements IDispatchRecordsService {

    @Resource
    private EmployeeMapper employeeMapper;

    @Override
    public Employee getDispatchEmployeesByOrderId(Integer orderId) {

        QueryWrapper<DispatchRecords> dispatchWrapper = new QueryWrapper<>();
        dispatchWrapper.eq("order_id", orderId);
        DispatchRecords dispatchRecord = this.getOne(dispatchWrapper);
        if(dispatchRecord == null) {
            return null;
        }
        return employeeMapper.selectById(dispatchRecord.getEmployeeId());
    }
}
