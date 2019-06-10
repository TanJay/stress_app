package com.tanushaj.element.models;

public class HRVDto {

    String dateTime;
    int rR;
    float HR;

    public HRVDto(String dateTime, int rR, float HR) {
        this.dateTime = dateTime;
        this.rR = rR;
        this.HR = HR;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public int getrR() {
        return rR;
    }

    public void setrR(int rR) {
        this.rR = rR;
    }

    public float getHR() {
        return HR;
    }

    public void setHR(float HR) {
        this.HR = HR;
    }
}
