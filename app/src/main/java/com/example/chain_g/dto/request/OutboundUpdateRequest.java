package com.example.chain_g.dto.request;

import java.util.List;

public class OutboundUpdateRequest {
    private List<String> serialCodes;

    public OutboundUpdateRequest() {
    }

    public OutboundUpdateRequest(List<String> serialCodes) {
        this.serialCodes = serialCodes;
    }

    public List<String> getSerialCodes() {
        return serialCodes;
    }

    public void setSerialCodes(List<String> serialCodes) {
        this.serialCodes = serialCodes;
    }
}
