package com.zhixue.system.controller;

import com.zhixue.common.core.PageQuery;
import com.zhixue.common.core.PageResult;
import com.zhixue.common.core.R;
import com.zhixue.system.domain.SysRole;
import com.zhixue.system.service.SysRoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 角色管理
 */
@Tag(name = "角色管理")
@RestController
@RequestMapping("/system/role")
@RequiredArgsConstructor
public class SysRoleController {

    private final SysRoleService roleService;

    @Operation(summary = "角色列表")
    @GetMapping("/list")
    @PreAuthorize("hasAnyAuthority('admin','system:role:list')")
    public R<List<SysRole>> list() {
        return R.ok(roleService.list());
    }

    @Operation(summary = "分页查询角色")
    @GetMapping("/page")
    @PreAuthorize("hasAnyAuthority('admin','system:role:list')")
    public R<PageResult<SysRole>> page(PageQuery query,
                                       @RequestParam(required = false) String keyword) {
        return R.ok(PageResult.of(roleService.pageRole(query, keyword)));
    }

    @Operation(summary = "角色详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('admin','system:role:list')")
    public R<SysRole> detail(@PathVariable Long id) {
        return R.ok(roleService.getById(id));
    }

    @Operation(summary = "新增角色")
    @PostMapping
    @PreAuthorize("hasAnyAuthority('admin','system:role:add')")
    public R<Void> add(@RequestBody SysRole role) {
        roleService.save(role);
        return R.ok();
    }

    @Operation(summary = "修改角色")
    @PutMapping
    @PreAuthorize("hasAnyAuthority('admin','system:role:edit')")
    public R<Void> update(@RequestBody SysRole role) {
        roleService.updateById(role);
        return R.ok();
    }

    @Operation(summary = "删除角色")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('admin','system:role:remove')")
    public R<Void> remove(@PathVariable Long id) {
        roleService.removeById(id);
        return R.ok();
    }
}
