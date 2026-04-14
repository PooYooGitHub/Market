package org.shyu.marketservicearbitration.enums;

import lombok.Getter;

@Getter
public enum DisputeActorRoleEnum {
    BUYER("BUYER"),
    SELLER("SELLER"),
    SYSTEM("SYSTEM"),
    ADMIN("ADMIN");

    private final String code;

    DisputeActorRoleEnum(String code) {
        this.code = code;
    }
}

