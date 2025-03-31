package com.example.intelligentdispatchingsystem.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.intelligentdispatchingsystem.common.R;
import com.example.intelligentdispatchingsystem.entity.Employee;
import com.example.intelligentdispatchingsystem.entity.Skills;
import com.example.intelligentdispatchingsystem.service.IEmployeeService;
import com.example.intelligentdispatchingsystem.service.ISkillsService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author lm
 * @since 2025-03-13
 */
@RestController
@RequestMapping("/employee")
public class EmployeeController {

    @Resource
    IEmployeeService employeeService;
    @Resource
    ISkillsService skillsService;
    /***
     * 分页查询,输入页数与每页的个数
     * @param pageNum
     * @param pageSize
     * @return
     */
    @GetMapping("/page")
    public Map<String, Object> selectPage(@RequestParam int pageNum,
                                          @RequestParam int pageSize,
                                          @RequestParam String username,
                                          @RequestParam String phone
    ) {
        Page<Employee> page = new Page<>(pageNum, pageSize);
        QueryWrapper<Employee> query=new QueryWrapper<>();
        query.like("name",username).like("phone",phone);
        IPage<Employee> employeeIPage = employeeService.page(page,query);
        List<Employee> records = employeeIPage.getRecords();
        Map<String, Object> res = new HashMap<>();
        res.put("data", records);
        res.put("total", employeeIPage.getTotal());
        return res;
    }

    @GetMapping("/getSkillList")
    public R<Object> getSkillList() {
        List<Skills> skills = skillsService.list();

        if (skills == null || skills.isEmpty()) {
            return R.error("查询技能集失败");
        }else {
            return R.ArraySuccess(new ArrayList<>(skills));
        }
    }


}
