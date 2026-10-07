package com.zhixue.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhixue.system.domain.SysMenu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SysMenuMapper extends BaseMapper<SysMenu> {

    /** 查询用户可见的目录与菜单（用于前端动态路由），按显示顺序排列 */
    @Select("""
            SELECT DISTINCT m.* FROM sys_menu m
            JOIN sys_role_menu rm ON m.id = rm.menu_id
            JOIN sys_user u ON u.role_id = rm.role_id
            WHERE u.id = #{userId} AND m.menu_type IN ('M','C') AND m.status = 0 AND m.deleted = 0
            ORDER BY m.order_num
            """)
    List<SysMenu> selectMenusByUserId(@Param("userId") Long userId);
}
