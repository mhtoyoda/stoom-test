package com.stoom.model.google;

public class GoogleGeoCode {

    private String status;
    private GoogleGeoResult [] results;
    private String error_message;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public GoogleGeoResult[] getResults() {
        return results;
    }

    public void setResults(GoogleGeoResult[] results) {
        this.results = results;
    }

    public String getError_message() {
        return error_message;
    }

    public void setError_message(String error_message) {
        this.error_message = error_message;
    }
}
