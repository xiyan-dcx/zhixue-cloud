package com.zhixue.system.service;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhixue.common.core.PageQuery;
import com.zhixue.system.domain.SysUser;
import com.zhixue.system.mapper.SysUserMapper;
import org.springframework.stereotype.Service;

/**
 * 用户管理服务
 */
@Service
public class SysUserService extends ServiceImpl<SysUserMapper, SysUser> {

    public Page<SysUser> pageUser(PageQuery query, String keyword) {
        return lambdaQuery()
                .like(StrUtil.isNotBlank(keyword), SysUser::getUsername, keyword)
                .or()
                .like(StrUtil.isNotBlank(keyword), SysUser::getNickname, keyword)
                .orderByDesc(SysUser::getId)
                .page(query.toMpPage());
    }
}
