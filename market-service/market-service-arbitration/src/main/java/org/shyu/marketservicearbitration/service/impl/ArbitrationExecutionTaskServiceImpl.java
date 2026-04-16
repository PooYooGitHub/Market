package org.shyu.marketservicearbitration.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.shyu.marketservicearbitration.entity.ArbitrationExecutionTaskEntity;
import org.shyu.marketservicearbitration.mapper.ArbitrationExecutionTaskMapper;
import org.shyu.marketservicearbitration.service.IArbitrationExecutionTaskService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ArbitrationExecutionTaskServiceImpl
        extends ServiceImpl<ArbitrationExecutionTaskMapper, ArbitrationExecutionTaskEntity>
        implements IArbitrationExecutionTaskService {

    @Override
    public List<ArbitrationExecutionTaskEntity> listByArbitrationId(Long arbitrationId) {
        QueryWrapper<ArbitrationExecutionTaskEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("arbitration_id", arbitrationId).orderByAsc("create_time");
        return list(wrapper);
    }
}

