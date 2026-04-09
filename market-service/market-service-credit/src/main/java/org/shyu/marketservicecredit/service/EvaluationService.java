package org.shyu.marketservicecredit.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import org.shyu.marketapicredit.dto.CreateEvaluationDTO;
import org.shyu.marketservicecredit.entity.Evaluation;
import org.shyu.marketapicredit.vo.EvaluationVO;

/**
 * 评价服务接口
 *
 * @author Market Team
 * @since 2026-04-01
 */
public interface EvaluationService extends IService<Evaluation> {

    /**
     * 创建评价
     *
     * @param dto        评价信息
     * @param evaluatorId 评价人ID
     * @return 是否成功
     */
    boolean createEvaluation(CreateEvaluationDTO dto, Long evaluatorId);

    /**
     * 分页查询用户收到的评价
     *
     * @param userId   用户ID
     * @param pageNum  页码
     * @param pageSize 页大小
     * @return 评价分页列表
     */
    IPage<EvaluationVO> getReceivedEvaluations(Long userId, Integer pageNum, Integer pageSize);

    /**
     * 分页查询用户给出的评价
     *
     * @param userId   用户ID
     * @param pageNum  页码
     * @param pageSize 页大小
     * @return 评价分页列表
     */
    IPage<EvaluationVO> getGivenEvaluations(Long userId, Integer pageNum, Integer pageSize);

    /**
     * 检查订单评价状态
     *
     * @param orderId     订单ID
     * @param evaluatorId 评价人ID
     * @return 评价状态信息
     */
    boolean checkEvaluationStatus(Long orderId, Long evaluatorId);

    /**
     * 计算评价对信用分的影响
     *
     * @param score 评价分数
     * @return 信用分变化
     */
    Integer calculateCreditChange(Integer score);
}