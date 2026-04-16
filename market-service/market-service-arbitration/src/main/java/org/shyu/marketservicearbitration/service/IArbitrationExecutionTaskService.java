package org.shyu.marketservicearbitration.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.shyu.marketservicearbitration.entity.ArbitrationExecutionTaskEntity;

import java.util.List;

public interface IArbitrationExecutionTaskService extends IService<ArbitrationExecutionTaskEntity> {

    List<ArbitrationExecutionTaskEntity> listByArbitrationId(Long arbitrationId);
}

