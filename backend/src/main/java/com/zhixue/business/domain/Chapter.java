package com.zhixue.business.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zhixue.common.core.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.*;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("chapter")
public class Chapter extends BaseEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    @NotNull
    @Positive
    private Long courseId;
    @NotBlank
    @Size(max = 100)
    private String title;
    @NotNull
    @Min(1)
    private Integer sortOrder;
    @NotNull
    @Min(0)
    private Integer duration;   // 时长（分钟）
    @Size(max = 15000)
    private String content;    // 纯文本章节讲义
}
