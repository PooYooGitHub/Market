package org.shyu.marketapicredit.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import org.shyu.marketapicredit.dto.CreateEvaluationDTO;
import org.shyu.marketapicredit.vo.CreditVO;
import org.shyu.marketcommon.result.Result;

/**
 * Credit service Feign client.
 */
@FeignClient(value = "market-service-credit", path = "/feign/credit")
public interface CreditFeignClient {

    @GetMapping("/{userId}")
    Result<CreditVO> getUserCredit(@PathVariable("userId") Long userId);

    @PostMapping("/init/{userId}")
    Result<Void> initUserCredit(@PathVariable("userId") Long userId);

    @PostMapping("/update")
    Result<Void> updateUserCredit(@RequestParam("userId") Long userId,
                                  @RequestParam("scoreChange") Integer scoreChange);

    @PostMapping("/transaction/complete")
    Result<Void> onTradeCompleted(@RequestParam("orderId") Long orderId,
                                  @RequestParam("buyerId") Long buyerId,
                                  @RequestParam("sellerId") Long sellerId);

    @PostMapping("/evaluation/internal")
    Result<Void> createEvaluationInternal(@RequestBody CreateEvaluationDTO dto,
                                          @RequestParam("evaluatorId") Long evaluatorId);
}
