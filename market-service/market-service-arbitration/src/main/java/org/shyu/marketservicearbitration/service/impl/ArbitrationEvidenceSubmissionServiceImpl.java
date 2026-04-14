package org.shyu.marketservicearbitration.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.shyu.marketservicearbitration.entity.ArbitrationEvidenceSubmissionEntity;
import org.shyu.marketservicearbitration.mapper.ArbitrationEvidenceSubmissionMapper;
import org.shyu.marketservicearbitration.service.IArbitrationEvidenceSubmissionService;

import java.util.List;

@Service
public class ArbitrationEvidenceSubmissionServiceImpl
        extends ServiceImpl<ArbitrationEvidenceSubmissionMapper, ArbitrationEvidenceSubmissionEntity>
        implements IArbitrationEvidenceSubmissionService {

    @Override
    public List<ArbitrationEvidenceSubmissionEntity> listByArbitrationId(Long arbitrationId) {
        QueryWrapper<ArbitrationEvidenceSubmissionEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("arbitration_id", arbitrationId).orderByAsc("create_time");
        return this.list(wrapper);
    }

    @Override
    public List<ArbitrationEvidenceSubmissionEntity> listByRequestId(Long requestId) {
        QueryWrapper<ArbitrationEvidenceSubmissionEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("supplement_request_id", requestId).orderByAsc("create_time");
        return this.list(wrapper);
    }
}
