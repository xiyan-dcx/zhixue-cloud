package com.zhixue.system.controller;

import com.zhixue.common.core.PageQuery;
import com.zhixue.common.core.PageResult;
import com.zhixue.common.core.R;
import com.zhixue.system.domain.SysUser;
import com.zhixue.system.service.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

/**
 * 用户管理
 */
@Tag(name = "用户管理")
@RestController
@RequestMapping("/system/user")
@RequiredArgsConstructor
public class SysUserController {

    private final SysUserService userService;
    private final PasswordEncoder passwordEncoder;

    @Operation(summary = "分页查询用户")
    @GetMapping("/page")
    @PreAuthorize("hasAnyAuthority('admin','system:user:list')")
    public R<PageResult<SysUser>> page(PageQuery query,
                                       @RequestParam(required = false) String keyword) {
        return R.ok(PageResult.of(userService.pageUser(query, keyword)));
    }

    @Operation(summary = "用户详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('admin','system:user:list')")
    public R<SysUser> detail(@PathVariable Long id) {
        return R.ok(userService.getById(id));
    }

    @Operation(summary = "新增用户")
    @PostMapping
    @PreAuthorize("hasAnyAuthority('admin','system:user:add')")
    public R<Void> add(@RequestBody SysUser user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userService.save(user);
        return R.ok();
    }

    @Operation(summary = "修改用户")
    @PutMapping
    @PreAuthorize("hasAnyAuthority('admin','system:user:edit')")
    public R<Void> update(@RequestBody SysUser user) {
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        userService.updateById(user);
        return R.ok();
    }

    @Operation(summary = "删除用户")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('admin','system:user:remove')")
    public R<Void> remove(@PathVariable Long id) {
        userService.removeById(id);
        return R.ok();
    }
}
