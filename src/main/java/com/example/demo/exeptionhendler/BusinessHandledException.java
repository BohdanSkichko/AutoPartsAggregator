package com.example.demo.exeptionhendler;

public class BusinessHandledException extends Exception {
    private String code;

    public BusinessHandledException(String code, String message) {
        super(message);
        this.setCode(code);
    }

    public BusinessHandledException(String code, String message, Throwable cause) {
        super(message, cause);
        this.setCode(code);
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
