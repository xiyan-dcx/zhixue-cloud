package com.zhixue.system.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zhixue.common.core.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 角色
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_role")
@Schema(description = "角色")
public class SysRole extends BaseEntity {
    @Schema(description = "角色ID")
    @TableId(type = IdType.AUTO)
    private Long id;

    @Schema(description = "角色名称")
    private String roleName;

    @Schema(description = "角色标识，如 admin、teacher、student")
    private String roleKey;

    @Schema(description = "显示顺序")
    @TableField(exist = false)
    private Integer orderNum;

    @Schema(description = "状态：0正常 1停用")
    private Integer status;
}
