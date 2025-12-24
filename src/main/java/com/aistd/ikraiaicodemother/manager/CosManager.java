package com.aistd.ikraiaicodemother.manager;

import com.aistd.ikraiaicodemother.config.CosClientConfig;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;

@Slf4j
@Component
public class CosManager {

    @Resource
    private COSClient cosClient;

    @Resource
    private CosClientConfig cosClientConfig;

    /**
     * 上传对象到COS
     *
     * @param key 对象键（路径）
     * @param file 本地文件
     * @return PutObjectResult
     */
    public PutObjectResult putObject(String key, File file) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), key, file);
        return cosClient.putObject(putObjectRequest);
    }
    /**
     * 上传文件到 COS 并返回访问 URL
     *
     * @param key  COS对象键（完整路径）
     * @param file 要上传的文件
     * @return 文件的访问URL，失败返回null
     */
    public String uploadFile(String key, File file) {
       //上传文件
        PutObjectResult result = putObject(key, file);
        if (result !=null) {
            //构建访问URL
            String url=String.format("%s/%s", cosClientConfig.getHost(), key);
            log.info("上传文件到COS成功，key: {}, url: {}", file.getName(), url);
            return url;
        }else {
            log.error("上传文件到COS失败，key: {}", key);
            return null;
        }
    }
}
