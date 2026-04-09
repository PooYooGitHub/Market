package org.shyu.marketservicecredit.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.shyu.marketcommon.result.Result;
import org.shyu.marketapicredit.dto.CreateEvaluationDTO;
import org.shyu.marketservicecredit.service.EvaluationService;
import org.shyu.marketapicredit.vo.EvaluationVO;

/**
 * 评价控制器
 *
 * @author Market Team
 * @since 2026-04-01
 */
@RestController
@RequestMapping("/evaluation")
@RequiredArgsConstructor
public class EvaluationController {

    private final EvaluationService evaluationService;

    /**
     * 创建评价
     */
    @PostMapping
    public Result<Void> createEvaluation(@RequestBody CreateEvaluationDTO dto) {
        // 获取当前登录用户ID
        Long evaluatorId = StpUtil.getLoginIdAsLong();

        boolean success = evaluationService.createEvaluation(dto, evaluatorId);
        if (success) {
            return Result.success();
        } else {
            return Result.error("评价创建失败");
        }
    }

    /**
     * 获取用户收到的评价
     */
    @GetMapping("/received")
    public Result<IPage<EvaluationVO>> getReceivedEvaluations(
            @RequestParam(required = false) Long userId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {

        // 如果没有指定用户ID，则查询当前登录用户的评价
        if (userId == null) {
            userId = StpUtil.getLoginIdAsLong();
        }

        IPage<EvaluationVO> result = evaluationService.getReceivedEvaluations(userId, pageNum, pageSize);
        return Result.success(result);
    }

    /**
     * 获取用户给出的评价
     */
    @GetMapping("/given")
    public Result<IPage<EvaluationVO>> getGivenEvaluations(
            @RequestParam(required = false) Long userId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {

        // 如果没有指定用户ID，则查询当前登录用户的评价
        if (userId == null) {
            userId = StpUtil.getLoginIdAsLong();
        }

        IPage<EvaluationVO> result = evaluationService.getGivenEvaluations(userId, pageNum, pageSize);
        return Result.success(result);
    }

    /**
     * 检查订单评价状态
     */
    @GetMapping("/status")
    public Result<Boolean> checkEvaluationStatus(@RequestParam Long orderId) {
        Long evaluatorId = StpUtil.getLoginIdAsLong();
        boolean hasEvaluated = evaluationService.checkEvaluationStatus(orderId, evaluatorId);
        return Result.success(hasEvaluated);
    }
}