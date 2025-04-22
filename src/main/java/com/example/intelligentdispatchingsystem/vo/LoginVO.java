package com.example.intelligentdispatchingsystem.vo;

import com.example.intelligentdispatchingsystem.entity.role.Employee;
import com.example.intelligentdispatchingsystem.entity.role.User;
import lombok.Data;

@Data
public class LoginVO {
    private Integer id;
    private String token;
    private User user;
    private Employee employee;
    private String role;
}
