package com.example.intelligentdispatchingsystem.vo;

import com.example.intelligentdispatchingsystem.entity.User;
import lombok.Data;

@Data
public class LoginVO {
    private Integer id;
    private String token;
    private User user;
}
