package org.shyu.marketservicearbitration.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.shyu.marketservicearbitration.entity.ArbitrationEvidenceSubmissionEntity;

import java.util.List;

public interface IArbitrationEvidenceSubmissionService extends IService<ArbitrationEvidenceSubmissionEntity> {

    List<ArbitrationEvidenceSubmissionEntity> listByArbitrationId(Long arbitrationId);

    List<ArbitrationEvidenceSubmissionEntity> listByRequestId(Long requestId);
}
