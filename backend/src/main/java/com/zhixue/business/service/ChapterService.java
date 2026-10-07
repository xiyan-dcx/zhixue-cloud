package com.zhixue.business.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhixue.business.domain.Chapter;
import com.zhixue.business.mapper.ChapterMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 课程章节服务
 */
@Service
public class ChapterService extends ServiceImpl<ChapterMapper, Chapter> {

    public List<Chapter> listByCourse(Long courseId) {
        return lambdaQuery()
                .eq(Chapter::getCourseId, courseId)
                .orderByAsc(Chapter::getSortOrder)
                .orderByAsc(Chapter::getId)
                .list();
    }
}
