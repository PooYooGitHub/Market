package org.shyu.marketserviceuser.controller;

import org.shyu.marketcommon.result.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 健康检查控制器
 */
@RestController
@RequestMapping("/health")
@Api(tags = "健康检查")
public class HealthController {

    @GetMapping
    @ApiOperation("服务健康检查")
    public Result<Map<String, Object>> health() {
        Map<String, Object> data = new HashMap<>();
        data.put("service", "market-service-user");
        data.put("status", "UP");
        data.put("timestamp", LocalDateTime.now());
        data.put("message", "用户服务运行正常");
        return Result.success(data);
    }
}

