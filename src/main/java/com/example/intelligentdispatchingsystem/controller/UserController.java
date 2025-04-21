package com.example.intelligentdispatchingsystem.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.intelligentdispatchingsystem.common.ServerResponse;
import com.example.intelligentdispatchingsystem.entity.info.ReparType;
import com.example.intelligentdispatchingsystem.entity.role.User;
import com.example.intelligentdispatchingsystem.service.IReparTypeService;
import com.example.intelligentdispatchingsystem.service.IUserService;
import jakarta.annotation.Resource;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.net.URLEncoder;
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
@RequestMapping("/user")
public class UserController {
    @Resource
    private IUserService userService;
    @Resource
    private IReparTypeService repairTypeService;
    /***
     * 分页查询,输入页数与每页的个数
     * @param pageNum·~
     * @param pageSize
     * @return
     */
    @GetMapping("/page")
    public Map<String, Object> selectPage(@RequestParam int pageNum,
                                          @RequestParam int pageSize,
                                          @RequestParam String username,
                                          @RequestParam String phone,
                                          @RequestParam String address
    ) {
        Page<User> page = new Page<>(pageNum, pageSize);
        QueryWrapper<User> query=new QueryWrapper<>();
        query.like("username",username).like("phone",phone).like("address",address);
        IPage<User> UserIPage = userService.page(page,query);
        List<User> records = UserIPage.getRecords();
        Map<String, Object> res = new HashMap<>();
        res.put("data", records);
        res.put("total", UserIPage.getTotal());
        return res;
    }

    // 添加用户
    @PostMapping("/addNewUser")
    public ServerResponse<Object> addUser(@RequestBody User user) {
        //设置用户手机号码就是登录账号
        user.setAccount(user.getPhone());
        user.setRole("customer");
        user.setPassword("123456");
        if (userService.save(user)) {
            return ServerResponse.createBySuccessMsg("新增成功");
        }else {
            return ServerResponse.createError("新增失败");
        }
    }

    //更新用户
    @PostMapping("/updateUser")
    public ServerResponse<Object> updateTeacher(@RequestBody User user){
        //设置用户手机号码就是登录账号
        user.setAccount(user.getPhone());
        if (userService.saveOrUpdate(user)){
            return ServerResponse.createBySuccessMsg("修改成功");
        }else {
            return ServerResponse.createError("修改失败");
        }
    }
    @DeleteMapping("/delete")
    public ServerResponse<Object> deleteOne(@RequestParam int id){
        if (userService.removeById(id)){
            return ServerResponse.createBySuccessMsg("删除成功");

        }else {
            return ServerResponse.createError("删除失败");
        }
    }

    @DeleteMapping("/delBatch")
    public ServerResponse<Object> delBatch(@RequestBody List<Integer> ids){
        if (userService.removeByIds(ids)) {
            return ServerResponse.createBySuccessMsg("批量删除成功");
        }else {
            return ServerResponse.createError("批量删除失败");
        }
    }

    @GetMapping("getRepairType")
    public ServerResponse<Object> getRepairType(){
        // 创建查询条件
        QueryWrapper<ReparType> queryWrapper = new QueryWrapper<>();
        // 获取所有维修类型
        List<ReparType> repairTypes = repairTypeService.list(queryWrapper);
        
        if (repairTypes != null && !repairTypes.isEmpty()) {
            return ServerResponse.createSuccess(repairTypes);
        } else {
            return ServerResponse.createBySuccessMsg("暂无维修类型数据");
        }
    }
}
