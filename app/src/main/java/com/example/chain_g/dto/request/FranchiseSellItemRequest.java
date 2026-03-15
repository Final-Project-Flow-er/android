package com.example.chain_g.dto.request;

import java.math.BigDecimal;

public class FranchiseSellItemRequest {
    private Long productId;
    private String productCode;
    private String productName;
    private BigDecimal unitPrice;
    private String serialCode;

    public FranchiseSellItemRequest(Long productId, String productCode, String productName, BigDecimal unitPrice, String serialCode) {
        this.productId = productId;
        this.productCode = productCode;
        this.productName = productName;
        this.unitPrice = unitPrice;
        this.serialCode = serialCode;
    }

    public Long getProductId() { return productId; }
    public String getProductCode() { return productCode; }
    public String getProductName() { return productName; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public String getSerialCode() { return serialCode; }
}
