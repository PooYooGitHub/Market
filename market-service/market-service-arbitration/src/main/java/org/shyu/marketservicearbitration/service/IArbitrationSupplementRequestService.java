package org.shyu.marketservicearbitration.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.shyu.marketservicearbitration.entity.ArbitrationSupplementRequestEntity;

import java.util.List;

public interface IArbitrationSupplementRequestService extends IService<ArbitrationSupplementRequestEntity> {

    List<ArbitrationSupplementRequestEntity> listByArbitrationId(Long arbitrationId);

    List<ArbitrationSupplementRequestEntity> listPendingByArbitrationId(Long arbitrationId);
}
