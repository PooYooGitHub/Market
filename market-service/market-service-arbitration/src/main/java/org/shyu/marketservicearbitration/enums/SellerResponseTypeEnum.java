package org.shyu.marketservicearbitration.enums;

import lombok.Getter;

@Getter
public enum SellerResponseTypeEnum {
    AGREE("AGREE", "同意买家诉求"),
    REJECT("REJECT", "拒绝买家诉求"),
    PROPOSE("PROPOSE", "提出替代方案");

    private final String code;
    private final String label;

    SellerResponseTypeEnum(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public static boolean contains(String code) {
        for (SellerResponseTypeEnum value : values()) {
            if (value.code.equals(code)) {
                return true;
            }
        }
        return false;
    }
}

