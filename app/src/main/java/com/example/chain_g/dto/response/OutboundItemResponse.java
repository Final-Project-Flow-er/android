package com.example.chain_g.dto.response;

public class OutboundItemResponse {
    private String serialCode;
    private Long productId;
    private String productName;
    private String manufactureDate;
    private Boolean isPicking;

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

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getManufactureDate() {
        return manufactureDate;
    }

    public void setManufactureDate(String manufactureDate) {
        this.manufactureDate = manufactureDate;
    }

    public Boolean getPicking() {
        return isPicking;
    }

    public void setPicking(Boolean picking) {
        isPicking = picking;
    }
}
