package org.shyu.marketservicearbitration.service;

import org.shyu.marketservicearbitration.dto.ArbitrationCompleteDTO;
import org.shyu.marketservicearbitration.dto.ArbitrationRejectDTO;
import org.shyu.marketservicearbitration.entity.ArbitrationEntity;
import org.shyu.marketservicearbitration.vo.AdminArbitrationDetailVO;
import org.shyu.marketservicearbitration.vo.AdminArbitrationListItemVO;

public interface IArbitrationDecisionWorkflowService {

    Boolean completeDecision(ArbitrationCompleteDTO dto, Long handlerId);

    Boolean rejectDecision(ArbitrationRejectDTO dto, Long handlerId);

    void enrichAdminDetail(AdminArbitrationDetailVO detail, ArbitrationEntity entity);

    void enrichAdminListItem(AdminArbitrationListItemVO item, ArbitrationEntity entity);
}
