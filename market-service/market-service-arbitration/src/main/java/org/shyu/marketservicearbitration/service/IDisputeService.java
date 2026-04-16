package org.shyu.marketservicearbitration.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import org.shyu.marketservicearbitration.dto.BuyerConfirmProposalDTO;
import org.shyu.marketservicearbitration.dto.DisputeCreateDTO;
import org.shyu.marketservicearbitration.dto.DisputeEscalateDTO;
import org.shyu.marketservicearbitration.dto.SellerDisputeResponseDTO;
import org.shyu.marketservicearbitration.entity.DisputeRequestEntity;
import org.shyu.marketservicearbitration.vo.DisputeChatSummaryVO;
import org.shyu.marketservicearbitration.vo.DisputeDetailVO;
import org.shyu.marketservicearbitration.vo.DisputeListItemVO;

import java.time.LocalDateTime;
import java.util.List;

public interface IDisputeService extends IService<DisputeRequestEntity> {

    Long createDispute(DisputeCreateDTO dto, Long buyerId);

    IPage<DisputeListItemVO> getBuyerDisputeList(Long buyerId, Integer current, Integer size);

    DisputeListItemVO getMyDisputeByOrderId(Long buyerId, Long orderId);

    IPage<DisputeListItemVO> getSellerPendingDisputes(Long sellerId, Integer current, Integer size);

    IPage<DisputeListItemVO> getSellerAllDisputes(Long sellerId, Integer current, Integer size, List<String> statuses);

    DisputeDetailVO getDisputeDetail(Long disputeId, Long currentUserId);

    Boolean sellerRespond(SellerDisputeResponseDTO dto, Long sellerId);

    Boolean buyerConfirmProposal(BuyerConfirmProposalDTO dto, Long buyerId);

    Long escalateToArbitration(DisputeEscalateDTO dto, Long buyerId);

    Integer checkAndMarkTimeout();

    String buildNegotiationSummary(Long disputeId);

    List<DisputeChatSummaryVO> buildChatSummaryByOrder(Long orderId, Long buyerId, Long sellerId,
                                                        LocalDateTime startTime, LocalDateTime endTime);
}
