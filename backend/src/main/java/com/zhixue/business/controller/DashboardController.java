package com.zhixue.business.controller;

import com.zhixue.business.service.DashboardService;
import com.zhixue.common.core.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 数据看板
 */
@Tag(name = "数据看板")
@RestController
@RequestMapping("/business/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('admin', 'teacher')")
public class DashboardController {

    private final DashboardService dashboardService;

    @Operation(summary = "概览统计")
    @GetMapping("/overview")
    public R<Map<String, Object>> overview() {
        return R.ok(dashboardService.overview());
    }

    @Operation(summary = "近30天报名趋势")
    @GetMapping("/enrollTrend")
    public R<List<Map<String, Object>>> enrollTrend() {
        return R.ok(dashboardService.enrollTrend());
    }

    @Operation(summary = "课程销量Top10")
    @GetMapping("/courseTop10")
    public R<List<Map<String, Object>>> courseTop10() {
        return R.ok(dashboardService.courseTop10());
    }

    @Operation(summary = "讲师营收占比")
    @GetMapping("/teacherRevenue")
    public R<List<Map<String, Object>>> teacherRevenue() {
        return R.ok(dashboardService.teacherRevenue());
    }

    @Operation(summary = "月营收环比（窗口函数）")
    @GetMapping("/monthRevenue")
    public R<List<Map<String, Object>>> monthRevenue() {
        return R.ok(dashboardService.monthRevenueGrowth());
    }
}
