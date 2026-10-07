package com.zhixue.business.controller;

import com.zhixue.business.domain.Chapter;
import com.zhixue.business.service.ChapterService;
import com.zhixue.common.core.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import com.zhixue.common.exception.ServiceException;
import com.zhixue.business.service.CourseService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 课程章节管理
 */
@Tag(name = "课程章节")
@RestController
@RequestMapping("/business/chapter")
@RequiredArgsConstructor
public class ChapterController {

    private final ChapterService chapterService;
    private final CourseService courseService;
    private final com.zhixue.business.mapper.MyCourseMapper myCourses;

    @Operation(summary = "按课程查章节")
    @GetMapping("/list")
    @PreAuthorize("hasAnyAuthority('admin','course:list')")
    public R<List<Chapter>> list(@RequestParam Long courseId) {
        var authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof com.zhixue.common.security.LoginUser user
                && "student".equals(user.getRoleKey())) {
            var course = courseService.getById(courseId);
            if (course == null || !Integer.valueOf(1).equals(course.getStatus())) throw new ServiceException("课程暂不可学习");
            if (course.getPrice() != null && course.getPrice().signum() > 0 && myCourses.purchase(user.getUserId(), courseId) == null)
                throw new ServiceException(403, "请先完成模拟购买，再查看章节讲义");
        }
        return R.ok(chapterService.listByCourse(courseId));
    }

    @Operation(summary = "新增章节")
    @PostMapping
    @PreAuthorize("!hasAuthority('student') and hasAnyAuthority('admin','course:edit')")
    public R<Void> add(@Valid @RequestBody Chapter chapter) {
        if (courseService.getById(chapter.getCourseId()) == null) {
            throw new ServiceException("课程不存在");
        }
        chapter.setId(null);
        chapter.setDeleted(0);
        chapterService.save(chapter);
        return R.ok();
    }

    @Operation(summary = "修改章节")
    @PutMapping
    @PreAuthorize("!hasAuthority('student') and hasAnyAuthority('admin','course:edit')")
    public R<Void> update(@Valid @RequestBody Chapter chapter) {
        Chapter existing = chapter.getId() == null ? null : chapterService.getById(chapter.getId());
        if (existing == null) throw new ServiceException("章节不存在");
        if (!existing.getCourseId().equals(chapter.getCourseId())) {
            throw new ServiceException("不能将章节移动到其他课程");
        }
        existing.setTitle(chapter.getTitle().trim());
        existing.setSortOrder(chapter.getSortOrder());
        existing.setDuration(chapter.getDuration());
        existing.setContent(chapter.getContent());
        chapterService.updateById(existing);
        return R.ok();
    }

    @Operation(summary = "删除章节")
    @DeleteMapping("/{id}")
    @PreAuthorize("!hasAuthority('student') and hasAnyAuthority('admin','course:edit')")
    public R<Void> remove(@PathVariable Long id) {
        chapterService.removeById(id);
        return R.ok();
    }
}
