package com.lrs.core.system.controller;

import com.lrs.core.config.CommonConfig;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Controller;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.*;
import java.nio.file.Paths;

/**
 * 上传图片展示类
 */
@Slf4j
@Controller
@RequestMapping("/show")
@RequiredArgsConstructor
public class ShowController {

    private final CommonConfig commonConfig;


    private String uploadRootPath;

    @PostConstruct
    public void init() {
        this.uploadRootPath = commonConfig.getUpload().getRoot();
        log.info("图片展示控制器初始化完成，上传根路径: {}", uploadRootPath);
    }

    //显示本地图片
    @GetMapping(value = "/{filename:.+}")
    public void getImg(@PathVariable("filename") String filename, HttpServletResponse response) throws IOException {
        String address = buildFilePath(filename);
        sendImageToResponse(response, address);
    }

    @GetMapping(value = "/{folderName}/{filename:.+}")
    public void getImg(@PathVariable("folderName") String folderName, @PathVariable("filename") String filename, HttpServletResponse response) throws IOException {
        String address = buildFilePath(folderName,filename);
        sendImageToResponse(response, address);
    }

    @GetMapping(value = "/{folderName}/{folderName2}/{filename:.+}")
    public void getImg(@PathVariable("folderName") String folderName, @PathVariable("folderName2") String folderName2, @PathVariable("filename") String filename, HttpServletResponse response) throws IOException {
        String address = buildFilePath(folderName,folderName2,filename);
        sendImageToResponse(response, address);
    }

    // 输出图片到HTTP响应
    public void sendImageToResponse(HttpServletResponse response, String filePath) throws IOException {
        if (ObjectUtils.isEmpty(filePath)) {
            throw new IllegalArgumentException("文件路径不能为空.");
        }
        File filePic = new File(filePath);
        if (!filePic.exists()) {
            filePic = new File(System.getProperty("user.dir") + "/" + filePath);
        }
        if (!filePic.exists() || !filePic.isFile()) {
            throw new FileNotFoundException("文件不存在，路径: " + filePath);
        }
        response.setContentType(getContentType(filePath));
        try (FileInputStream fis = new FileInputStream(filePic);
             OutputStream outputStream = response.getOutputStream()) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        } catch (FileNotFoundException e) {
            throw new IOException("查找文件失败：err=", e);
        }
    }

    /**
     * 构建文件路径
     */
    private String buildFilePath(String... pathSegments) {
        if (pathSegments == null || pathSegments.length == 0) {
            throw new IllegalArgumentException("路径段不能为空");
        }
        // 安全验证：检查路径段是否包含路径遍历字符
        for (String segment : pathSegments) {
            if (segment.contains("..") || segment.contains("/") || segment.contains("\\")) {
                throw new SecurityException("非法路径参数: " + segment);
            }
        }
        return Paths.get(uploadRootPath, pathSegments).toString();
    }

    // 根据文件扩展名获取MIME类型
    private String getContentType(String imagePath) {
        String extension = FilenameUtils.getExtension(imagePath).toLowerCase();
        switch (extension) {
            case "png":
                return "image/png";
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            default:
                return "application/octet-stream";
        }
    }
}
