package org.shyu.marketapiproduct.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 商品状态枚举
 */
@Getter
@AllArgsConstructor
public enum ProductStatus {

    /**
     * 草稿（暂未使用）
     */
    DRAFT(0, "草稿"),

    /**
     * 已发布（正常显示）
     */
    PUBLISHED(1, "已发布"),

    /**
     * 已售出
     */
    SOLD(2, "已售出"),

    /**
     * 已下架（删除）
     */
    REMOVED(3, "已下架");

    private final Integer code;
    private final String desc;

    /**
     * 根据code获取枚举
     */
    public static ProductStatus getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (ProductStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }

    /**
     * 判断是否可售
     */
    public static boolean isAvailable(Integer code) {
        return PUBLISHED.getCode().equals(code);
    }

    /**
     * 判断是否已售出
     */
    public static boolean isSold(Integer code) {
        return SOLD.getCode().equals(code);
    }
}

