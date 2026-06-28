package com.traffic.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.traffic.entity.User;
import com.traffic.mapper.UserMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用户业务逻辑
 */
@Service
public class UserService extends ServiceImpl<UserMapper, User> {

    /**
     * 用户注册
     */
    public User register(String username, String password) {
        // 检查用户名是否已存在
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        if (count(wrapper) > 0) {
            return null; // 用户名已存在
        }
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setRole("USER");
        save(user);
        return user;
    }

    /**
     * 用户登录
     */
    public User login(String username, String password) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username)
               .eq(User::getPassword, password);
        return getOne(wrapper);
    }

    /**
     * 获取所有用户列表
     */
    public List<User> listAll() {
        return list();
    }

    /**
     * 根据用户名查询
     */
    public User getByUsername(String username) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        return getOne(wrapper);
    }
}
