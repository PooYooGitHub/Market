package org.shyu.marketapiarbitration.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 仲裁原因枚举
 *
 * @author Market Team
 * @since 2026-04-01
 */
@Getter
@AllArgsConstructor
public enum ArbitrationReason {

    QUALITY_ISSUE("QUALITY_ISSUE", "商品质量问题"),
    DESCRIPTION_MISMATCH("DESCRIPTION_MISMATCH", "商品描述不符"),
    SHIPPING_DELAY("SHIPPING_DELAY", "发货延迟"),
    SHIPPING_DAMAGE("SHIPPING_DAMAGE", "运输损坏"),
    FAKE_PRODUCT("FAKE_PRODUCT", "假冒伪劣商品"),
    NO_RESPONSE("NO_RESPONSE", "对方无响应"),
    PRICE_DISPUTE("PRICE_DISPUTE", "价格争议"),
    RETURN_REFUND("RETURN_REFUND", "退货退款问题"),
    OTHER("OTHER", "其他原因");

    /**
     * 原因代码
     */
    private final String code;

    /**
     * 原因描述
     */
    private final String description;

    /**
     * 根据代码获取仲裁原因
     *
     * @param code 原因代码
     * @return 仲裁原因
     */
    public static ArbitrationReason getByCode(String code) {
        if (code == null) {
            return OTHER;
        }

        for (ArbitrationReason reason : values()) {
            if (reason.code.equals(code)) {
                return reason;
            }
        }

        return OTHER;
    }

    /**
     * 是否为严重问题（影响信用分更多）
     */
    public boolean isSeriousIssue() {
        return this == FAKE_PRODUCT || this == QUALITY_ISSUE || this == DESCRIPTION_MISMATCH;
    }
}