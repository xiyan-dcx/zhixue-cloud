package com.zhixue.business.service;

import com.zhixue.business.mapper.CourseMapper;
import com.zhixue.business.mapper.CourseOrderMapper;
import com.zhixue.business.mapper.StudentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据看板服务
 */
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final CourseMapper courseMapper;
    private final StudentMapper studentMapper;
    private final CourseOrderMapper orderMapper;

    /** 顶部概览卡片 */
    public Map<String, Object> overview() {
        Map<String, Object> map = new HashMap<>();
        map.put("courseCount", courseMapper.selectCount(null));
        map.put("studentCount", studentMapper.selectCount(null));
        map.put("orderCount", orderMapper.selectCount(null));
        map.put("totalRevenue", orderMapper.sumPaidRevenue());
        return map;
    }

    public List<Map<String, Object>> enrollTrend() {
        return orderMapper.enrollTrend();
    }

    public List<Map<String, Object>> courseTop10() {
        return orderMapper.courseTop10();
    }

    public List<Map<String, Object>> teacherRevenue() {
        return orderMapper.teacherRevenue();
    }

    public List<Map<String, Object>> monthRevenueGrowth() {
        return orderMapper.monthRevenueGrowth();
    }
}
