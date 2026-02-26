package com.saveetha.myjoints;

public class ReferralItem {

    private final int id;
    private final String message;
    private final String patientId;

    public ReferralItem(int id, String message, String patientId) {
        this.id = id;
        this.message = message;
        this.patientId = patientId;
    }

    public int getId() {
        return id;
    }

    public String getMessage() {
        return message;
    }

    public String getPatientId() {
        return patientId;
    }
}
