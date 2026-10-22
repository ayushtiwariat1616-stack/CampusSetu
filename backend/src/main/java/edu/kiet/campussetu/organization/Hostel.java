package edu.kiet.campussetu.organization;

import jakarta.persistence.*;

@Entity
@Table(name = "hostels")
public class Hostel {

    @Id
    @Column(length = 50)
    private String code; // e.g. "ARYABHATT", "CV_RAMAN", "TAGORE", "SARASWATI"

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, length = 20)
    private String hostelType; // BOYS, GIRLS

    @Column(length = 50)
    private String rectorQueueCode;

    @Column(nullable = false)
    private boolean active = true;

    public Hostel() {}

    public Hostel(String code, String name, String hostelType, String rectorQueueCode) {
        this.code = code;
        this.name = name;
        this.hostelType = hostelType;
        this.rectorQueueCode = rectorQueueCode;
        this.active = true;
    }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getHostelType() { return hostelType; }
    public void setHostelType(String hostelType) { this.hostelType = hostelType; }

    public String getRectorQueueCode() { return rectorQueueCode; }
    public void setRectorQueueCode(String rectorQueueCode) { this.rectorQueueCode = rectorQueueCode; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
