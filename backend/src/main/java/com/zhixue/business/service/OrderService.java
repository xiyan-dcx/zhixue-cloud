package com.zhixue.business.service;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhixue.business.domain.CourseOrder;
import com.zhixue.business.mapper.CourseOrderMapper;
import com.zhixue.common.core.PageQuery;
import org.springframework.stereotype.Service;

/**
 * 订单服务
 */
@Service
public class OrderService extends ServiceImpl<CourseOrderMapper, CourseOrder> {

    public Page<CourseOrder> pageOrder(PageQuery query, String keyword) {
        return lambdaQuery()
                .like(StrUtil.isNotBlank(keyword), CourseOrder::getOrderNo, keyword)
                .or()
                .like(StrUtil.isNotBlank(keyword), CourseOrder::getStudentName, keyword)
                .orderByDesc(CourseOrder::getId)
                .page(query.toMpPage());
    }
}
