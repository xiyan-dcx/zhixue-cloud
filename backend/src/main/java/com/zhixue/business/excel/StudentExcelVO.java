package com.zhixue.business.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;

/**
 * 学员导入 / 导出 Excel 模型
 */
@Data
@ColumnWidth(18)
public class StudentExcelVO {

    @ExcelProperty("姓名")
    private String name;

    @ExcelProperty("手机号")
    private String phone;

    @ExcelProperty("邮箱")
    private String email;

    @ExcelProperty("性别")
    private String gender;
}
