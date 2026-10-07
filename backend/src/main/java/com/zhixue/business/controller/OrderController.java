package com.zhixue.business.controller;

import com.zhixue.business.domain.CourseOrder;
import com.zhixue.business.service.OrderService;
import com.zhixue.common.core.PageQuery;
import com.zhixue.common.core.PageResult;
import com.zhixue.common.core.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 订单管理
 */
@Tag(name = "订单管理")
@RestController
@RequestMapping("/business/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @Operation(summary = "分页查询订单")
    @GetMapping("/page")
    @PreAuthorize("hasAnyAuthority('admin','order:list')")
    public R<PageResult<CourseOrder>> page(PageQuery query,
                                           @RequestParam(required = false) String keyword) {
        return R.ok(PageResult.of(orderService.pageOrder(query, keyword)));
    }

    @Operation(summary = "订单详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('admin','order:list')")
    public R<CourseOrder> detail(@PathVariable Long id) {
        return R.ok(orderService.getById(id));
    }

    @Operation(summary = "新增订单")
    @PostMapping
    @PreAuthorize("hasAnyAuthority('admin','order:add')")
    public R<Void> add(@RequestBody CourseOrder order) {
        orderService.save(order);
        return R.ok();
    }

    @Operation(summary = "修改订单")
    @PutMapping
    @PreAuthorize("hasAnyAuthority('admin','order:edit')")
    public R<Void> update(@RequestBody CourseOrder order) {
        orderService.updateById(order);
        return R.ok();
    }

    @Operation(summary = "删除订单")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('admin','order:remove')")
    public R<Void> remove(@PathVariable Long id) {
        orderService.removeById(id);
        return R.ok();
    }
}
