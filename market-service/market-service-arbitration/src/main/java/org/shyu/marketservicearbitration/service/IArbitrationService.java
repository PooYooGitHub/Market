package org.shyu.marketservicearbitration.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import org.shyu.marketservicearbitration.entity.ArbitrationEntity;
import org.shyu.marketservicearbitration.vo.ArbitrationStatsVO;
import org.shyu.marketservicearbitration.vo.ArbitrationVO;

import java.util.List;

/**
 * 仲裁服务接口
 * @author shyu
 * @since 2026-04-01
 */
public interface IArbitrationService extends IService<ArbitrationEntity> {

    /**
     * 提交仲裁申请
     * @param arbitrationVO 仲裁申请信息
     * @return 申请结果
     */
    ArbitrationEntity submitArbitration(ArbitrationVO arbitrationVO);

    /**
     * 修改仲裁申请（仅申请人本人可修改）
     * @param id 仲裁ID
     * @param applicantId 申请人ID
     * @param arbitrationVO 仲裁修改信息
     * @return 修改后的仲裁
     */
    ArbitrationEntity updateArbitration(Long id, Long applicantId, ArbitrationVO arbitrationVO);

    /**
     * 分页查询仲裁列表
     * @param current 当前页
     * @param size 每页大小
     * @param status 状态筛选
     * @param applicantId 申请人ID筛选
     * @param respondentId 被申诉人ID筛选
     * @return 分页结果
     */
    IPage<ArbitrationEntity> getArbitrationPage(Integer current, Integer size,
                                              Integer status, Long applicantId, Long respondentId);

    /**
     * 管理端分页查询仲裁列表
     * @param current 当前页
     * @param size 每页大小
     * @param status 状态筛选
     * @param applicantId 申请人ID筛选
     * @param respondentId 被申诉人ID筛选
     * @param keyword 关键词筛选
     * @param priority 优先级筛选
     * @return 分页结果
     */
    IPage<ArbitrationEntity> getArbitrationPage(Integer current, Integer size,
                                              Integer status, Long applicantId, Long respondentId,
                                              String keyword, String priority);

    /**
     * 根据ID获取仲裁详情
     * @param id 仲裁ID
     * @return 仲裁详情
     */
    ArbitrationEntity getArbitrationDetail(Long id);

    /**
     * 管理员受理仲裁申请
     * @param id 仲裁ID
     * @param handlerId 处理人ID
     * @return 处理结果
     */
    Boolean acceptArbitration(Long id, Long handlerId);

    /**
     * 管理员处理仲裁
     * @param id 仲裁ID
     * @param result 裁决结果
     * @param handlerId 处理人ID
     * @return 处理结果
     */
    Boolean handleArbitration(Long id, String result, Long handlerId);

    /**
     * 管理员驳回仲裁申请
     * @param id 仲裁ID
     * @param reason 驳回原因
     * @param handlerId 处理人ID
     * @return 处理结果
     */
    Boolean rejectArbitration(Long id, String reason, Long handlerId);

    /**
     * 获取仲裁统计数据
     * @return 统计数据
     */
    ArbitrationStatsVO getArbitrationStats();

    /**
     * 获取用户的仲裁申请列表
     * @param userId 用户ID
     * @param current 当前页
     * @param size 每页大小
     * @return 用户仲裁列表
     */
    IPage<ArbitrationEntity> getUserArbitrationList(Long userId, Integer current, Integer size);

    /**
     * 根据订单ID查询用户的仲裁申请
     * @param userId 用户ID
     * @param orderId 订单ID
     * @return 仲裁申请，不存在则返回null
     */
    ArbitrationEntity getUserArbitrationByOrderId(Long userId, Long orderId);

    /**
     * 检查订单是否已有仲裁申请
     * @param orderId 订单ID
     * @param applicantId 申请人ID
     * @return 是否存在
     */
    Boolean checkArbitrationExists(Long orderId, Long applicantId);
}
