package org.shyu.marketapitrade.dto;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
@Data
public class OrderDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private String orderNo;
    private Long buyerId;
    private Long sellerId;
    private Long productId;
    private BigDecimal amount;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}