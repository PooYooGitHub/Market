package org.shyu.marketservicearbitration.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.shyu.marketservicearbitration.entity.ArbitrationLogEntity;

import java.util.List;

/**
 * 仲裁日志服务接口
 * @author shyu
 * @since 2026-04-01
 */
public interface IArbitrationLogService extends IService<ArbitrationLogEntity> {

    /**
     * 根据仲裁ID获取操作日志
     * @param arbitrationId 仲裁ID
     * @return 日志列表
     */
    List<ArbitrationLogEntity> getLogsByArbitrationId(Long arbitrationId);

    /**
     * 记录操作日志
     * @param arbitrationId 仲裁ID
     * @param operatorId 操作人ID
     * @param action 操作动作
     * @param remark 备注
     */
    void recordLog(Long arbitrationId, Long operatorId, String action, String remark);
}