package com.zhixue.business.service;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhixue.business.domain.Course;
import com.zhixue.business.mapper.CourseMapper;
import com.zhixue.common.core.PageQuery;
import com.zhixue.common.exception.ServiceException;
import org.springframework.stereotype.Service;

/**
 * 课程服务
 */
@Service
public class CourseService extends ServiceImpl<CourseMapper, Course> {

    public Page<Course> pageCourse(PageQuery query, String keyword, Integer status) {
        return lambdaQuery()
                .like(StrUtil.isNotBlank(keyword), Course::getTitle, keyword)
                .eq(status != null, Course::getStatus, status)
                .orderByDesc(Course::getId)
                .page(query.toMpPage());
    }

    public void changeStatus(Long id, Integer status) {
        Course course = getById(id);
        if (course == null) {
            throw new ServiceException("课程不存在");
        }
        course.setStatus(status);
        updateById(course);
    }
}
