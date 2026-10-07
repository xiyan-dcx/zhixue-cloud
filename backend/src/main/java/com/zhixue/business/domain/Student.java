package com.zhixue.business.domain;

import com.baomidou.mybatisplus.annotation.*;
import com.zhixue.common.core.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("student")
public class Student extends BaseEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String phone;
    private String email;
    private String gender;
    private Integer status;
}
