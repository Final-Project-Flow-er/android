package com.example.chain_g.dto.request;

public class InboundScanItemRequest {
    private String serialCode;
    private Long productId;
    private String manufactureDate;

    public InboundScanItemRequest() {
    }

    public InboundScanItemRequest(String serialCode, Long productId, String manufactureDate) {
        this.serialCode = serialCode;
        this.productId = productId;
        this.manufactureDate = manufactureDate;
    }

    public String getSerialCode() {
        return serialCode;
    }

    public void setSerialCode(String serialCode) {
        this.serialCode = serialCode;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getManufactureDate() {
        return manufactureDate;
    }

    public void setManufactureDate(String manufactureDate) {
        this.manufactureDate = manufactureDate;
    }
}
