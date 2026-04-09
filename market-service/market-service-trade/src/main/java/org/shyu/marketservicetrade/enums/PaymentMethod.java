package org.shyu.marketservicetrade.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 支付方式枚举
 */
@Getter
@AllArgsConstructor
public enum PaymentMethod {

    /**
     * 支付宝
     */
    ALIPAY(1, "支付宝"),

    /**
     * 微信支付
     */
    WECHAT(2, "微信支付"),

    /**
     * 余额支付
     */
    BALANCE(3, "余额支付"),

    /**
     * 银行卡
     */
    BANK_CARD(4, "银行卡");

    private final Integer code;
    private final String desc;

    /**
     * 根据code获取描述
     */
    public static String getDescByCode(Integer code) {
        if (code == null) {
            return "";
        }
        for (PaymentMethod method : PaymentMethod.values()) {
            if (method.getCode().equals(code)) {
                return method.getDesc();
            }
        }
        return "";
    }

    /**
     * 根据code获取枚举
     */
    public static PaymentMethod getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (PaymentMethod method : PaymentMethod.values()) {
            if (method.getCode().equals(code)) {
                return method;
            }
        }
        return null;
    }
}