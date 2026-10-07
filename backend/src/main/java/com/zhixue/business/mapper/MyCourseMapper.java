package com.zhixue.business.mapper;

import com.zhixue.business.domain.Course;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface MyCourseMapper {
    @Select("SELECT course_id FROM simulated_purchase WHERE user_id=#{userId}")
    java.util.List<Long> purchasedIds(Long userId);
    @Select("SELECT * FROM course WHERE id=#{id} AND deleted=0 FOR UPDATE")
    Course lockCourse(Long id);

    @Select("SELECT order_no AS orderNo, amount FROM simulated_purchase WHERE user_id=#{userId} AND course_id=#{courseId}")
    java.util.Map<String, Object> purchase(@Param("userId") Long userId, @Param("courseId") Long courseId);

    @Insert("INSERT INTO simulated_purchase(order_no,user_id,course_id,course_title,amount) VALUES(#{orderNo},#{userId},#{courseId},#{title},#{amount})")
    int savePurchase(@Param("orderNo") String orderNo, @Param("userId") Long userId, @Param("courseId") Long courseId,
                     @Param("title") String title, @Param("amount") java.math.BigDecimal amount);
    @Select("""
        SELECT c.* FROM my_course mc JOIN course c ON c.id=mc.course_id AND c.deleted=0
        WHERE mc.user_id=#{userId} ORDER BY mc.create_time DESC, mc.id DESC
        LIMIT #{limit} OFFSET #{offset}
        """)
    List<Course> list(@Param("userId") Long userId, @Param("offset") int offset, @Param("limit") int limit);

    @Select("SELECT COUNT(*) FROM my_course mc JOIN course c ON c.id=mc.course_id AND c.deleted=0 WHERE mc.user_id=#{userId}")
    long count(Long userId);

    @Insert("""
        INSERT INTO my_course(user_id,course_id,create_time)
        SELECT #{userId},id,NOW() FROM course WHERE id=#{courseId} AND status=1 AND deleted=0
        ON DUPLICATE KEY UPDATE course_id=VALUES(course_id)
        """)
    int join(@Param("userId") Long userId, @Param("courseId") Long courseId);

    @Delete("DELETE FROM my_course WHERE user_id=#{userId} AND course_id=#{courseId}")
    int remove(@Param("userId") Long userId, @Param("courseId") Long courseId);
}
