package com.example.chain_g.dto.response;

import java.math.BigDecimal;
import java.util.List;

public class ScannedForSaleResponse {
    private Integer totalQuantity;
    private BigDecimal totalAmount;
    private List<ScannedItemForSaleResponse> requestList;

    public Integer getTotalQuantity() { return totalQuantity; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public List<ScannedItemForSaleResponse> getRequestList() { return requestList; }
}
