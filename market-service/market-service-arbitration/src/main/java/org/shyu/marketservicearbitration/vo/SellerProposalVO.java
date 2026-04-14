package org.shyu.marketservicearbitration.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SellerProposalVO {

    private String proposalType;

    private BigDecimal proposalAmount;

    private String proposalDescription;

    private String freightBearer;
}

