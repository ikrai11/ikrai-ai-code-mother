package com.aistd.ikraiaicodemother.controller;

import com.aistd.ikraiaicodemother.common.BaseResponse;
import com.aistd.ikraiaicodemother.common.ResultUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/health")
public class HealthController {

    @GetMapping("/")
    public BaseResponse<String> healthCheck() {
        //接口网址http://localhost:8123/api/doc.html#/home
        return ResultUtils.success("ok");
    }
}

