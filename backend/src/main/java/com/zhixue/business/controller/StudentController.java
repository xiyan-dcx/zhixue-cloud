package com.zhixue.business.controller;

import com.alibaba.excel.EasyExcel;
import com.zhixue.business.domain.Student;
import com.zhixue.business.excel.StudentExcelVO;
import com.zhixue.business.service.StudentService;
import com.zhixue.common.core.PageQuery;
import com.zhixue.common.core.PageResult;
import com.zhixue.common.core.R;
import com.zhixue.common.exception.ServiceException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 学员管理（含 Excel 导入导出、模板下载）
 */
@Tag(name = "学员管理")
@RestController
@RequestMapping("/business/student")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;

    @Operation(summary = "分页查询学员")
    @GetMapping("/page")
    @PreAuthorize("hasAnyAuthority('admin','student:list')")
    public R<PageResult<Student>> page(PageQuery query,
                                       @RequestParam(required = false) String keyword) {
        return R.ok(PageResult.of(studentService.pageStudent(query, keyword)));
    }

    @Operation(summary = "新增学员")
    @PostMapping
    @PreAuthorize("hasAnyAuthority('admin','student:add')")
    public R<Void> add(@RequestBody Student student) {
        studentService.save(student);
        return R.ok();
    }

    @Operation(summary = "修改学员")
    @PutMapping
    @PreAuthorize("hasAnyAuthority('admin','student:edit')")
    public R<Void> update(@RequestBody Student student) {
        studentService.updateById(student);
        return R.ok();
    }

    @Operation(summary = "删除学员")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('admin','student:remove')")
    public R<Void> remove(@PathVariable Long id) {
        studentService.removeById(id);
        return R.ok();
    }

    @Operation(summary = "导出学员 Excel")
    @GetMapping("/export")
    @PreAuthorize("hasAnyAuthority('admin','student:list')")
    public void export(HttpServletResponse response) throws IOException {
        writeExcel(response, "学员列表", studentService.exportList());
    }

    @Operation(summary = "下载导入模板")
    @GetMapping("/template")
    @PreAuthorize("hasAnyAuthority('admin','student:import')")
    public void template(HttpServletResponse response) throws IOException {
        writeExcel(response, "学员导入模板", List.of());
    }

    @Operation(summary = "导入学员 Excel")
    @PostMapping("/import")
    @PreAuthorize("hasAnyAuthority('admin','student:import')")
    public R<String> importData(@RequestParam("file") MultipartFile file) {
        try {
            List<StudentExcelVO> rows = EasyExcel.read(file.getInputStream())
                    .head(StudentExcelVO.class)
                    .sheet()
                    .doReadSync();
            int count = studentService.importStudents(rows);
            return R.ok("成功导入 " + count + " 条数据");
        } catch (IOException e) {
            throw new ServiceException("文件读取失败");
        }
    }

    private void writeExcel(HttpServletResponse response, String fileName,
                            List<StudentExcelVO> data) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        String name = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replace("+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + name + ".xlsx");
        EasyExcel.write(response.getOutputStream(), StudentExcelVO.class)
                .sheet("学员")
                .doWrite(data);
    }
}
