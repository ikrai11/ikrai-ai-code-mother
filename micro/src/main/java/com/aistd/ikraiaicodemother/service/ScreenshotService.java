package com.aistd.ikraiaicodemother.service;

/**
 * 截图服务
 */
public interface ScreenshotService {
    /**
     * 生成并上传网页截图
     * @param webUrl 网页URL
     * @return 截图URL
     */
    String generateAndUploadScreenshot(String webUrl);
}
