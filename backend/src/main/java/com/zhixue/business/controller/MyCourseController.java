package com.zhixue.business.controller;

import com.zhixue.business.mapper.MyCourseMapper;
import com.zhixue.business.service.CourseService;
import com.zhixue.common.core.R;
import com.zhixue.common.exception.ServiceException;
import com.zhixue.common.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.math.BigDecimal;
import org.springframework.transaction.annotation.Transactional;

@RestController
@RequestMapping("/my/courses")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('student')")
public class MyCourseController {
    private final MyCourseMapper courses;
    private final CourseService courseService;

    @GetMapping
    public R<Map<String, Object>> list(@RequestParam(defaultValue="1") int pageNum,
                                     @RequestParam(defaultValue="12") int pageSize) {
        if (pageNum < 1 || pageNum > 100000 || pageSize < 1 || pageSize > 50) throw new ServiceException("分页参数不正确");
        Long userId = SecurityUtils.getUserId();
        return R.ok(Map.of("rows", courses.list(userId, (pageNum-1)*pageSize, pageSize), "total", courses.count(userId)));
    }

    @PostMapping("/{courseId}")
    @Transactional
    public R<Map<String, Object>> join(@PathVariable Long courseId, @RequestBody(required=false) PurchaseBody body) {
        var course = courses.lockCourse(courseId);
        if (course == null || !Integer.valueOf(1).equals(course.getStatus())) throw new ServiceException("只能加入已上架课程");
        Long userId = SecurityUtils.getUserId();
        var previous = courses.purchase(userId, courseId);
        if (previous != null && !previous.isEmpty()) {
            throw new ServiceException(409, "已购买该课程，请前往我的课程，不允许重复购买");
        }
        BigDecimal price = course.getPrice();
        if (price == null || price.signum() < 0) throw new ServiceException("课程价格配置不正确");
        if (price.signum() > 0 && (body == null || !body.confirmed())) throw new ServiceException("请确认模拟购买后加入课程");
        if (price.signum() > 0 && (body.expectedAmount() == null || price.compareTo(body.expectedAmount()) != 0))
            throw new ServiceException("课程价格已更新，请刷新后重新确认");
        String orderNo = "DEMO-" + java.util.UUID.randomUUID().toString().replace("-", "");
        courses.savePurchase(orderNo, userId, courseId, course.getTitle(), price);
        if (courses.join(userId, courseId) == 0) throw new ServiceException("开通课程失败，请重试");
        return R.ok(Map.of("orderNo", orderNo, "amount", price, "alreadyPurchased", false));
    }

    public record PurchaseBody(boolean confirmed, BigDecimal expectedAmount) {}

    @GetMapping("/purchased")
    public R<java.util.List<Long>> purchased() {
        return R.ok(courses.purchasedIds(SecurityUtils.getUserId()));
    }

    @PostMapping("/{courseId}/restore")
    @Transactional
    public R<Void> restore(@PathVariable Long courseId) {
        var course = courses.lockCourse(courseId);
        if (course == null || !Integer.valueOf(1).equals(course.getStatus())) throw new ServiceException("课程暂不可加入");
        Long userId = SecurityUtils.getUserId();
        var purchase = courses.purchase(userId, courseId);
        if (purchase == null || purchase.isEmpty()) throw new ServiceException(403, "没有该课程的购买记录");
        courses.join(userId, courseId);
        return R.ok();
    }

    @DeleteMapping("/{courseId}")
    public R<Void> remove(@PathVariable Long courseId) {
        courses.remove(SecurityUtils.getUserId(), courseId);
        return R.ok();
    }
}
