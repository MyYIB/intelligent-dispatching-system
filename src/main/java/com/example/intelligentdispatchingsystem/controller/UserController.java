package com.example.intelligentdispatchingsystem.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.intelligentdispatchingsystem.common.R;
import com.example.intelligentdispatchingsystem.entity.role.User;
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
    public R<Object> addUser(@RequestBody User user) {
        //设置用户手机号码就是登录账号
        user.setAccount(user.getPhone());
        user.setRole("customer");
        user.setPassword("123456");
        if (userService.save(user)) {
            return R.success("新增成功");
        }else {
            return R.error("新增失败");
        }
    }

    //更新用户
    @PostMapping("/updateUser")
    public R<Object> updateTeacher(@RequestBody User user){
        //设置用户手机号码就是登录账号
        user.setAccount(user.getPhone());
        if (userService.saveOrUpdate(user)){
            return R.success("修改成功");
        }else {
            return R.error("修改失败");
        }
    }
    @DeleteMapping("/delete")
    public R<Object> deleteOne(@RequestParam int id){
        if (userService.removeById(id)){
            return R.success("删除成功");

        }else {
            return R.error("删除失败");
        }
    }

    @DeleteMapping("/delBatch")
    public R<Object> delBatch(@RequestBody List<Integer> ids){
        if (userService.removeByIds(ids)) {
            return R.success("批量删除成功");
        }else {
            return R.error("批量删除失败");
        }
    }

    /**
     * 导出接口
     */
    @GetMapping("/export")
    public R<Object> export(HttpServletResponse response) throws Exception {
        // 从数据库查询出所有的数据
        List<User> list = userService.list();
        // 通过工具类创建writer 写出到磁盘路径
//        ExcelWriter writer = ExcelUtil.getWriter(filesUploadPath + "/用户信息.xlsx");
        // 在内存操作，写出到浏览器
        ExcelWriter writer = ExcelUtil.getWriter(true);
        //自定义标题别名
        writer.addHeaderAlias("id", "id");
        writer.addHeaderAlias("account", "账号");
        writer.addHeaderAlias("username", "用户名");
        writer.addHeaderAlias("password", "密码");
        writer.addHeaderAlias("email", "邮箱");
        writer.addHeaderAlias("phone", "电话");
        writer.addHeaderAlias("address", "地址");
        writer.addHeaderAlias("CreateTime", "创建时间");
        writer.addHeaderAlias("role", "角色");
        // 一次性写出list内的对象到excel，使用默认样式，强制输出标题
        writer.write(list, true);
        // 设置浏览器响应的格式
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8");
        String fileName = URLEncoder.encode("用户信息", "UTF-8");
        response.setHeader("Content-Disposition", "attachment;filename=" + fileName + ".xlsx");

        ServletOutputStream out = response.getOutputStream();
        writer.flush(out, true);
        out.close();
        writer.close();

        return R.success("成功");
    }

    /**
     * excel 导入
     * @param file
     * @throws Exception
     */
    @PostMapping("/import")
    public R<Object> imp(MultipartFile file) throws Exception {
        InputStream inputStream = file.getInputStream();
        ExcelReader reader = ExcelUtil.getReader(inputStream);
        // 方式1：(推荐) 通过 javabean的方式读取Excel内的对象，但是要求表头必须是英文，跟javabean的属性要对应起来
//        List<User> list = reader.readAll(User.class);

        // 方式2：忽略表头的中文，直接读取表的内容
        List<List<Object>> list = reader.read(1);
        List<User> users = CollUtil.newArrayList();
        for (List<Object> row : list) {
            User user = new User();
            user.setUsername(row.get(1).toString());
            user.setEmail(row.get(2).toString());
            user.setPhone(row.get(3).toString());
            user.setAddress(row.get(4).toString());
            user.setRole(row.get(5).toString());
            users.add(user);
        }
        if(userService.saveBatch(users)){
            return R.success("导入成功");

        }else {
            return R.error("导入失败");
        }
    }

}
