package com.zhixue.system.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zhixue.common.core.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

/**
 * 菜单 / 权限
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_menu")
@Schema(description = "菜单")
public class SysMenu extends BaseEntity {

    @Schema(description = "菜单ID")
    @TableId(type = IdType.AUTO)
    private Long id;

    @Schema(description = "父菜单ID，0为顶级")
    private Long parentId;

    @Schema(description = "菜单名称")
    private String menuName;

    @Schema(description = "路由路径")
    private String path;

    @Schema(description = "前端组件路径")
    private String component;

    @Schema(description = "权限标识，如 course:add")
    private String perms;

    @Schema(description = "菜单类型：M目录 C菜单 F按钮")
    private String menuType;

    @Schema(description = "菜单图标")
    private String icon;

    @Schema(description = "显示顺序")
    private Integer orderNum;

    @Schema(description = "状态：0正常 1停用")
    private Integer status;

    @Schema(description = "子菜单")
    @TableField(exist = false)
    private List<SysMenu> children = new ArrayList<>();
}
