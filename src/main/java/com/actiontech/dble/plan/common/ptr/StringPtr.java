package com.actiontech.dble.plan.common.ptr;

public class StringPtr {
    private String s;

    public StringPtr(String s) {
        this.s = s;
    }

    public String get() {
        return s;
    }

    public void set(String str) {
        this.s = str;
    }
}
