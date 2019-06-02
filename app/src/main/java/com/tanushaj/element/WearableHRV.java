package com.tanushaj.element;

public class WearableHRV {
    private String dateTime;
    private Integer rR;
    private Float HR;

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public Integer getrR() {
        return rR;
    }

    public void setrR(Integer rR) {
        this.rR = rR;
    }

    public Float getHR() {
        return HR;
    }

    public void setHR(Float HR) {
        this.HR = HR;
    }
}
