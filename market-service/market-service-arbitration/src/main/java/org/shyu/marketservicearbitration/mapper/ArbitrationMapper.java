package org.shyu.marketservicearbitration.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.shyu.marketservicearbitration.entity.ArbitrationEntity;
import org.shyu.marketservicearbitration.vo.ArbitrationStatsVO;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 仲裁申请 Mapper 接口
 * @author shyu
 * @since 2026-04-01
 */
@Mapper
public interface ArbitrationMapper extends BaseMapper<ArbitrationEntity> {

    /**
     * 查询用户的仲裁申请分页列表
     */
    IPage<ArbitrationEntity> selectUserArbitrationPage(Page<?> page, @Param("userId") Long userId, @Param("status") Integer status);

    /**
     * 查询管理员的仲裁处理分页列表
     */
    IPage<ArbitrationEntity> selectAdminArbitrationPage(Page<?> page, @Param("handlerId") Long handlerId, @Param("status") Integer status);

    /**
     * 查询仲裁统计数据
     */
    List<ArbitrationStatsVO> selectArbitrationStats(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /**
     * 查询用户仲裁统计
     */
    ArbitrationStatsVO selectUserArbitrationStats(@Param("userId") Long userId);

    /**
     * 查询订单是否已有仲裁申请
     */
    ArbitrationEntity selectByOrderAndApplicant(@Param("orderId") Long orderId, @Param("applicantId") Long applicantId);

}