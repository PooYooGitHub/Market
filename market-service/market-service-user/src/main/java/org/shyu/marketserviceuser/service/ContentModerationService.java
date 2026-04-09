package org.shyu.marketserviceuser.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.shyu.marketserviceuser.dto.ModerationActionRequest;
import org.shyu.marketserviceuser.dto.ModerationQueryRequest;
import org.shyu.marketserviceuser.dto.ModerationRuleRequest;
import org.shyu.marketserviceuser.vo.ModerationStatisticsVO;
import org.shyu.marketserviceuser.vo.ModerationTaskVO;

import java.util.List;
import java.util.Map;

/**
 * 内容审核服务接口
 */
public interface ContentModerationService {

    /**
     * 获取待审核内容列表
     */
    Page<ModerationTaskVO> getPendingTasks(ModerationQueryRequest request);

    /**
     * 获取审核任务详情
     */
    ModerationTaskVO getModerationTask(Long taskId);

    /**
     * 执行审核操作
     */
    void moderateContent(ModerationActionRequest request);

    /**
     * 批量审核
     */
    void batchModerateContent(List<ModerationActionRequest> requests);

    /**
     * 获取审核统计数据
     */
    ModerationStatisticsVO getModerationStatistics();

    /**
     * 获取审核规则列表
     */
    List<Map<String, Object>> getModerationRules();

    /**
     * 创建审核规则
     */
    void createModerationRule(ModerationRuleRequest request);

    /**
     * 更新审核规则
     */
    void updateModerationRule(Long ruleId, ModerationRuleRequest request);

    /**
     * 删除审核规则
     */
    void deleteModerationRule(Long ruleId);

    /**
     * 获取敏感词列表
     */
    List<String> getSensitiveWords();

    /**
     * 添加敏感词
     */
    void addSensitiveWords(List<String> words);

    /**
     * 删除敏感词
     */
    void removeSensitiveWords(List<String> words);

    /**
     * 检查内容是否包含敏感词
     */
    Map<String, Object> checkSensitiveWords(String content);

    /**
     * 获取审核日志
     */
    Page<Map<String, Object>> getModerationLogs(Integer pageNum, Integer pageSize, String startDate, String endDate);
}