package com.saveetha.myjoints;

import java.util.List;

public class InvestigationItem {

    private final int id;                 // ✅ REQUIRED FOR DELETE
    private final String title;
    private final List<String> details;

    // ✅ NEW CONSTRUCTOR (USED BY ACTIVITY)
    public InvestigationItem(int id, String title, List<String> details) {
        this.id = id;
        this.title = title;
        this.details = details;
    }

    // ✅ REQUIRED FOR DELETE
    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public List<String> getDetails() {
        return details;
    }
}
