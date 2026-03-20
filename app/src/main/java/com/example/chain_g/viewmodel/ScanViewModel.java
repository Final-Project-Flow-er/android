package com.example.chain_g.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ScanViewModel extends ViewModel {
    private final MutableLiveData<String> boxCode = new MutableLiveData<>();
    private final MutableLiveData<String> orderCode = new MutableLiveData<>();
    private final MutableLiveData<Long> orderItemId = new MutableLiveData<>();

    public void setBoxData(String boxCode, String orderCode) {
        this.boxCode.setValue(boxCode);
        this.orderCode.setValue(orderCode);
    }

    public void setBoxData(String boxCode, String orderCode, Long orderItemId) {
        this.boxCode.setValue(boxCode);
        this.orderCode.setValue(orderCode);
        this.orderItemId.setValue(orderItemId);
    }

    public LiveData<String> getBoxCode() {
        return boxCode;
    }

    public LiveData<String> getOrderCode() {
        return orderCode;
    }

    public LiveData<Long> getOrderItemId() {
        return orderItemId;
    }

    public String getBoxCodeValue() {
        return boxCode.getValue();
    }

    public String getOrderCodeValue() {
        return orderCode.getValue();
    }

    public Long getOrderItemIdValue() {
        return orderItemId.getValue();
    }
}
