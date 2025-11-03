package com.example.test_service.dto;

public class Response<T> {
    private int statusCode;
    private String error;
    private Object message;
    private T data;

    public Response(int statusCode, String error, Object message, T data) {
        this.statusCode = statusCode;
        this.error = error;
        this.message = message;
        this.data = data;
    }

    public Response() {
        this.statusCode = 200;
        this.error = null;
        this.message = null;
        this.data = null;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getError() {
        return error;
    }

    public Object getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public void setError(String error) {
        this.error = error;
    }

    public void setMessage(Object message) {
        this.message = message;
    }

    public void setData(T data) {
        this.data = data;
    }
}
