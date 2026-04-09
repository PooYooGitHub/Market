package org.shyu.marketservicecredit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.shyu.marketservicecredit.entity.Evaluation;
import org.shyu.marketapicredit.vo.EvaluationVO;

import java.util.List;

/**
 * 评价 Mapper 接口
 *
 * @author Market Team
 * @since 2026-04-01
 */
@Mapper
public interface EvaluationMapper extends BaseMapper<Evaluation> {

    /**
     * 分页查询用户收到的评价
     *
     * @param page   分页参数
     * @param userId 用户ID
     * @return 评价列表
     */
    IPage<EvaluationVO> selectReceivedEvaluations(Page<EvaluationVO> page, @Param("userId") Long userId);

    /**
     * 分页查询用户给出的评价
     *
     * @param page   分页参数
     * @param userId 用户ID
     * @return 评价列表
     */
    IPage<EvaluationVO> selectGivenEvaluations(Page<EvaluationVO> page, @Param("userId") Long userId);

    /**
     * 计算用户的平均评分
     *
     * @param userId 用户ID
     * @return 平均评分
     */
    @Select("SELECT AVG(score) FROM t_evaluation WHERE target_id = #{userId}")
    Double getAvgScore(Long userId);

    /**
     * 获取用户的评价总数
     *
     * @param userId 用户ID
     * @return 评价总数
     */
    @Select("SELECT COUNT(*) FROM t_evaluation WHERE target_id = #{userId}")
    Long getTotalCount(Long userId);

    /**
     * 获取用户的好评数量(评分>=4)
     *
     * @param userId 用户ID
     * @return 好评数量
     */
    @Select("SELECT COUNT(*) FROM t_evaluation WHERE target_id = #{userId} AND score >= 4")
    Long getGoodCount(Long userId);

    /**
     * 检查订单是否已经被评价过
     *
     * @param orderId     订单ID
     * @param evaluatorId 评价人ID
     * @return 评价记录数
     */
    @Select("SELECT COUNT(*) FROM t_evaluation WHERE order_id = #{orderId} AND evaluator_id = #{evaluatorId}")
    int checkOrderEvaluated(@Param("orderId") Long orderId, @Param("evaluatorId") Long evaluatorId);
}