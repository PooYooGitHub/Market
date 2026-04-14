package org.shyu.marketservicearbitration.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ArbitrationEvidenceBundleVO {

    /**
     * BUYER / SELLER / SYSTEM
     */
    private String role;

    private List<ArbitrationEvidenceSubmissionVO> submissions = new ArrayList<>();
}
