package org.shyu.marketservicearbitration.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.shyu.marketcommon.result.Result;
import org.shyu.marketservicearbitration.dto.BuyerConfirmProposalDTO;
import org.shyu.marketservicearbitration.dto.DisputeCreateDTO;
import org.shyu.marketservicearbitration.dto.DisputeEscalateDTO;
import org.shyu.marketservicearbitration.dto.SellerDisputeResponseDTO;
import org.shyu.marketservicearbitration.service.IDisputeService;
import org.shyu.marketservicearbitration.vo.DisputeDetailVO;
import org.shyu.marketservicearbitration.vo.DisputeListItemVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@Api(tags = "争议协商服务")
@Slf4j
@RestController
@RequestMapping("/dispute")
@Validated
public class DisputeController {

    @Autowired
    private IDisputeService disputeService;

    @ApiOperation("买家发起争议申请")
    @PostMapping("/create")
    @SaCheckLogin
    public Result<?> createDispute(@Valid @RequestBody DisputeCreateDTO dto) {
        Long buyerId = StpUtil.getLoginIdAsLong();
        try {
            Long disputeId = disputeService.createDispute(dto, buyerId);
            return Result.success("争议申请创建成功", disputeId);
        } catch (Exception e) {
            log.error("创建争议申请失败", e);
            return Result.error(e.getMessage());
        }
    }

    @ApiOperation("买家我的争议列表")
    @GetMapping("/my")
    @SaCheckLogin
    public Result<?> getMyDisputes(@RequestParam(defaultValue = "1") Integer current,
                                   @RequestParam(defaultValue = "10") Integer size) {
        Long buyerId = StpUtil.getLoginIdAsLong();
        try {
            IPage<DisputeListItemVO> page = disputeService.getBuyerDisputeList(buyerId, current, size);
            return Result.success(page);
        } catch (Exception e) {
            log.error("查询买家争议列表失败", e);
            return Result.error(e.getMessage());
        }
    }

    @ApiOperation("争议详情")
    @GetMapping("/detail/{id}")
    @SaCheckLogin
    public Result<?> getDisputeDetail(@PathVariable("id") Long id) {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        try {
            DisputeDetailVO detailVO = disputeService.getDisputeDetail(id, currentUserId);
            return Result.success(detailVO);
        } catch (Exception e) {
            log.error("查询争议详情失败", e);
            return Result.error(e.getMessage());
        }
    }

    @ApiOperation("升级仲裁")
    @PostMapping("/escalate")
    @SaCheckLogin
    public Result<?> escalateToArbitration(@Valid @RequestBody DisputeEscalateDTO dto) {
        Long buyerId = StpUtil.getLoginIdAsLong();
        try {
            Long arbitrationId = disputeService.escalateToArbitration(dto, buyerId);
            return Result.success("升级仲裁成功", arbitrationId);
        } catch (Exception e) {
            log.error("升级仲裁失败", e);
            return Result.error(e.getMessage());
        }
    }

    @ApiOperation("卖家待响应争议列表")
    @GetMapping("/seller/pending")
    @SaCheckLogin
    public Result<?> getSellerPendingDisputes(@RequestParam(defaultValue = "1") Integer current,
                                              @RequestParam(defaultValue = "10") Integer size) {
        Long sellerId = StpUtil.getLoginIdAsLong();
        try {
            IPage<DisputeListItemVO> page = disputeService.getSellerPendingDisputes(sellerId, current, size);
            return Result.success(page);
        } catch (Exception e) {
            log.error("查询卖家待响应争议失败", e);
            return Result.error(e.getMessage());
        }
    }

    @ApiOperation("卖家响应争议")
    @PostMapping("/seller/respond")
    @SaCheckLogin
    public Result<?> sellerRespond(@Valid @RequestBody SellerDisputeResponseDTO dto) {
        Long sellerId = StpUtil.getLoginIdAsLong();
        try {
            Boolean success = disputeService.sellerRespond(dto, sellerId);
            return success ? Result.success("卖家响应成功", null) : Result.error("卖家响应失败");
        } catch (Exception e) {
            log.error("卖家响应争议失败", e);
            return Result.error(e.getMessage());
        }
    }

    @ApiOperation("买家确认卖家替代方案")
    @PostMapping("/buyer/confirm-proposal")
    @SaCheckLogin
    public Result<?> buyerConfirmProposal(@Valid @RequestBody BuyerConfirmProposalDTO dto) {
        Long buyerId = StpUtil.getLoginIdAsLong();
        try {
            Boolean success = disputeService.buyerConfirmProposal(dto, buyerId);
            return success ? Result.success("买家确认成功", null) : Result.error("买家确认失败");
        } catch (Exception e) {
            log.error("买家确认卖家方案失败", e);
            return Result.error(e.getMessage());
        }
    }

    @ApiOperation("系统检查争议超时")
    @PostMapping("/system/check-timeout")
    @SaCheckLogin
    public Result<?> checkTimeout() {
        try {
            Integer changed = disputeService.checkAndMarkTimeout();
            return Result.success("超时检查完成", changed);
        } catch (Exception e) {
            log.error("争议超时检查失败", e);
            return Result.error(e.getMessage());
        }
    }
}

