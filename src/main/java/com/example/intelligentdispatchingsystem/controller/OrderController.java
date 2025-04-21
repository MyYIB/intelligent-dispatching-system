package com.example.intelligentdispatchingsystem.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.intelligentdispatchingsystem.common.ServerResponse;
import com.example.intelligentdispatchingsystem.entity.info.WorkOrders;
import com.example.intelligentdispatchingsystem.entity.role.User;
import com.example.intelligentdispatchingsystem.service.IWorkOrdersService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/order")
public class OrderController {
    @Resource
    public IWorkOrdersService workOrdersService;

    @PostMapping("/create")
    public ServerResponse<Object> create(@RequestBody WorkOrders workOrders) {
        workOrders.setPriority("med");
        if(workOrdersService.save(workOrders)){
            return ServerResponse.createBySuccessMsg("创建成功");
        }else{
            return ServerResponse.createError("创建失败");
        }
    }
    @GetMapping("/user/list")
    public ServerResponse<List<WorkOrders>> getUserOrders(@RequestParam Integer userId) {
        List<WorkOrders> list = workOrdersService.getOrdersByUserId(userId);
        if (list != null && !list.isEmpty()) {
            return ServerResponse.createSuccess(list);
        } else {
            return ServerResponse.createBySuccessMsg("暂无数据");
        }

    }
    @GetMapping("/detail")
    public ServerResponse<WorkOrders> getOrderDetail(@RequestParam Integer orderId) {
        QueryWrapper<WorkOrders> query=new QueryWrapper<>();
        query.eq("order_id", orderId);
        WorkOrders workOrders = workOrdersService.getOne(query);
        if (workOrders != null ) {
            return ServerResponse.createSuccess(workOrders);
        } else {
            return ServerResponse.createBySuccessMsg("暂无该订单数据");
        }
    }
}
