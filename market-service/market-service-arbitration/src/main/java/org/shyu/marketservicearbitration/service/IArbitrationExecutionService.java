package org.shyu.marketservicearbitration.service;

import org.shyu.marketservicearbitration.entity.ArbitrationEntity;
import org.shyu.marketservicearbitration.entity.ArbitrationExecutionTaskEntity;
import org.shyu.marketservicearbitration.entity.DisputeRequestEntity;

public interface IArbitrationExecutionService {

    ArbitrationExecutionTaskEntity createExecutionTask(ArbitrationEntity arbitration, DisputeRequestEntity dispute);

    void executeTask(Long taskId, Long operatorId);

    void markExecutionSuccess(ArbitrationExecutionTaskEntity task, String message, Long operatorId);

    void markExecutionFailed(ArbitrationExecutionTaskEntity task, String message, Long operatorId);

    void handleRefundFull(ArbitrationExecutionTaskEntity task, ArbitrationEntity arbitration, DisputeRequestEntity dispute, Long operatorId);

    void handleRefundPartial(ArbitrationExecutionTaskEntity task, ArbitrationEntity arbitration, DisputeRequestEntity dispute, Long operatorId);

    void handleReturnFlow(ArbitrationExecutionTaskEntity task, ArbitrationEntity arbitration, DisputeRequestEntity dispute, Long operatorId);

    void handleCloseDispute(ArbitrationExecutionTaskEntity task, ArbitrationEntity arbitration, DisputeRequestEntity dispute, Long operatorId);
}

