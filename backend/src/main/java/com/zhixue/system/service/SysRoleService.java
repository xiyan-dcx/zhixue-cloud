package com.zhixue.system.service;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhixue.common.core.PageQuery;
import com.zhixue.system.domain.SysRole;
import com.zhixue.system.mapper.SysRoleMapper;
import org.springframework.stereotype.Service;

/**
 * 角色管理服务
 */
@Service
public class SysRoleService extends ServiceImpl<SysRoleMapper, SysRole> {

    public Page<SysRole> pageRole(PageQuery query, String keyword) {
        return lambdaQuery()
                .like(StrUtil.isNotBlank(keyword), SysRole::getRoleName, keyword)
                .or()
                .like(StrUtil.isNotBlank(keyword), SysRole::getRoleKey, keyword)
                .orderByAsc(SysRole::getOrderNum)
                .page(query.toMpPage());
    }
}
