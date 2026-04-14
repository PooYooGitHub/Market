package org.shyu.marketservicearbitration.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.shyu.marketservicearbitration.entity.ArbitrationSupplementRequestEntity;
import org.shyu.marketservicearbitration.mapper.ArbitrationSupplementRequestMapper;
import org.shyu.marketservicearbitration.service.IArbitrationSupplementRequestService;

import java.util.List;

@Service
public class ArbitrationSupplementRequestServiceImpl
        extends ServiceImpl<ArbitrationSupplementRequestMapper, ArbitrationSupplementRequestEntity>
        implements IArbitrationSupplementRequestService {

    @Override
    public List<ArbitrationSupplementRequestEntity> listByArbitrationId(Long arbitrationId) {
        QueryWrapper<ArbitrationSupplementRequestEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("arbitration_id", arbitrationId).orderByDesc("create_time");
        return this.list(wrapper);
    }

    @Override
    public List<ArbitrationSupplementRequestEntity> listPendingByArbitrationId(Long arbitrationId) {
        QueryWrapper<ArbitrationSupplementRequestEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("arbitration_id", arbitrationId)
                .eq("status", 0)
                .orderByAsc("due_time");
        return this.list(wrapper);
    }
}
