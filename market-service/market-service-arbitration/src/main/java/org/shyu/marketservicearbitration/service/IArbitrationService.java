package org.shyu.marketservicearbitration.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import org.shyu.marketservicearbitration.dto.ArbitrationCompleteDTO;
import org.shyu.marketservicearbitration.dto.ArbitrationRejectDTO;
import org.shyu.marketservicearbitration.dto.SupplementRequestDTO;
import org.shyu.marketservicearbitration.dto.SupplementSubmitDTO;
import org.shyu.marketservicearbitration.entity.ArbitrationEntity;
import org.shyu.marketservicearbitration.vo.AdminArbitrationDetailVO;
import org.shyu.marketservicearbitration.vo.AdminArbitrationListItemVO;
import org.shyu.marketservicearbitration.vo.ArbitrationStatsVO;
import org.shyu.marketservicearbitration.vo.ArbitrationVO;

public interface IArbitrationService extends IService<ArbitrationEntity> {

    ArbitrationEntity submitArbitration(ArbitrationVO arbitrationVO);

    ArbitrationEntity updateArbitration(Long id, Long applicantId, ArbitrationVO arbitrationVO);

    IPage<ArbitrationEntity> getArbitrationPage(Integer current, Integer size,
                                                Integer status, Long applicantId, Long respondentId);

    IPage<ArbitrationEntity> getArbitrationPage(Integer current, Integer size,
                                                Integer status, Long applicantId, Long respondentId,
                                                String keyword, String priority);

    ArbitrationEntity getArbitrationDetail(Long id);

    IPage<AdminArbitrationListItemVO> getAdminArbitrationList(Integer current, Integer size,
                                                              Integer status, String keyword, String priority);

    Boolean acceptAdminArbitration(Long id, Long handlerId);

    Boolean completeAdminArbitration(ArbitrationCompleteDTO dto, Long handlerId);

    Boolean rejectAdminArbitration(ArbitrationRejectDTO dto, Long handlerId);

    Boolean acceptArbitration(Long id, Long handlerId);

    Boolean handleArbitration(Long id, String result, Long handlerId);

    Boolean handleArbitration(Long id, String result, Long handlerId, Boolean force);

    Boolean rejectArbitration(Long id, String reason, Long handlerId);

    Boolean cancelArbitration(Long id, Long applicantId);

    ArbitrationStatsVO getArbitrationStats();

    ArbitrationStatsVO getUserArbitrationStats(Long userId);

    IPage<ArbitrationEntity> getUserArbitrationList(Long userId, Integer current, Integer size);

    ArbitrationEntity getUserArbitrationByOrderId(Long userId, Long orderId);

    Boolean checkArbitrationExists(Long orderId, Long applicantId);

    AdminArbitrationDetailVO getAdminArbitrationDetail(Long arbitrationId);

    Boolean requestSupplement(SupplementRequestDTO requestDTO, Long handlerId);

    Boolean submitSupplement(SupplementSubmitDTO submitDTO, Long submitterId);

    Boolean expireSupplementRequest(Long requestId, Long operatorId);
}
