package org.shyu.marketservicearbitration.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.shyu.marketcommon.result.Result;
import org.shyu.marketservicearbitration.dto.ArbitrationCompleteDTO;
import org.shyu.marketservicearbitration.dto.ArbitrationRejectDTO;
import org.shyu.marketservicearbitration.dto.SupplementRequestDTO;
import org.shyu.marketservicearbitration.service.IArbitrationService;
import org.shyu.marketservicearbitration.vo.AdminArbitrationDetailVO;
import org.shyu.marketservicearbitration.vo.AdminArbitrationListItemVO;
import org.shyu.marketservicearbitration.vo.ArbitrationStatsVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Api(tags = "仲裁管理")
@Slf4j
@RestController
@RequestMapping("/arbitration/admin")
@Validated
public class ArbitrationAdminController {

    @Autowired
    private IArbitrationService arbitrationService;

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

    @ApiOperation("服务状态测试")
    @GetMapping("/ping")
    public Result<?> ping() {
        return Result.success("仲裁服务运行正常");
    }

    @ApiOperation("管理员仲裁列表查询")
    @GetMapping("/list")
    public Result<?> getAdminArbitrationList(
            @ApiParam("当前页") @RequestParam(defaultValue = "1") Integer current,
            @ApiParam("每页大小") @RequestParam(defaultValue = "10") Integer size,
            @ApiParam("状态筛选") @RequestParam(required = false) Integer status,
            @ApiParam("优先级筛选") @RequestParam(required = false) String priority,
            @ApiParam("关键词") @RequestParam(required = false) String keyword) {

        try {
            IPage<AdminArbitrationListItemVO> page = arbitrationService.getAdminArbitrationList(
                    current, size, status, keyword, priority);
            return Result.success(page);
        } catch (Exception e) {
            log.error("获取管理员仲裁列表失败", e);
            return Result.error(e.getMessage());
        }
    }

    @ApiOperation("管理员仲裁聚合详情")
    @GetMapping("/detail/{id}")
    public Result<?> getAdminArbitrationDetail(@PathVariable("id") @NotNull Long id) {
        try {
            AdminArbitrationDetailVO detail = arbitrationService.getAdminArbitrationDetail(id);
            return Result.success(detail);
        } catch (Exception e) {
            log.error("获取仲裁聚合详情失败", e);
            return Result.error(e.getMessage());
        }
    }

    @ApiOperation("管理员受理仲裁申请")
    @PostMapping("/accept/{id}")
    @SaCheckLogin
    public Result<?> acceptArbitration(@ApiParam("仲裁ID") @PathVariable @NotNull Long id) {
        Long handlerId = StpUtil.getLoginIdAsLong();

        try {
            Boolean result = arbitrationService.acceptAdminArbitration(id, handlerId);
            return result ? Result.success("受理成功", null) : Result.error("受理失败");
        } catch (Exception e) {
            log.error("受理仲裁申请失败", e);
            return Result.error(e.getMessage());
        }
    }

    @ApiOperation("管理员完结仲裁申请")
    @PostMapping("/complete")
    @SaCheckLogin
    public Result<?> completeArbitration(@Valid @RequestBody ArbitrationCompleteDTO dto) {
        Long handlerId = StpUtil.getLoginIdAsLong();
        try {
            Boolean success = arbitrationService.completeAdminArbitration(dto, handlerId);
            return success ? Result.success("处理成功", null) : Result.error("处理失败");
        } catch (Exception e) {
            log.error("处理仲裁申请失败", e);
            return Result.error(e.getMessage());
        }
    }

    @ApiOperation("管理员驳回仲裁申请")
    @PostMapping("/reject")
    @SaCheckLogin
    public Result<?> rejectArbitration(@Valid @RequestBody ArbitrationRejectDTO dto) {
        Long handlerId = StpUtil.getLoginIdAsLong();
        try {
            Boolean result = arbitrationService.rejectAdminArbitration(dto, handlerId);
            return result ? Result.success("驳回成功", null) : Result.error("驳回失败");
        } catch (Exception e) {
            log.error("驳回仲裁申请失败", e);
            return Result.error(e.getMessage());
        }
    }

    @Deprecated
    @ApiOperation("管理员处理仲裁申请（兼容旧接口）")
    @PostMapping("/handle")
    @SaCheckLogin
    public Result<?> handleArbitration(@Valid @RequestBody ArbitrationCompleteDTO dto) {
        return completeArbitration(dto);
    }

    @Deprecated
    @ApiOperation("管理员驳回仲裁申请（兼容旧接口）")
    @PostMapping("/reject/{id}")
    @SaCheckLogin
    public Result<?> rejectArbitrationCompat(
            @ApiParam("仲裁ID") @PathVariable @NotNull Long id,
            @ApiParam("驳回原因") @RequestParam @NotBlank String reason) {
        ArbitrationRejectDTO dto = new ArbitrationRejectDTO();
        dto.setArbitrationId(id);
        dto.setRejectReason(reason);
        return rejectArbitration(dto);
    }

    @ApiOperation("发起补证请求")
    @PostMapping("/supplement/request")
    @SaCheckLogin
    public Result<?> requestSupplement(@Valid @RequestBody SupplementRequestDTO requestDTO) {
        Long handlerId = StpUtil.getLoginIdAsLong();
        try {
            Boolean success = arbitrationService.requestSupplement(requestDTO, handlerId);
            return success ? Result.success("补证请求已发起", null) : Result.error("发起补证失败");
        } catch (Exception e) {
            log.error("发起补证失败", e);
            return Result.error(e.getMessage());
        }
    }

    @ApiOperation("补证请求超时结转")
    @PostMapping("/supplement/expire/{requestId}")
    @SaCheckLogin
    public Result<?> expireSupplement(@PathVariable("requestId") @NotNull Long requestId) {
        Long operatorId = StpUtil.getLoginIdAsLong();
        try {
            Boolean success = arbitrationService.expireSupplementRequest(requestId, operatorId);
            return success ? Result.success("补证请求已结转", null) : Result.error("结转失败");
        } catch (Exception e) {
            log.error("补证请求结转失败", e);
            return Result.error(e.getMessage());
        }
    }
}
