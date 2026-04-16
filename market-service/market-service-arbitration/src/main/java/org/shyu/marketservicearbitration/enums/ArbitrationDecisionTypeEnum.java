package org.shyu.marketservicearbitration.enums;

import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Locale;

/**
 * 仲裁裁决类型：描述管理员判了什么。
 */
public enum ArbitrationDecisionTypeEnum {
    SUPPORT_FULL_REFUND("SUPPORT_FULL_REFUND", "支持全额退款"),
    SUPPORT_PARTIAL_REFUND("SUPPORT_PARTIAL_REFUND", "支持部分退款"),
    SUPPORT_RETURN_AND_REFUND("SUPPORT_RETURN_AND_REFUND", "支持退货退款"),
    SUPPORT_REPLACE("SUPPORT_REPLACE", "支持换货/补发"),
    REJECT_BUYER_REQUEST("REJECT_BUYER_REQUEST", "驳回买家诉求"),
    REQUIRE_SUPPLEMENT("REQUIRE_SUPPLEMENT", "要求补充材料"),
    CLOSE_WITH_NEGOTIATION_RESULT("CLOSE_WITH_NEGOTIATION_RESULT", "按协商结果结案"),
    OTHER("OTHER", "其他裁决");

    private final String code;
    private final String label;

    ArbitrationDecisionTypeEnum(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public static ArbitrationDecisionTypeEnum fromCode(String code) {
        if (!StringUtils.hasText(code)) {
            return OTHER;
        }
        String normalized = code.trim().toUpperCase(Locale.ROOT);
        return Arrays.stream(values())
                .filter(x -> x.code.equals(normalized))
                .findFirst()
                .orElse(OTHER);
    }

    public static String labelOf(String code) {
        return fromCode(code).label;
    }
}

