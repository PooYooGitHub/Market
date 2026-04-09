package org.shyu.marketservicecredit.controller;

import cn.dev33.satoken.stp.StpUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.shyu.marketcommon.result.Result;
import org.shyu.marketservicecredit.service.CreditScoreService;
import org.shyu.marketapicredit.vo.CreditVO;

/**
 * 信用统计控制器
 *
 * @author Market Team
 * @since 2026-04-01
 */
@Slf4j
@RestController
@RequestMapping("/credit")
@RequiredArgsConstructor
public class CreditStatsController {

    private final CreditScoreService creditScoreService;

    /**
     * 获取用户信用统计信息
     */
    @GetMapping("/stats")
    public Result<CreditVO> getCreditStats(@RequestParam(required = false) Long userId) {
        // 如果没有指定用户ID，则查询当前登录用户
        if (userId == null) {
            userId = StpUtil.getLoginIdAsLong();
        }

        CreditVO creditVO = creditScoreService.getUserCredit(userId);
        return Result.success(creditVO);
    }

    /**
     * 获取信用分排行榜（前10名）
     */
    @GetMapping("/rankings")
    public Result<Object> getCreditRankings() {
        // TODO: 实现信用分排行榜功能
        return Result.success("排行榜功能待实现");
    }

    /**
     * 重新计算用户信用分
     */
    @PostMapping("/recalculate")
    public Result<Void> recalculateCredit() {
        Long userId = StpUtil.getLoginIdAsLong();
        creditScoreService.recalculateUserCredit(userId);
        return Result.success();
    }
}