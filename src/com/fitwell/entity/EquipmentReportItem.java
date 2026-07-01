package com.fitwell.entity;

public class EquipmentReportItem {
    private String typeName;
    private String category; 
    private int timesUsed;   

    public EquipmentReportItem(String typeName, String category, int timesUsed) {
        this.typeName = typeName;
        this.category = category;
        this.timesUsed = timesUsed;
    }

    public String getTypeName() { return typeName; }
    public String getCategory() { return category; }
    public int getTimesUsed() { return timesUsed; }
}

