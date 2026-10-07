package com.zhixue.common.core;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.Data;
import java.util.List;

@Data
public class PageResult<T> {
    private long total;
    private List<T> rows;

    public static <T> PageResult<T> of(IPage<T> page) {
        PageResult<T> pr = new PageResult<>();
        pr.setTotal(page.getTotal());
        pr.setRows(page.getRecords());
        return pr;
    }
}
