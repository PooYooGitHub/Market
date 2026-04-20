package org.shyu.marketservicecredit.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.shyu.marketapicredit.dto.CreateEvaluationDTO;
import org.shyu.marketapicredit.vo.EvaluationVO;
import org.shyu.marketapiuser.dto.UserDTO;
import org.shyu.marketapiuser.feign.UserFeignClient;
import org.shyu.marketapitrade.dto.OrderDTO;
import org.shyu.marketapitrade.feign.TradeFeignClient;
import org.shyu.marketcommon.exception.BusinessException;
import org.shyu.marketcommon.result.Result;
import org.shyu.marketservicecredit.constant.CreditPolicy;
import org.shyu.marketservicecredit.entity.Evaluation;
import org.shyu.marketservicecredit.enums.CreditEventType;
import org.shyu.marketservicecredit.mapper.EvaluationMapper;
import org.shyu.marketservicecredit.service.CreditScoreService;
import org.shyu.marketservicecredit.service.EvaluationService;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Evaluation service implementation.
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
            throw new BusinessException("评分必须在1-5之间");
        }
        if (dto.getTargetId().equals(evaluatorId)) {
            throw new BusinessException("不能评价自己");
        }

        int count = baseMapper.checkOrderEvaluated(dto.getOrderId(), evaluatorId);
        if (count > 0) {
            throw new BusinessException("该订单已评价");
        }

        validateOrderAndPermission(dto.getOrderId(), evaluatorId, dto.getTargetId());
        validateEvaluationContent(dto.getContent());

        Evaluation evaluation = new Evaluation();
        evaluation.setOrderId(dto.getOrderId());
        evaluation.setEvaluatorId(evaluatorId);
        evaluation.setTargetId(dto.getTargetId());
        evaluation.setScore(dto.getScore());
        evaluation.setComment(dto.getContent());
        evaluation.setCreateTime(LocalDateTime.now());
        save(evaluation);

        Integer scoreChange = calculateCreditChange(dto.getScore());
        String eventKey = "EVALUATION:" + dto.getOrderId() + ":" + evaluatorId + ":" + dto.getTargetId();
        creditScoreService.applyCreditEvent(
                dto.getTargetId(),
                CreditEventType.EVALUATION,
                scoreChange,
                evaluation.getId(),
                "order evaluation",
                eventKey
        );

        log.info("create evaluation success, evaluatorId={}, targetId={}, orderId={}, score={}, scoreChange={}",
                evaluatorId, dto.getTargetId(), dto.getOrderId(), dto.getScore(), scoreChange);
        return true;
    }

    @Override
    public IPage<EvaluationVO> getReceivedEvaluations(Long userId, Integer pageNum, Integer pageSize) {
        if (userId == null) {
            throw new BusinessException("用户ID不能为空");
        }

        int safePageNum = pageNum == null || pageNum < 1 ? 1 : pageNum;
        int safePageSize = pageSize == null || pageSize < 1 ? 10 : pageSize;

        Page<EvaluationVO> page = new Page<>(safePageNum, safePageSize);
        IPage<EvaluationVO> result = baseMapper.selectReceivedEvaluations(page, userId);
        fillUserInfo(result.getRecords(), true);
        return result;
    }

    @Override
    public IPage<EvaluationVO> getGivenEvaluations(Long userId, Integer pageNum, Integer pageSize) {
        if (userId == null) {
            throw new BusinessException("用户ID不能为空");
        }

        int safePageNum = pageNum == null || pageNum < 1 ? 1 : pageNum;
        int safePageSize = pageSize == null || pageSize < 1 ? 10 : pageSize;

        Page<EvaluationVO> page = new Page<>(safePageNum, safePageSize);
        IPage<EvaluationVO> result = baseMapper.selectGivenEvaluations(page, userId);
        fillUserInfo(result.getRecords(), false);
        return result;
    }

    @Override
    public boolean checkEvaluationStatus(Long orderId, Long evaluatorId) {
        if (orderId == null || evaluatorId == null) {
            return false;
        }
        return baseMapper.checkOrderEvaluated(orderId, evaluatorId) > 0;
    }

    @Override
    public Integer calculateCreditChange(Integer score) {
        return CreditPolicy.mapEvaluationScoreChange(score);
    }

    private void validateOrderAndPermission(Long orderId, Long evaluatorId, Long targetId) {
        try {
            Result<OrderDTO> orderResult = tradeFeignClient.getOrderById(orderId);
            if (orderResult == null || orderResult.getData() == null) {
                throw new BusinessException("订单不存在");
            }

            OrderDTO order = orderResult.getData();
            if (order.getStatus() == null || order.getStatus() != 3) {
                throw new BusinessException("订单未完成，暂时无法评价");
            }

            boolean isOrderParticipant = evaluatorId.equals(order.getBuyerId()) || evaluatorId.equals(order.getSellerId());
            if (!isOrderParticipant) {
                throw new BusinessException("无评价权限");
            }

            boolean isValidTarget = (evaluatorId.equals(order.getBuyerId()) && targetId.equals(order.getSellerId()))
                    || (evaluatorId.equals(order.getSellerId()) && targetId.equals(order.getBuyerId()));
            if (!isValidTarget) {
                throw new BusinessException("被评价人不匹配订单关系");
            }
        } catch (Exception e) {
            log.error("validate order and permission failed, orderId={}, evaluatorId={}, targetId={}",
                    orderId, evaluatorId, targetId, e);
            if (e instanceof BusinessException) {
                throw e;
            }
            throw new BusinessException("订单验证失败，请稍后重试");
        }
    }

    private void validateEvaluationContent(String content) {
        if (content != null && content.length() > 500) {
            throw new BusinessException("评价内容不能超过500字符");
        }
    }

    private void fillUserInfo(List<EvaluationVO> evaluations, boolean isReceived) {
        if (CollectionUtils.isEmpty(evaluations)) {
            return;
        }

        List<Long> userIds = evaluations.stream()
                .map(item -> isReceived ? item.getEvaluatorId() : item.getTargetId())
                .distinct()
                .collect(Collectors.toList());

        if (CollectionUtils.isEmpty(userIds)) {
            return;
        }

        Map<Long, UserDTO> userMap = new HashMap<Long, UserDTO>();
        for (Long userId : userIds) {
            try {
                Result<UserDTO> userResult = userFeignClient.getUserById(userId);
                if (userResult != null && userResult.getData() != null) {
                    userMap.put(userId, userResult.getData());
                }
            } catch (Exception e) {
                log.warn("query user info failed, userId={}", userId, e);
            }
        }

        for (EvaluationVO evaluation : evaluations) {
            Long userId = isReceived ? evaluation.getEvaluatorId() : evaluation.getTargetId();
            UserDTO user = userMap.get(userId);
            if (user == null) {
                continue;
            }

            if (isReceived) {
                evaluation.setEvaluatorName(user.getUsername());
                evaluation.setEvaluatorAvatar(user.getAvatar());
            } else {
                evaluation.setTargetName(user.getUsername());
                evaluation.setTargetAvatar(user.getAvatar());
            }
        }
    }
}
