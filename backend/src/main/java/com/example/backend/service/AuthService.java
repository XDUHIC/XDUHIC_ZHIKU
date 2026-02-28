package com.example.backend.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import com.example.backend.dto.AuthDtos;
import com.example.backend.entity.User;
import com.example.backend.mapper.UserMapper;
import com.example.backend.security.JwtService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserMapper userMapper;
    private final JwtService jwtService;

    @Value("${app.hic.auth.key:}")
    private String hicAuthKey;

    public void register(AuthDtos.RegisterReq req) {
        if (userMapper.findByUsername(req.getUsername()) != null) {
            throw new RuntimeException("用户名已存在");
        }
        User u = new User();
        u.setUsername(req.getUsername());
        u.setPassword(req.getPassword());
        // 使用传入的昵称，如果为空则使用用户名作为默认昵称
        u.setNickname(req.getNickname() != null && !req.getNickname().trim().isEmpty() 
                      ? req.getNickname().trim() : req.getUsername());
        u.setStatus(1);
        userMapper.insert(u);
    }

    public Map<String, Object> login(AuthDtos.LoginReq req) {
        User u = userMapper.findByUsername(req.getUsername());
        if (u == null || u.getStatus() != 1) {
            throw new RuntimeException("用户不存在或被禁用");
        }
        if (!req.getPassword().equals(u.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }
        
        // 更新最后登录时间
        userMapper.updateLastLoginTime(u.getId());
        
        String token = jwtService.generateToken(u.getId(), u.getUsername(), "user");
        Map<String, Object> resp = new HashMap<>();
        resp.put("accessToken", token);
        Map<String, Object> user = new HashMap<>();
        user.put("id", u.getId());
        user.put("username", u.getUsername());
        user.put("nickname", u.getNickname());
        user.put("avatarUrl", u.getAvatarUrl());
        user.put("hic", u.getHic() != null ? u.getHic() : 0);
        resp.put("user", user);
        return resp;
    }

    public boolean isUsernameAvailable(String username) {
        // 验证用户名格式
        if (username == null || username.length() < 3 || username.length() > 20) {
            return false;
        }
        if (!username.matches("^[a-zA-Z0-9_]+$")) {
            return false;
        }
        
        // 检查用户名是否已存在
        return userMapper.findByUsername(username) == null;
    }

    /**
     * HIC认证：校验密钥并将当前用户hic置为1
     */
    public boolean verifyHic(String username, String key) {
        if (key == null || key.isEmpty()) {
            return false;
        }
        // 读取配置的认证密钥并进行比较
        if (hicAuthKey == null || hicAuthKey.isEmpty()) {
            throw new RuntimeException("系统未配置HIC认证密钥");
        }
        if (!key.equals(hicAuthKey)) {
            return false;
        }
        User u = userMapper.findByUsername(username);
        if (u == null) {
            throw new RuntimeException("用户不存在");
        }
        // 已认证则直接返回true
        if (u.getHic() != null && u.getHic() == 1) {
            return true;
        }
        userMapper.updateHicStatus(u.getId(), 1);
        return true;
    }
}


