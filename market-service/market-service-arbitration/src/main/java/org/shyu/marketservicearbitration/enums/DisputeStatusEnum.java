package org.shyu.marketservicearbitration.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Getter
public enum DisputeStatusEnum {
    INIT("INIT", "已创建"),
    WAIT_SELLER_RESPONSE("WAIT_SELLER_RESPONSE", "待卖家响应"),
    SELLER_PROPOSED("SELLER_PROPOSED", "卖家已提替代方案"),
    WAIT_BUYER_CONFIRM("WAIT_BUYER_CONFIRM", "待买家确认"),
    NEGOTIATION_SUCCESS("NEGOTIATION_SUCCESS", "协商成功"),
    NEGOTIATION_FAILED("NEGOTIATION_FAILED", "协商失败"),
    SELLER_TIMEOUT("SELLER_TIMEOUT", "卖家超时未响应"),
    ESCALATED_TO_ARBITRATION("ESCALATED_TO_ARBITRATION", "已升级仲裁"),
    CLOSED("CLOSED", "已关闭");

    private static final Set<String> SELLER_RESPONDABLE_STATUSES = new HashSet<>(Arrays.asList(
            WAIT_SELLER_RESPONSE.code,
            SELLER_TIMEOUT.code
    ));

    private static final Set<String> ESCALATABLE_STATUSES = new HashSet<>(Arrays.asList(
            NEGOTIATION_FAILED.code,
            SELLER_TIMEOUT.code,
            WAIT_BUYER_CONFIRM.code
    ));

    private final String code;
    private final String label;

    DisputeStatusEnum(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public static boolean canSellerRespond(String status) {
        return SELLER_RESPONDABLE_STATUSES.contains(status);
    }

    public static boolean canEscalate(String status) {
        return ESCALATABLE_STATUSES.contains(status);
    }

    public static String getLabel(String code) {
        for (DisputeStatusEnum value : values()) {
            if (value.code.equals(code)) {
                return value.label;
            }
        }
        return code;
    }
}
