package org.shyu.marketservicecredit.controller;

import cn.dev33.satoken.stp.StpUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.shyu.marketcommon.result.Result;
import org.shyu.marketservicecredit.service.CreditScoreService;
import org.shyu.marketapicredit.vo.CreditVO;

/**
 * 信用分控制器
 *
 * @author Market Team
 * @since 2026-04-01
 */
@RestController
@RequestMapping("/credit")
@RequiredArgsConstructor
public class CreditScoreController {

    private final CreditScoreService creditScoreService;

    /**
     * 获取当前用户信用信息
     */
    @GetMapping("/my")
    public Result<CreditVO> getMyCredit() {
        Long userId = StpUtil.getLoginIdAsLong();
        CreditVO creditVO = creditScoreService.getUserCredit(userId);
        return Result.success(creditVO);
    }

    /**
     * 获取指定用户信用信息
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
     * 重新计算用户信用分
     */
    @PostMapping("/recalculate/{userId}")
    public Result<Void> recalculateUserCredit(@PathVariable Long userId) {
        creditScoreService.recalculateUserCredit(userId);
        return Result.success();
    }
}