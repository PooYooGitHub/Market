package org.shyu.marketservicecredit.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.shyu.marketapicredit.vo.CreditVO;
import org.shyu.marketservicecredit.entity.CreditScore;
import org.shyu.marketservicecredit.enums.CreditEventType;

public interface CreditScoreService extends IService<CreditScore> {

    CreditVO getUserCredit(Long userId);

    void initUserCredit(Long userId);

    void updateUserCredit(Long userId, Integer scoreChange);

    void recalculateUserCredit(Long userId);

    void applyCreditEvent(Long userId,
                          CreditEventType eventType,
                          Integer rawScoreChange,
                          Long relatedId,
                          String reason,
                          String eventKey);

    void handleTradeCompleted(Long orderId, Long buyerId, Long sellerId);
}
