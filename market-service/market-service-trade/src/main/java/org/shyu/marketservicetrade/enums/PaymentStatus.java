package org.shyu.marketservicetrade.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 支付状态枚举
 */
@Getter
@AllArgsConstructor
public enum PaymentStatus {

    /**
     * 待支付
     */
    WAIT_PAY(0, "待支付"),

    /**
     * 支付成功
     */
    SUCCESS(1, "支付成功"),

    /**
     * 支付失败
     */
    FAILED(2, "支付失败"),

    /**
     * 已退款
     */
    REFUNDED(3, "已退款");

    private final Integer code;
    private final String desc;

    /**
     * 根据code获取描述
     */
    public static String getDescByCode(Integer code) {
        if (code == null) {
            return "";
        }
        for (PaymentStatus status : PaymentStatus.values()) {
            if (status.getCode().equals(code)) {
                return status.getDesc();
            }
        }
        return "";
    }

    /**
     * 根据code获取枚举
     */
    public static PaymentStatus getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (PaymentStatus status : PaymentStatus.values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }
}