package com.example.chain_g.dto.request;

public class InboundScanBoxRequest {
    private String boxCode;

    public InboundScanBoxRequest() {
    }

    public InboundScanBoxRequest(String boxCode) {
        this.boxCode = boxCode;
    }

    public String getBoxCode() {
        return boxCode;
    }

    public void setBoxCode(String boxCode) {
        this.boxCode = boxCode;
    }
}

