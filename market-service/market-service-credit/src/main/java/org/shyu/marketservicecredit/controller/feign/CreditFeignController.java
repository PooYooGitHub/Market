package org.shyu.marketservicecredit.controller.feign;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.shyu.marketapicredit.dto.CreateEvaluationDTO;
import org.shyu.marketapicredit.vo.CreditVO;
import org.shyu.marketcommon.result.Result;
import org.shyu.marketservicecredit.service.CreditScoreService;
import org.shyu.marketservicecredit.service.EvaluationService;

/**
 * 信用服务Feign内部调用控制器
 *
 * @author Market Team
 * @since 2026-04-01
 */
@RestController
@RequestMapping("/feign/credit")
@RequiredArgsConstructor
public class CreditFeignController {

    private final CreditScoreService creditScoreService;
    private final EvaluationService evaluationService;

    /**
     * 获取用户信用信息
     */
    @GetMapping("/{userId}")
    public Result<CreditVO> getUserCredit(@PathVariable Long userId) {
        CreditVO creditVO = creditScoreService.getUserCredit(userId);
        return Result.success(creditVO);
    }

    /**
     * 初始化用户信用分
     */
    @PostMapping("/init/{userId}")
    public Result<Void> initUserCredit(@PathVariable Long userId) {
        creditScoreService.initUserCredit(userId);
        return Result.success();
    }

    /**
     * 更新用户信用分
     */
    @PostMapping("/update")
    public Result<Void> updateUserCredit(@RequestParam Long userId,
                                        @RequestParam Integer scoreChange) {
        creditScoreService.updateUserCredit(userId, scoreChange);
        return Result.success();
    }

    /**
     * 创建评价（内部调用）
     */
    @PostMapping("/evaluation/internal")
    public Result<Void> createEvaluationInternal(@RequestBody CreateEvaluationDTO dto,
                                                @RequestParam Long evaluatorId) {
        boolean success = evaluationService.createEvaluation(dto, evaluatorId);
        if (success) {
            return Result.success();
        } else {
            return Result.error("评价创建失败");
        }
    }
}