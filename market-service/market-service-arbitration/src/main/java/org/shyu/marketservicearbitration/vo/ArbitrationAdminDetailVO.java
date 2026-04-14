package org.shyu.marketservicearbitration.vo;

import lombok.Data;
import org.shyu.marketservicearbitration.entity.ArbitrationEntity;
import org.shyu.marketservicearbitration.entity.ArbitrationLogEntity;

import java.util.ArrayList;
import java.util.List;

@Data
public class ArbitrationAdminDetailVO {

    private ArbitrationEntity arbitration;

    private ArbitrationSystemContextVO systemContext;

    private List<ArbitrationEvidenceBundleVO> evidenceBundles = new ArrayList<>();

    private List<ArbitrationSupplementRequestVO> supplementRequests = new ArrayList<>();

    private List<ArbitrationLogEntity> logs = new ArrayList<>();
}
