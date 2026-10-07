package com.zhixue.business.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zhixue.common.core.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("course")
public class Course extends BaseEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String cover;
    private Long teacherId;
    private String teacherName;
    private String category;
    private BigDecimal price;
    private String intro;
    private Integer studentCount;
    private Integer status;   // 0草稿 1上架 2下架
}
