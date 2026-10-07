package com.zhixue.system.controller;

import com.zhixue.common.core.R;
import com.zhixue.common.security.LoginUser;
import com.zhixue.common.utils.SecurityUtils;
import com.zhixue.system.domain.SysMenu;
import com.zhixue.system.dto.LoginBody;
import com.zhixue.system.service.AuthService;
import com.zhixue.system.service.SysMenuService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public R<Map<String, Object>> login(@Valid @RequestBody LoginBody body) {
        String token = authService.login(body);
        return R.ok(Map.of("token", token));
    }
    @GetMapping("/getInfo")
    public R<Map<String, Object>> getInfo() {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        Map<String, Object> info = new HashMap<>();
        info.put("userId", loginUser.getUserId());
        info.put("nickname", loginUser.getNickname());
        info.put("avatar", loginUser.getAvatar());
        info.put("roleKey", loginUser.getRoleKey());
        info.put("permissions", loginUser.getPermissions());
        return R.ok(info);
    }
    private final SysMenuService menuService;
    @GetMapping("/getRouters")
    public R<List<SysMenu>> getRouters() {

        LoginUser user = SecurityUtils.getLoginUser();
        return R.ok(menuService.buildRouters(user.getUserId(), user.getRoleKey()));
    }
}
