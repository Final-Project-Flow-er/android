package com.example.chain_g.dto.request;

import java.math.BigDecimal;
import java.util.List;

public class FranchiseSellRequest {
    private Integer totalQuantity;
    private BigDecimal totalAmount;
    private List<FranchiseSellItemRequest> requestList;

    public FranchiseSellRequest(Integer totalQuantity, BigDecimal totalAmount, List<FranchiseSellItemRequest> requestList) {
        this.totalQuantity = totalQuantity;
        this.totalAmount = totalAmount;
        this.requestList = requestList;
    }

    public Integer getTotalQuantity() { return totalQuantity; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public List<FranchiseSellItemRequest> getRequestList() { return requestList; }
}
