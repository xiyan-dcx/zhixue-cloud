package com.zhixue.business.controller;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import com.zhixue.common.core.R;
import com.zhixue.common.exception.ServiceException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 文件上传（本地存储）
 */
@Tag(name = "文件管理")
@RestController
@RequestMapping("/file")
public class FileController {

    @Value("${zhixue.upload.path}")
    private String uploadPath;

    @Operation(summary = "上传文件")
    @PostMapping("/upload")
    public R<Map<String, String>> upload(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ServiceException("上传文件不能为空");
        }
        String ext = FileUtil.extName(file.getOriginalFilename());
        String date = DateUtil.format(new Date(), "yyyyMMdd");
        String fileName = IdUtil.fastSimpleUUID() + (ext == null ? "" : "." + ext);
        File dest = new File(uploadPath, date + File.separator + fileName);
        FileUtil.mkParentDirs(dest);
        try {
            file.transferTo(dest.getAbsoluteFile());
        } catch (IOException e) {
            throw new ServiceException("文件保存失败");
        }
        Map<String, String> result = new HashMap<>();
        result.put("url", "/uploads/" + date + "/" + fileName);
        result.put("name", file.getOriginalFilename());
        return R.ok(result);
    }
}
