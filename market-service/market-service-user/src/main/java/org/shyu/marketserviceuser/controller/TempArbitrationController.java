package org.shyu.marketserviceuser.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.shyu.marketcommon.result.Result;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 临时仲裁管理员接口
 * 通过用户服务提供仲裁管理功能，用于前端调试
 */
@Api(tags = "仲裁管理（临时）")
@Slf4j
@RestController
@RequestMapping("/arbitration")
public class TempArbitrationController {

    /**
     * 查询管理员仲裁处理列表
     */
    @ApiOperation("查询管理员仲裁处理列表")
    @GetMapping("/admin/list")
    public Result<?> getAdminArbitrationList(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority) {

        log.info("查询管理员仲裁处理列表，参数：current={}, size={}, status={}, priority={}",
                current, size, status, priority);

        // 模拟分页数据
        Map<String, Object> result = new HashMap<>();

        // 创建假数据列表
        List<Map<String, Object>> records = Arrays.asList(
            createArbitrationRecord("ARB20240001", "电子产品质量争议", "张同学", "李同学", "商品质量", "紧急", 3, "2024-04-02 14:30:25"),
            createArbitrationRecord("ARB20240002", "二手教材易纠纷", "王同学", "陈同学", "交易纠纷", "普通", 1, "2024-04-04 09:15:10"),
            createArbitrationRecord("ARB20240003", "服装尺寸问题投诉", "赵同学", "孙同学", "商品质量", "普通", 2, "2024-04-03 16:20:18")
        );

        result.put("records", records);
        result.put("total", 3L);

        return Result.success(result);
    }

    /**
     * 获取仲裁统计数据
     */
    @ApiOperation("获取仲裁统计数据")
    @GetMapping("/admin/stats")
    public Result<?> getArbitrationStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("pendingCount", 12);      // 待处理案件
        stats.put("urgentCount", 3);        // 紧急案件
        stats.put("todayNewCount", 5);      // 今日新增
        stats.put("avgHandleDays", 2.1);    // 平均等待天数

        return Result.success(stats);
    }

    /**
     * 受理仲裁申请
     */
    @ApiOperation("受理仲裁申请")
    @PostMapping("/admin/accept/{id}")
    public Result<?> acceptArbitration(@PathVariable Long id) {
        log.info("受理仲裁申请，ID：{}", id);
        return Result.success("受理成功");
    }

    /**
     * 处理仲裁申请
     */
    @ApiOperation("处理仲裁申请")
    @PostMapping("/admin/handle")
    public Result<?> handleArbitration(@RequestBody Map<String, Object> data) {
        log.info("处理仲裁申请，数据：{}", data);
        return Result.success("处理成功");
    }

    /**
     * 驳回仲裁申请
     */
    @ApiOperation("驳回仲裁申请")
    @PostMapping("/admin/reject/{id}")
    public Result<?> rejectArbitration(@PathVariable Long id, @RequestParam String reason) {
        log.info("驳回仲裁申请，ID：{}，原因：{}", id, reason);
        return Result.success("驳回成功");
    }

    private Map<String, Object> createArbitrationRecord(String caseNumber, String disputeSubject,
            String applicant, String respondent, String disputeType, String priority, int waitDays, String createTime) {
        Map<String, Object> record = new HashMap<>();
        record.put("caseNumber", caseNumber);
        record.put("disputeSubject", disputeSubject);
        record.put("applicant", applicant);
        record.put("respondent", respondent);
        record.put("disputeType", disputeType);
        record.put("priority", priority);
        record.put("waitDays", waitDays);
        record.put("createTime", createTime);
        record.put("status", 0); // 待处理
        return record;
    }
}