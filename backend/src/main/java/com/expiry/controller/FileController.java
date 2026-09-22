package com.expiry.controller;

import com.expiry.common.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/api/file")
public class FileController {

    @Value("${upload.path}")
    private String uploadPath;

    @Value("${upload.url-prefix}")
    private String urlPrefix;

    @PostMapping("/upload")
    public Result<String> upload(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return Result.error("请选择文件");
        }
        String originalFilename = file.getOriginalFilename();
        String ext = originalFilename != null ? originalFilename.substring(originalFilename.lastIndexOf(".")) : ".jpg";
        String newFilename = UUID.randomUUID().toString().replace("-", "") + ext;

        String datePath = cn.hutool.core.date.DateUtil.format(new java.util.Date(), "yyyy/MM/dd");
        File dir = new File(uploadPath + datePath).getAbsoluteFile();
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File dest = new File(dir, newFilename);
        file.transferTo(dest);
        String url = urlPrefix + datePath + "/" + newFilename;
        return Result.success("上传成功", url);
    }
}
