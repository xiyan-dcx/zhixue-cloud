package com.zhixue.business.controller;

import com.zhixue.business.domain.Course;
import com.zhixue.business.service.CourseService;
import com.zhixue.common.core.PageQuery;
import com.zhixue.common.core.PageResult;
import com.zhixue.common.core.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 课程管理
 */
@Tag(name = "课程管理")
@RestController
@RequestMapping("/business/course")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    @Operation(summary = "分页查询课程")
    @GetMapping("/page")
    @PreAuthorize("hasAnyAuthority('admin','course:list')")
    public R<PageResult<Course>> page(PageQuery query,
                                      @RequestParam(required = false) String keyword,
                                      @RequestParam(required = false) Integer status) {
        return R.ok(PageResult.of(courseService.pageCourse(query, keyword, status)));
    }

    @Operation(summary = "课程详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('admin','course:list')")
    public R<Course> detail(@PathVariable Long id) {
        return R.ok(courseService.getById(id));
    }

    @Operation(summary = "新增课程")
    @PostMapping
    @PreAuthorize("hasAnyAuthority('admin','course:add')")
    public R<Void> add(@RequestBody Course course) {
        courseService.save(course);
        return R.ok();
    }

    @Operation(summary = "修改课程")
    @PutMapping
    @PreAuthorize("hasAnyAuthority('admin','course:edit')")
    public R<Void> update(@RequestBody Course course) {
        courseService.updateById(course);
        return R.ok();
    }

    @Operation(summary = "删除课程")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('admin','course:remove')")
    public R<Void> remove(@PathVariable Long id) {
        courseService.removeById(id);
        return R.ok();
    }

    @Operation(summary = "上架/下架课程")
    @PutMapping("/status")
    @PreAuthorize("hasAnyAuthority('admin','course:edit')")
    public R<Void> changeStatus(@RequestParam Long id, @RequestParam Integer status) {
        courseService.changeStatus(id, status);
        return R.ok();
    }
}
