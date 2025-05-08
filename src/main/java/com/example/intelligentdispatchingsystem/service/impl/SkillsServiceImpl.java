package com.example.intelligentdispatchingsystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.intelligentdispatchingsystem.entity.info.Skills;
import com.example.intelligentdispatchingsystem.mapper.SkillsMapper;
import com.example.intelligentdispatchingsystem.service.ISkillsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author lm
 * @since 2025-03-27
 */
@Service
public class SkillsServiceImpl extends ServiceImpl<SkillsMapper, Skills> implements ISkillsService {

}
