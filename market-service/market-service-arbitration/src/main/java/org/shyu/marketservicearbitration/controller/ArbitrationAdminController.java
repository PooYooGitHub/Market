package org.shyu.marketservicearbitration.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.shyu.marketcommon.result.Result;
import org.shyu.marketservicearbitration.entity.ArbitrationEntity;
import org.shyu.marketservicearbitration.service.IArbitrationService;
import org.shyu.marketservicearbitration.vo.ArbitrationStatsVO;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Map;

/**
 * 仲裁管理员控制器
 * @author shyu
 * @since 2026-04-01
 */
@Api(tags = "仲裁管理")
@Slf4j
@RestController
@RequestMapping("/arbitration/admin")
@Validated
public class ArbitrationAdminController {

    @Autowired
    private IArbitrationService arbitrationService;

    /**
     * 获取仲裁统计数据
     */
    @ApiOperation("获取仲裁统计数据")
    @GetMapping("/stats")
    public Result<?> getArbitrationStats() {
        try {
            ArbitrationStatsVO stats = arbitrationService.getArbitrationStats();
            return Result.success(stats);
        } catch (Exception e) {
            log.error("获取仲裁统计数据失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 服务状态测试接口（无需登录）
     */
    @ApiOperation("服务状态测试")
    @GetMapping("/ping")
    public Result<?> ping() {
        return Result.success("仲裁服务运行正常");
    }

    /**
     * 管理员仲裁列表查询
     */
    @ApiOperation("管理员仲裁列表查询")
    @GetMapping("/list")
    public Result<?> getAdminArbitrationList(
            @ApiParam("当前页") @RequestParam(defaultValue = "1") Integer current,
            @ApiParam("每页大小") @RequestParam(defaultValue = "10") Integer size,
            @ApiParam("状态筛选") @RequestParam(required = false) Integer status,
            @ApiParam("优先级筛选") @RequestParam(required = false) String priority,
            @ApiParam("关键词") @RequestParam(required = false) String keyword) {

        try {
            // 管理员可以查看所有仲裁申请
            IPage<ArbitrationEntity> page = arbitrationService.getArbitrationPage(
                    current, size, status, null, null, keyword, priority);
            return Result.success(page);
        } catch (Exception e) {
            log.error("获取管理员仲裁列表失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 管理员受理仲裁申请
     */
    @ApiOperation("管理员受理仲裁申请")
    @PostMapping("/accept/{id}")
    @SaCheckLogin
    public Result<?> acceptArbitration(
            @ApiParam("仲裁ID") @PathVariable @NotNull Long id) {

        // 获取当前管理员ID（简化实现，实际应该检查管理员权限）
        Long handlerId = StpUtil.getLoginIdAsLong();

        try {
            Boolean result = arbitrationService.acceptArbitration(id, handlerId);
            return result ? Result.success("受理成功") : Result.error("受理失败");
        } catch (Exception e) {
            log.error("受理仲裁申请失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 管理员处理仲裁申请
     */
    @ApiOperation("管理员处理仲裁申请")
    @PostMapping("/handle")
    @SaCheckLogin
    public Result<?> handleArbitration(@RequestBody Map<String, Object> data) {
        Long id = Long.valueOf(data.get("id").toString());
        String result = data.get("result").toString();

        // 获取当前管理员ID
        Long handlerId = StpUtil.getLoginIdAsLong();

        try {
            Boolean success = arbitrationService.handleArbitration(id, result, handlerId);
            return success ? Result.success("处理成功") : Result.error("处理失败");
        } catch (Exception e) {
            log.error("处理仲裁申请失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 管理员驳回仲裁申请
     */
    @ApiOperation("管理员驳回仲裁申请")
    @PostMapping("/reject/{id}")
    @SaCheckLogin
    public Result<?> rejectArbitration(
            @ApiParam("仲裁ID") @PathVariable @NotNull Long id,
            @ApiParam("驳回原因") @RequestParam @NotBlank String reason) {

        // 获取当前管理员ID
        Long handlerId = StpUtil.getLoginIdAsLong();

        try {
            Boolean result = arbitrationService.rejectArbitration(id, reason, handlerId);
            return result ? Result.success("驳回成功") : Result.error("驳回失败");
        } catch (Exception e) {
            log.error("驳回仲裁申请失败", e);
            return Result.error(e.getMessage());
        }
    }
}
