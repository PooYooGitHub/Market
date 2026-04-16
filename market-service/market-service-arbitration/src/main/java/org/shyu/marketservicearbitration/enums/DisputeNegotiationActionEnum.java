package org.shyu.marketservicearbitration.enums;

import lombok.Getter;

@Getter
public enum DisputeNegotiationActionEnum {
    BUYER_CREATE("BUYER_CREATE", "买家发起争议"),
    SELLER_AGREE("SELLER_AGREE", "卖家同意买家诉求"),
    SELLER_REJECT("SELLER_REJECT", "卖家拒绝买家诉求"),
    SELLER_PROPOSE("SELLER_PROPOSE", "卖家提出替代方案"),
    BUYER_ACCEPT_PROPOSAL("BUYER_ACCEPT_PROPOSAL", "买家接受卖家方案"),
    BUYER_REJECT_PROPOSAL("BUYER_REJECT_PROPOSAL", "买家拒绝卖家方案"),
    SELLER_TIMEOUT("SELLER_TIMEOUT", "卖家超时未响应"),
    BUYER_CONFIRM_TIMEOUT("BUYER_CONFIRM_TIMEOUT", "买家确认超时"),
    SELLER_LATE_RESPONSE("SELLER_LATE_RESPONSE", "卖家超时后补响应"),
    ESCALATE_TO_ARBITRATION("ESCALATE_TO_ARBITRATION", "升级仲裁"),
    SYSTEM_AUTO_EXECUTE("SYSTEM_AUTO_EXECUTE", "系统记录协商执行语义");

    private final String code;
    private final String label;

    DisputeNegotiationActionEnum(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public static String labelOf(String code) {
        for (DisputeNegotiationActionEnum value : values()) {
            if (value.code.equals(code)) {
                return value.label;
            }
        }
        return code;
    }
}
