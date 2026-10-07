package com.zhixue.business.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhixue.business.domain.CourseOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Mapper
public interface CourseOrderMapper extends BaseMapper<CourseOrder> {

    /** 已支付总营收 */
    @Select("SELECT IFNULL(SUM(amount), 0) FROM course_order WHERE status = 1 AND deleted = 0")
    BigDecimal sumPaidRevenue();

    /** 近30天每日报名（订单）数 */
    @Select("""
            SELECT DATE_FORMAT(pay_time, '%Y-%m-%d') AS day, COUNT(*) AS cnt
            FROM course_order
            WHERE status = 1 AND deleted = 0 AND pay_time >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)
            GROUP BY day ORDER BY day
            """)
    List<Map<String, Object>> enrollTrend();

    /** 课程销量 Top10（窗口函数 ROW_NUMBER 排名） */
    @Select("""
            SELECT course_title AS courseTitle, cnt, revenue FROM (
                SELECT course_title, COUNT(*) AS cnt, SUM(amount) AS revenue,
                       ROW_NUMBER() OVER (ORDER BY COUNT(*) DESC) AS rn
                FROM course_order
                WHERE status = 1 AND deleted = 0
                GROUP BY course_title
            ) t WHERE rn <= 10
            """)
    List<Map<String, Object>> courseTop10();

    /** 各讲师营收占比 */
    @Select("""
            SELECT teacher_name AS teacherName, SUM(amount) AS revenue
            FROM course_order
            WHERE status = 1 AND deleted = 0
            GROUP BY teacher_name ORDER BY revenue DESC
            """)
    List<Map<String, Object>> teacherRevenue();

    /** 月营收及环比（窗口函数 LAG） */
    @Select("""
            SELECT month, revenue,
                   LAG(revenue) OVER (ORDER BY month) AS prevRevenue,
                   ROUND((revenue - LAG(revenue) OVER (ORDER BY month))
                         / LAG(revenue) OVER (ORDER BY month) * 100, 2) AS growthRate
            FROM (
                SELECT DATE_FORMAT(pay_time, '%Y-%m') AS month, SUM(amount) AS revenue
                FROM course_order
                WHERE status = 1 AND deleted = 0
                GROUP BY DATE_FORMAT(pay_time, '%Y-%m')
            ) t ORDER BY month
            """)
    List<Map<String, Object>> monthRevenueGrowth();
}
