package ru.backspark.SockKeeper.error;

public class ErrorResponse {
    final String error;

    public ErrorResponse(String error) {
        this.error = error;
    }

    public String getError() {
        return error;
    }
}
