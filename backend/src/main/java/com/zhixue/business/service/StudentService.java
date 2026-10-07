package com.zhixue.business.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhixue.business.domain.Student;
import com.zhixue.business.excel.StudentExcelVO;
import com.zhixue.business.mapper.StudentMapper;
import com.zhixue.common.core.PageQuery;
import com.zhixue.common.exception.ServiceException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 学员服务（含 Excel 导入导出）
 */
@Service
public class StudentService extends ServiceImpl<StudentMapper, Student> {

    public Page<Student> pageStudent(PageQuery query, String keyword) {
        return lambdaQuery()
                .like(StrUtil.isNotBlank(keyword), Student::getName, keyword)
                .orderByDesc(Student::getId)
                .page(query.toMpPage());
    }

    /** 导出数据 */
    public List<StudentExcelVO> exportList() {
        return list().stream().map(s -> {
            StudentExcelVO vo = new StudentExcelVO();
            BeanUtil.copyProperties(s, vo);
            return vo;
        }).toList();
    }

    /** 导入：逐行校验，返回成功条数 */
    public int importStudents(List<StudentExcelVO> rows) {
        if (rows == null || rows.isEmpty()) {
            throw new ServiceException("导入数据为空");
        }
        List<Student> toSave = new ArrayList<>();
        for (int i = 0; i < rows.size(); i++) {
            StudentExcelVO vo = rows.get(i);
            int rowNum = i + 2; // 含表头，第一条数据在第2行
            if (StrUtil.isBlank(vo.getName())) {
                throw new ServiceException("第 " + rowNum + " 行：姓名不能为空");
            }
            if (StrUtil.isBlank(vo.getPhone())) {
                throw new ServiceException("第 " + rowNum + " 行：手机号不能为空");
            }
            Student student = new Student();
            BeanUtil.copyProperties(vo, student);
            student.setStatus(0);
            toSave.add(student);
        }
        saveBatch(toSave);
        return toSave.size();
    }
}
