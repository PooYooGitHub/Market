package org.shyu.marketapiarbitration.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 仲裁状态枚举
 *
 * @author Market Team
 * @since 2026-04-01
 */
@Getter
@AllArgsConstructor
public enum ArbitrationStatus {

    PENDING(0, "待处理", "#E6A23C"),
    PROCESSING(1, "处理中", "#409EFF"),
    COMPLETED(2, "已完结", "#67C23A"),
    REJECTED(3, "已驳回", "#F56C6C");

    /**
     * 状态码
     */
    private final Integer code;

    /**
     * 状态名称
     */
    private final String name;

    /**
     * 显示颜色
     */
    private final String color;

    /**
     * 根据状态码获取状态
     *
     * @param code 状态码
     * @return 仲裁状态
     */
    public static ArbitrationStatus getByCode(Integer code) {
        if (code == null) {
            return PENDING;
        }

        for (ArbitrationStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }

        return PENDING;
    }

    /**
     * 判断状态是否可以进行处理操作
     */
    public boolean canHandle() {
        return this == PENDING;
    }

    /**
     * 判断状态是否已结束
     */
    public boolean isFinished() {
        return this == COMPLETED || this == REJECTED;
    }
}