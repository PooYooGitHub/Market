package org.shyu.marketservicecredit.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.shyu.marketcommon.exception.BusinessException;
import org.shyu.marketcommon.result.Result;
import org.shyu.marketapicredit.dto.CreateEvaluationDTO;
import org.shyu.marketapiuser.dto.UserDTO;
import org.shyu.marketapiuser.feign.UserFeignClient;
import org.shyu.marketapitrade.dto.OrderDTO;
import org.shyu.marketapitrade.feign.TradeFeignClient;
import org.shyu.marketservicecredit.entity.Evaluation;
import org.shyu.marketservicecredit.mapper.EvaluationMapper;
import org.shyu.marketservicecredit.service.CreditScoreService;
import org.shyu.marketservicecredit.service.EvaluationService;
import org.shyu.marketapicredit.vo.EvaluationVO;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 评价服务实现类
 *
 * @author Market Team
 * @since 2026-04-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EvaluationServiceImpl extends ServiceImpl<EvaluationMapper, Evaluation> implements EvaluationService {

    private final CreditScoreService creditScoreService;
    private final UserFeignClient userFeignClient;
    private final TradeFeignClient tradeFeignClient;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createEvaluation(CreateEvaluationDTO dto, Long evaluatorId) {
        if (dto == null || evaluatorId == null) {
            throw new BusinessException("评价信息不能为空");
        }

        if (dto.getTargetId() == null) {
            throw new BusinessException("被评价用户ID不能为空");
        }

        if (dto.getOrderId() == null) {
            throw new BusinessException("订单ID不能为空");
        }

        if (dto.getScore() == null || dto.getScore() < 1 || dto.getScore() > 5) {
            throw new BusinessException("评价分数必须在1-5之间");
        }

        // 检查是否已经评价过
        int count = baseMapper.checkOrderEvaluated(dto.getOrderId(), evaluatorId);
        if (count > 0) {
            throw new BusinessException("该订单已经评价过了");
        }

        // 不能评价自己
        if (dto.getTargetId().equals(evaluatorId)) {
            throw new BusinessException("不能评价自己");
        }

        // 验证订单信息和评价权限
        validateOrderAndPermission(dto.getOrderId(), evaluatorId, dto.getTargetId());

        // 验证评价内容
        validateEvaluationContent(dto.getContent());

        try {
            // 创建评价记录
            Evaluation evaluation = new Evaluation();
            evaluation.setOrderId(dto.getOrderId());
            evaluation.setEvaluatorId(evaluatorId);
            evaluation.setTargetId(dto.getTargetId());
            evaluation.setScore(dto.getScore());
            evaluation.setComment(dto.getContent());
            evaluation.setCreateTime(LocalDateTime.now());

            save(evaluation);

            // 计算信用分变化并更新
            Integer scoreChange = calculateCreditChange(dto.getScore());
            creditScoreService.updateUserCredit(dto.getTargetId(), scoreChange);

            log.info("创建评价成功，评价人: {}, 被评价人: {}, 订单: {}, 分数: {}, 信用分变化: {}",
                    evaluatorId, dto.getTargetId(), dto.getOrderId(), dto.getScore(), scoreChange);

            return true;
        } catch (Exception e) {
            log.error("创建评价失败", e);
            throw new BusinessException("创建评价失败：" + e.getMessage());
        }
    }

    @Override
    public IPage<EvaluationVO> getReceivedEvaluations(Long userId, Integer pageNum, Integer pageSize) {
        if (userId == null) {
            throw new BusinessException("用户ID不能为空");
        }

        if (pageNum == null || pageNum < 1) {
            pageNum = 1;
        }
        if (pageSize == null || pageSize < 1) {
            pageSize = 10;
        }

        Page<EvaluationVO> page = new Page<>(pageNum, pageSize);
        IPage<EvaluationVO> result = baseMapper.selectReceivedEvaluations(page, userId);

        // 填充用户信息
        fillUserInfo(result.getRecords(), true);

        return result;
    }

    @Override
    public IPage<EvaluationVO> getGivenEvaluations(Long userId, Integer pageNum, Integer pageSize) {
        if (userId == null) {
            throw new BusinessException("用户ID不能为空");
        }

        if (pageNum == null || pageNum < 1) {
            pageNum = 1;
        }
        if (pageSize == null || pageSize < 1) {
            pageSize = 10;
        }

        Page<EvaluationVO> page = new Page<>(pageNum, pageSize);
        IPage<EvaluationVO> result = baseMapper.selectGivenEvaluations(page, userId);

        // 填充用户信息
        fillUserInfo(result.getRecords(), false);

        return result;
    }

    @Override
    public boolean checkEvaluationStatus(Long orderId, Long evaluatorId) {
        if (orderId == null || evaluatorId == null) {
            return false;
        }

        int count = baseMapper.checkOrderEvaluated(orderId, evaluatorId);
        return count > 0;
    }

    @Override
    public Integer calculateCreditChange(Integer score) {
        if (score == null) {
            return 0;
        }

        // 根据评价分数计算信用分变化
        // 调整为更合理的变化幅度：5分: +2, 4分: +1, 3分: 0, 2分: -1, 1分: -2
        switch (score) {
            case 5:
                return 2;
            case 4:
                return 1;
            case 3:
                return 0;
            case 2:
                return -1;
            case 1:
                return -2;
            default:
                return 0;
        }
    }

    /**
     * 验证订单信息和评价权限
     */
    private void validateOrderAndPermission(Long orderId, Long evaluatorId, Long targetId) {
        try {
            // 调用Trade服务获取订单信息
            Result<OrderDTO> orderResult = tradeFeignClient.getOrderById(orderId);
            if (orderResult == null || orderResult.getData() == null) {
                throw new BusinessException("订单不存在");
            }

            OrderDTO order = orderResult.getData();

            // 检查订单状态是否允许评价（已收货/完成状态）
            if (order.getStatus() != 3) { // 3表示已收货/完成状态
                throw new BusinessException("订单未完成，暂时无法评价");
            }

            // 检查评价权限：评价人必须是订单的买家或卖家
            boolean isOrderParticipant = evaluatorId.equals(order.getBuyerId()) ||
                                       evaluatorId.equals(order.getSellerId());

            if (!isOrderParticipant) {
                throw new BusinessException("您无权对此订单进行评价");
            }

            // 检查被评价人是否正确（买家评价卖家，卖家评价买家）
            boolean isValidTarget = (evaluatorId.equals(order.getBuyerId()) && targetId.equals(order.getSellerId())) ||
                                   (evaluatorId.equals(order.getSellerId()) && targetId.equals(order.getBuyerId()));

            if (!isValidTarget) {
                throw new BusinessException("被评价人信息有误");
            }

        } catch (Exception e) {
            log.error("验证订单权限失败，订单ID: {}, 评价人: {}, 被评价人: {}", orderId, evaluatorId, targetId, e);
            if (e instanceof BusinessException) {
                throw e;
            }
            throw new BusinessException("验证订单信息失败，请稍后重试");
        }
    }

    /**
     * 验证评价内容
     */
    private void validateEvaluationContent(String content) {
        if (content != null && content.length() > 500) {
            throw new BusinessException("评价内容不能超过500个字符");
        }

        // 简单的内容过滤（可扩展为更复杂的内容审核）
        if (content != null && (content.contains("垃圾") || content.contains("骗子"))) {
            log.warn("评价内容可能包含不当词汇: {}", content);
        }
    }

    /**
     * 填充用户信息
     * @param evaluations 评价列表
     * @param isReceived 是否为收到的评价（true=收到的评价需要填充评价人信息，false=给出的评价需要填充被评价人信息）
     */
    private void fillUserInfo(List<EvaluationVO> evaluations, boolean isReceived) {
        if (CollectionUtils.isEmpty(evaluations)) {
            return;
        }

        try {
            // 收集需要查询的用户ID
            List<Long> userIds = evaluations.stream()
                    .map(evaluation -> isReceived ? evaluation.getEvaluatorId() : evaluation.getTargetId())
                    .distinct()
                    .collect(Collectors.toList());

            if (CollectionUtils.isEmpty(userIds)) {
                return;
            }

            // 构建用户信息映射
            Map<Long, UserDTO> userMap = new HashMap<>();

            // 逐个查询用户信息（暂时用单个查询，后续可优化为批量查询）
            for (Long userId : userIds) {
                try {
                    Result<UserDTO> userResult = userFeignClient.getUserById(userId);
                    if (userResult != null && userResult.getData() != null) {
                        userMap.put(userId, userResult.getData());
                    }
                } catch (Exception e) {
                    log.warn("查询用户信息失败，userId: {}", userId, e);
                    // 继续处理其他用户，不中断整个流程
                }
            }

            // 填充用户信息
            for (EvaluationVO evaluation : evaluations) {
                Long userId = isReceived ? evaluation.getEvaluatorId() : evaluation.getTargetId();
                UserDTO user = userMap.get(userId);

                if (user != null) {
                    if (isReceived) {
                        evaluation.setEvaluatorName(user.getUsername());
                        evaluation.setEvaluatorAvatar(user.getAvatar());
                    } else {
                        evaluation.setTargetName(user.getUsername());
                        evaluation.setTargetAvatar(user.getAvatar());
                    }
                }
            }

        } catch (Exception e) {
            log.error("获取用户信息失败", e);
            // 不抛异常，允许在没有用户信息的情况下返回评价数据
        }
    }
}