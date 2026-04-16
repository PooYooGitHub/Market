package org.shyu.marketservicearbitration.enums;

import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Locale;

/**
 * 仲裁执行动作：描述系统接下来做什么。
 */
public enum ArbitrationExecutionTypeEnum {
    NO_ACTION("NO_ACTION", "无需执行动作"),
    REFUND_FULL("REFUND_FULL", "执行全额退款"),
    REFUND_PARTIAL("REFUND_PARTIAL", "执行部分退款"),
    RETURN_FLOW("RETURN_FLOW", "执行退货流程"),
    REPLACE_FLOW("REPLACE_FLOW", "执行换货/补发流程"),
    CLOSE_DISPUTE("CLOSE_DISPUTE", "关闭争议"),
    WAIT_MANUAL_OFFLINE("WAIT_MANUAL_OFFLINE", "等待线下/人工处理");

    private final String code;
    private final String label;

    ArbitrationExecutionTypeEnum(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public static ArbitrationExecutionTypeEnum fromCode(String code) {
        if (!StringUtils.hasText(code)) {
            return NO_ACTION;
        }
        String normalized = code.trim().toUpperCase(Locale.ROOT);
        return Arrays.stream(values())
                .filter(x -> x.code.equals(normalized))
                .findFirst()
                .orElse(NO_ACTION);
    }

    public static String labelOf(String code) {
        return fromCode(code).label;
    }
}

