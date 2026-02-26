package com.saveetha.myjoints;

public class TreatmentRecord {

    private final int id;
    private final String medicationName;
    private final String dose;
    private final String route;
    private final String frequencyNumber;
    private final String frequencyText;
    private final String duration;
    private final String patientId;

    public TreatmentRecord(
            int id,
            String medicationName,
            String dose,
            String route,
            String frequencyNumber,
            String frequencyText,
            String duration,
            String patientId
    ) {
        this.id = id;
        this.medicationName = medicationName;
        this.dose = dose;
        this.route = route;
        this.frequencyNumber = frequencyNumber;
        this.frequencyText = frequencyText;
        this.duration = duration;
        this.patientId = patientId;
    }

    public int getId() { return id; }
    public String getMedicationName() { return medicationName; }
    public String getDose() { return dose; }
    public String getRoute() { return route; }
    public String getFrequencyNumber() { return frequencyNumber; }
    public String getFrequencyText() { return frequencyText; }
    public String getDuration() { return duration; }
}
