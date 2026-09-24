package com.ticketing.model;

import jakarta.persistence.*;

@Entity
@Table(name = "project_sequences")
public class ProjectSequence {

    @Id
    @Column(name = "project_code", length = 3)
    private String projectCode;

    @Column(name = "last_sequence", nullable = false)
    private long lastSequence = 0;

    public ProjectSequence() {}

    public ProjectSequence(String projectCode, long lastSequence) {
        this.projectCode = projectCode;
        this.lastSequence = lastSequence;
    }

    public String getProjectCode() { return projectCode; }
    public void setProjectCode(String projectCode) { this.projectCode = projectCode; }

    public long getLastSequence() { return lastSequence; }
    public void setLastSequence(long lastSequence) { this.lastSequence = lastSequence; }
}
