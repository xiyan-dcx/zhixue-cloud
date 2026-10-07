package com.zhixue.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhixue.system.domain.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {
    @Update("UPDATE sys_user SET nickname=#{nickname}, update_time=NOW() WHERE id=#{id} AND deleted=0")
    int updateOwnNickname(@Param("id") Long id, @Param("nickname") String nickname);

    @Update("UPDATE sys_user SET avatar=#{avatar}, update_time=NOW() WHERE id=#{id} AND deleted=0")
    int updateOwnAvatar(@Param("id") Long id, @Param("avatar") String avatar);

    @Update("UPDATE sys_user SET password=#{password}, token_version=token_version+1, update_time=NOW() WHERE id=#{id} AND password=#{oldHash} AND deleted=0")
    int updateOwnPassword(@Param("id") Long id, @Param("oldHash") String oldHash, @Param("password") String password);

    @Select("""
            SELECT DISTINCT m.perms FROM sys_menu m
            JOIN sys_role_menu rm ON m.id = rm.menu_id
            JOIN sys_user u ON u.role_id = rm.role_id
            WHERE u.id = #{userId} AND m.perms IS NOT NULL AND m.perms <> '' AND m.deleted = 0
            """)
    List<String> selectPermsByUserId(Long userId);

    @Select("SELECT r.role_key FROM sys_role r JOIN sys_user u ON u.role_id = r.id WHERE u.id = #{userId}")
    String selectRoleKeyByUserId(Long userId);
}
