package org.shyu.marketservicecredit.controller.feign;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.shyu.marketapicredit.dto.CreateEvaluationDTO;
import org.shyu.marketapicredit.vo.CreditVO;
import org.shyu.marketcommon.result.Result;
import org.shyu.marketservicecredit.service.CreditScoreService;
import org.shyu.marketservicecredit.service.EvaluationService;

/**
 * Internal Feign controller for credit service.
 */
@RestController
@RequestMapping("/feign/credit")
@RequiredArgsConstructor
public class CreditFeignController {

    private final CreditScoreService creditScoreService;
    private final EvaluationService evaluationService;

    @GetMapping("/{userId}")
    public Result<CreditVO> getUserCredit(@PathVariable Long userId) {
        return Result.success(creditScoreService.getUserCredit(userId));
    }

    @PostMapping("/init/{userId}")
    public Result<Void> initUserCredit(@PathVariable Long userId) {
        creditScoreService.initUserCredit(userId);
        return Result.success();
    }

    @PostMapping("/update")
    public Result<Void> updateUserCredit(@RequestParam Long userId,
                                         @RequestParam Integer scoreChange) {
        creditScoreService.updateUserCredit(userId, scoreChange);
        return Result.success();
    }

    @PostMapping("/transaction/complete")
    public Result<Void> onTradeCompleted(@RequestParam Long orderId,
                                         @RequestParam Long buyerId,
                                         @RequestParam Long sellerId) {
        creditScoreService.handleTradeCompleted(orderId, buyerId, sellerId);
        return Result.success();
    }

    @PostMapping("/evaluation/internal")
    public Result<Void> createEvaluationInternal(@RequestBody CreateEvaluationDTO dto,
                                                 @RequestParam Long evaluatorId) {
        boolean success = evaluationService.createEvaluation(dto, evaluatorId);
        return success ? Result.success() : Result.error("create evaluation failed");
    }
}
