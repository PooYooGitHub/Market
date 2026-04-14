package org.shyu.marketservicearbitration.vo;

import lombok.Data;

import java.util.Map;

@Data
public class ArbitrationSystemContextVO {

    private Map<String, Object> orderSnapshot;

    private Map<String, Object> productSnapshot;

    private Map<String, Object> chatContext;
}
