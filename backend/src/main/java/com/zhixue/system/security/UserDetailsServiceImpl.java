package com.zhixue.system.security;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zhixue.common.security.LoginUser;
import com.zhixue.system.domain.SysUser;
import com.zhixue.system.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final SysUserMapper userMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUser user = userMapper.selectOne(
                Wrappers.<SysUser>lambdaQuery().eq(SysUser::getUsername, username));
        if (user == null) throw new UsernameNotFoundException("用户不存在");

        List<String> perms = userMapper.selectPermsByUserId(user.getId());
        String roleKey = userMapper.selectRoleKeyByUserId(user.getId());

        LoginUser loginUser = new LoginUser();
        loginUser.setUserId(user.getId());
        loginUser.setUsername(user.getUsername());
        loginUser.setPassword(user.getPassword());
        loginUser.setNickname(user.getNickname());
        loginUser.setAvatar(user.getAvatar());
        loginUser.setTokenVersion(user.getTokenVersion());
        loginUser.setStatus(user.getStatus());
        loginUser.setRoleKey(roleKey);
        loginUser.setPermissions(new HashSet<>(perms));
        return loginUser;
    }
}
