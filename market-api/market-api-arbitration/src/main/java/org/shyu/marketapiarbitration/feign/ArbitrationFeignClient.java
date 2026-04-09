package org.shyu.marketapiarbitration.feign;

import org.shyu.marketapiarbitration.vo.ArbitrationVO;
import org.shyu.marketcommon.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 仲裁服务Feign客户端
 *
 * @author Market Team
 * @since 2026-04-01
 */
@FeignClient(name = "market-service-arbitration", path = "/feign/arbitration")
public interface ArbitrationFeignClient {

    /**
     * 根据订单ID查询仲裁状态
     *
     * @param orderId 订单ID
     * @return 仲裁信息（如果存在）
     */
    @GetMapping("/order/{orderId}/status")
    Result<ArbitrationVO> getArbitrationByOrderId(@PathVariable("orderId") Long orderId);

    /**
     * 检查订单是否可以申请仲裁
     *
     * @param orderId 订单ID
     * @param userId  用户ID
     * @return 是否可以申请
     */
    @GetMapping("/order/{orderId}/canApply")
    Result<Boolean> canApplyArbitration(@PathVariable("orderId") Long orderId,
                                       @RequestParam("userId") Long userId);

    /**
     * 批量查询用户的仲裁统计信息
     *
     * @param userIds 用户ID列表
     * @return 仲裁统计信息
     */
    @PostMapping("/users/stats")
    Result<List<ArbitrationStatsVO>> getArbitrationStats(@RequestBody List<Long> userIds);

    /**
     * 仲裁统计信息VO
     */
    class ArbitrationStatsVO {
        private Long userId;
        private Long totalCount;        // 总申请数
        private Long pendingCount;      // 待处理数
        private Long completedCount;    // 已完结数
        private Long rejectedCount;     // 被驳回数
        private Double successRate;     // 成功率

        // getters and setters
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }

        public Long getTotalCount() { return totalCount; }
        public void setTotalCount(Long totalCount) { this.totalCount = totalCount; }

        public Long getPendingCount() { return pendingCount; }
        public void setPendingCount(Long pendingCount) { this.pendingCount = pendingCount; }

        public Long getCompletedCount() { return completedCount; }
        public void setCompletedCount(Long completedCount) { this.completedCount = completedCount; }

        public Long getRejectedCount() { return rejectedCount; }
        public void setRejectedCount(Long rejectedCount) { this.rejectedCount = rejectedCount; }

        public Double getSuccessRate() { return successRate; }
        public void setSuccessRate(Double successRate) { this.successRate = successRate; }
    }
}