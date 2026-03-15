package com.example.chain_g.dto.response;

import java.math.BigDecimal;

public class ScannedItemForSaleResponse {
    private Long productId;
    private String productCode;
    private String productName;
    private BigDecimal unitPrice;
    private String serialCode;

    public Long getProductId() { return productId; }
    public String getProductCode() { return productCode; }
    public String getProductName() { return productName; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public String getSerialCode() { return serialCode; }
}
