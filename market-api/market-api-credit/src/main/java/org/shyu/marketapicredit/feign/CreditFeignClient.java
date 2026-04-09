package org.shyu.marketapicredit.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import org.shyu.marketapicredit.dto.CreateEvaluationDTO;
import org.shyu.marketapicredit.vo.CreditVO;
import org.shyu.marketcommon.result.Result;

/**
 * 信用服务Feign客户端
 *
 * @author Market Team
 * @since 2026-04-01
 */
@FeignClient(value = "market-service-credit", path = "/feign/credit")
public interface CreditFeignClient {

    /**
     * 获取用户信用信息
     */
    @GetMapping("/{userId}")
    Result<CreditVO> getUserCredit(@PathVariable("userId") Long userId);

    /**
     * 初始化用户信用分
     */
    @PostMapping("/init/{userId}")
    Result<Void> initUserCredit(@PathVariable("userId") Long userId);

    /**
     * 更新用户信用分
     */
    @PostMapping("/update")
    Result<Void> updateUserCredit(@RequestParam("userId") Long userId,
                                 @RequestParam("scoreChange") Integer scoreChange);

    /**
     * 创建评价（内部调用）
     */
    @PostMapping("/evaluation/internal")
    Result<Void> createEvaluationInternal(@RequestBody CreateEvaluationDTO dto,
                                         @RequestParam("evaluatorId") Long evaluatorId);
}