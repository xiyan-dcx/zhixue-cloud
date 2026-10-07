package com.zhixue.system.service;

import com.zhixue.system.domain.SysMenu;
import com.zhixue.system.mapper.SysMenuMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 菜单服务：构建用户动态路由树
 */
@Service
@RequiredArgsConstructor
public class SysMenuService {

    private final SysMenuMapper menuMapper;

    public List<SysMenu> buildRouters(Long userId, String roleKey) {
        List<SysMenu> menus = menuMapper.selectMenusByUserId(userId);   // 只查该用户有权的菜单
        if (!"admin".equals(roleKey) && !"teacher".equals(roleKey)) {
            menus = menus.stream()
                    .filter(menu -> !isDashboard(menu.getPath()))
                    .toList();
        }
        return buildTree(menus, 0L);
    }

    private boolean isDashboard(String path) {
        return path != null && "dashboard".equals(path.replaceAll("^/+|/+$", ""));
    }

    private List<SysMenu> buildTree(List<SysMenu> all, Long parentId) {
        List<SysMenu> children = new ArrayList<>();
        for (SysMenu m : all) {
            if (parentId.equals(m.getParentId())) {
                m.setChildren(buildTree(all, m.getId()));   // 递归找子菜单
                children.add(m);
            }
        }
        return children;
    }
}
