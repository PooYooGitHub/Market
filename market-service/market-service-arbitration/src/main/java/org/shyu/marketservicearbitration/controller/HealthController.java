package org.shyu.marketservicearbitration.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.shyu.marketcommon.result.Result;

/**
 * 健康检查控制器
 * @author shyu
 * @since 2026-04-01
 */
@Api(tags = "健康检查")
@Slf4j
@RestController
@RequestMapping("/health")
public class HealthController {

    @ApiOperation("健康检查")
    @GetMapping("/check")
    public Result<String> healthCheck() {
        log.info("仲裁服务健康检查");
        return Result.success("仲裁服务运行正常");
    }

    @ApiOperation("服务信息")
    @GetMapping("/info")
    public Result<String> serviceInfo() {
        return Result.success("Market Arbitration Service v1.0.0");
    }

}