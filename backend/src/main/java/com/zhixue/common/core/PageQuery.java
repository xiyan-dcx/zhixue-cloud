package com.zhixue.common.core;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;

@Data
public class PageQuery {
    private Integer pageNum = 1;
    private Integer pageSize = 10;

    public <T> Page<T> toMpPage() {
        long num = (pageNum == null || pageNum < 1) ? 1 : pageNum;
        long size = (pageSize == null || pageSize < 1) ? 10 : pageSize;
        return new Page<>(num, size);
    }
}
