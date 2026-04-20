package org.shyu.marketservicecredit.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CreditEventType {

    INIT("INIT", "Initialize credit score"),
    TRADE_COMPLETED("TRADE_COMPLETED", "Valid trade completed"),
    EVALUATION("EVALUATION", "Evaluation score impact"),
    MANUAL_ADJUST("MANUAL_ADJUST", "Manual score adjustment");

    private final String code;
    private final String description;
}
