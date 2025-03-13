package com.example.intelligentdispatchingsystem.untils;

import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;

public class CodeGenerator {
    public static void main(String[] args) {
        FastAutoGenerator.create(
                        "jdbc:mysql://localhost:3306/dispatch_system?useSSL=false&serverTimezone=UTC",
                        "root",
                        "123456")
                .globalConfig(builder -> {
                    builder.author("lm") // 设置作者
                            .outputDir(System.getProperty("user.dir") + "/src/main/java") // 代码生成路径
                            .commentDate("yyyy-MM-dd"); // 注释日期
                })
                .packageConfig(builder -> {
                    builder.parent("com.example") // 设置包名
                            .moduleName("intelligentdispatchingsystem") // 模块名
                            .pathInfo(null); // 不生成 XML
                })
                .strategyConfig(builder -> {
                    builder.addInclude("user") // 设置要生成的表
                            .addInclude("employee")
                            .addInclude("inventory")
                            .addTablePrefix("t_"); // 忽略表前缀（如 t_user → User）
                    builder.controllerBuilder().enableHyphenStyle();// 开启驼峰转连字符
                    builder.entityBuilder().enableLombok();
                })
                .templateEngine(new FreemarkerTemplateEngine()) // 使用 FreeMarker 模板
                .execute();
    }
}
