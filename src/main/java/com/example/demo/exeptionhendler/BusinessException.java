package com.example.demo.exeptionhendler;

public class BusinessException extends RuntimeException {
    private String code;

    public BusinessException(String code, String message) {
        super(message);
        this.setCode(code);
    }

    public BusinessException(String code, String message, Throwable cause) {
        super(message, cause);
        this.setCode(code);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
