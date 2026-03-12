package com.example.chain_g.dto.request;

import java.util.List;

public class OutboundAssignRequest {
    private String boxCode;
    private List<String> serialCodes;

    public OutboundAssignRequest() {
    }

    public OutboundAssignRequest(String boxCode, List<String> serialCodes) {
        this.boxCode = boxCode;
        this.serialCodes = serialCodes;
    }

    public String getBoxCode() {
        return boxCode;
    }

    public void setBoxCode(String boxCode) {
        this.boxCode = boxCode;
    }

    public List<String> getSerialCodes() {
        return serialCodes;
    }

    public void setSerialCodes(List<String> serialCodes) {
        this.serialCodes = serialCodes;
    }
}
