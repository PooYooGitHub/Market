package org.shyu.marketservicearbitration.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.shyu.marketservicearbitration.entity.ArbitrationLogEntity;
import org.shyu.marketservicearbitration.mapper.ArbitrationLogMapper;
import org.shyu.marketservicearbitration.service.IArbitrationLogService;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 仲裁日志服务实现类
 * @author shyu
 * @since 2026-04-01
 */
@Slf4j
@Service
public class ArbitrationLogServiceImpl extends ServiceImpl<ArbitrationLogMapper, ArbitrationLogEntity>
        implements IArbitrationLogService {

    @Override
    public List<ArbitrationLogEntity> getLogsByArbitrationId(Long arbitrationId) {
        log.info("获取仲裁操作日志，仲裁ID: {}", arbitrationId);

        QueryWrapper<ArbitrationLogEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("arbitration_id", arbitrationId);
        queryWrapper.orderByAsc("create_time");

        return this.list(queryWrapper);
    }

    @Override
    public void recordLog(Long arbitrationId, Long operatorId, String action, String remark) {
        log.info("记录仲裁操作日志: arbitrationId={}, operatorId={}, action={}",
                arbitrationId, operatorId, action);

        ArbitrationLogEntity logEntity = new ArbitrationLogEntity();
        logEntity.setArbitrationId(arbitrationId);
        logEntity.setOperatorId(operatorId);
        logEntity.setAction(action);
        logEntity.setRemark(remark);
        logEntity.setCreateTime(LocalDateTime.now());

        this.save(logEntity);
    }
}