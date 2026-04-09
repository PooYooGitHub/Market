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
import org.shyu.marketservicearbitration.entity.ArbitrationLogEntity;
import org.shyu.marketservicearbitration.service.IArbitrationLogService;
import org.shyu.marketservicearbitration.service.IArbitrationService;
import org.shyu.marketservicearbitration.vo.ArbitrationStatsVO;
import org.shyu.marketservicearbitration.vo.ArbitrationVO;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 仲裁服务控制器
 * @author shyu
 * @since 2026-04-01
 */
@Api(tags = "仲裁服务")
@Slf4j
@RestController
@RequestMapping("/arbitration")
@Validated
public class ArbitrationController {

    @Autowired
    private IArbitrationService arbitrationService;

    @Autowired
    private IArbitrationLogService arbitrationLogService;

    /**
     * 提交仲裁申请
     */
    @ApiOperation("提交仲裁申请")
    @PostMapping("/submit")
    @SaCheckLogin
    public Result<?> submitArbitration(@Valid @RequestBody ArbitrationVO arbitrationVO) {
        // 从token中获取当前用户ID
        Long userId = StpUtil.getLoginIdAsLong();
        arbitrationVO.setApplicantId(userId);

        log.info("用户{}提交仲裁申请: {}", userId, arbitrationVO);

        try {
            ArbitrationEntity arbitration = arbitrationService.submitArbitration(arbitrationVO);
            return Result.success("仲裁申请提交成功", arbitration);
        } catch (Exception e) {
            log.error("提交仲裁申请失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 修改仲裁申请
     */
    @ApiOperation("修改仲裁申请")
    @PutMapping("/update/{id}")
    @SaCheckLogin
    public Result<?> updateArbitration(
            @ApiParam("仲裁ID") @PathVariable @NotNull Long id,
            @Valid @RequestBody ArbitrationVO arbitrationVO) {
        Long userId = StpUtil.getLoginIdAsLong();
        arbitrationVO.setApplicantId(userId);

        try {
            ArbitrationEntity arbitration = arbitrationService.updateArbitration(id, userId, arbitrationVO);
            return Result.success("仲裁申请修改成功", arbitration);
        } catch (Exception e) {
            log.error("修改仲裁申请失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 分页查询仲裁列表
     */
    @ApiOperation("分页查询仲裁列表")
    @GetMapping("/page")
    public Result<?> getArbitrationPage(
            @ApiParam("当前页") @RequestParam(defaultValue = "1") Integer current,
            @ApiParam("每页大小") @RequestParam(defaultValue = "10") Integer size,
            @ApiParam("状态筛选") @RequestParam(required = false) Integer status,
            @ApiParam("申请人ID") @RequestParam(required = false) Long applicantId,
            @ApiParam("被申诉人ID") @RequestParam(required = false) Long respondentId) {

        try {
            IPage<ArbitrationEntity> page = arbitrationService.getArbitrationPage(
                    current, size, status, applicantId, respondentId);
            return Result.success(page);
        } catch (Exception e) {
            log.error("查询仲裁列表失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取仲裁详情
     */
    @ApiOperation("获取仲裁详情")
    @GetMapping("/detail/{id}")
    public Result<?> getArbitrationDetail(
            @ApiParam("仲裁ID") @PathVariable @NotNull Long id) {

        try {
            ArbitrationEntity arbitration = arbitrationService.getArbitrationDetail(id);
            return Result.success(arbitration);
        } catch (Exception e) {
            log.error("获取仲裁详情失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取仲裁操作日志
     */
    @ApiOperation("获取仲裁操作日志")
    @GetMapping("/logs/{arbitrationId}")
    public Result<?> getArbitrationLogs(
            @ApiParam("仲裁ID") @PathVariable @NotNull Long arbitrationId) {

        try {
            List<ArbitrationLogEntity> logs = arbitrationLogService.getLogsByArbitrationId(arbitrationId);
            return Result.success(logs);
        } catch (Exception e) {
            log.error("获取仲裁日志失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取当前用户的仲裁申请列表
     */
    @ApiOperation("获取当前用户的仲裁申请列表")
    @GetMapping("/my")
    @SaCheckLogin
    public Result<?> getMyArbitrationList(
            @ApiParam("当前页") @RequestParam(defaultValue = "1") Integer current,
            @ApiParam("每页大小") @RequestParam(defaultValue = "10") Integer size) {

        // 从token中获取当前用户ID
        Long userId = StpUtil.getLoginIdAsLong();

        try {
            IPage<ArbitrationEntity> page = arbitrationService.getUserArbitrationList(userId, current, size);
            return Result.success(page);
        } catch (Exception e) {
            log.error("获取用户仲裁列表失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 根据订单ID获取当前用户的仲裁申请
     */
    @ApiOperation("根据订单ID获取当前用户仲裁申请")
    @GetMapping("/my/order/{orderId}")
    @SaCheckLogin
    public Result<?> getMyArbitrationByOrderId(
            @ApiParam("订单ID") @PathVariable @NotNull Long orderId) {
        Long userId = StpUtil.getLoginIdAsLong();
        try {
            ArbitrationEntity arbitration = arbitrationService.getUserArbitrationByOrderId(userId, orderId);
            return Result.success(arbitration);
        } catch (Exception e) {
            log.error("查询订单仲裁申请失败", e);
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
}
