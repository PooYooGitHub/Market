package org.shyu.marketservicearbitration.enums;

import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Locale;

/**
 * 仲裁执行状态：描述执行是否完成。
 */
public enum ArbitrationExecutionStatusEnum {
    PENDING("PENDING", "待执行"),
    PROCESSING("PROCESSING", "执行中"),
    SUCCESS("SUCCESS", "执行完成"),
    FAILED("FAILED", "执行失败"),
    MANUAL_REQUIRED("MANUAL_REQUIRED", "需人工介入"),
    NOT_REQUIRED("NOT_REQUIRED", "无需执行");

    private final String code;
    private final String label;

    ArbitrationExecutionStatusEnum(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public static ArbitrationExecutionStatusEnum fromCode(String code) {
        if (!StringUtils.hasText(code)) {
            return PENDING;
        }
        String normalized = code.trim().toUpperCase(Locale.ROOT);
        return Arrays.stream(values())
                .filter(x -> x.code.equals(normalized))
                .findFirst()
                .orElse(PENDING);
    }

    public static String labelOf(String code) {
        return fromCode(code).label;
    }
}

