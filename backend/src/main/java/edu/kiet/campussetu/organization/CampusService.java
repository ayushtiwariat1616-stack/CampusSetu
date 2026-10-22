package edu.kiet.campussetu.organization;

import jakarta.persistence.*;

@Entity
@Table(name = "campus_services")
public class CampusService {

    @Id
    @Column(length = 50)
    private String code; // IT_SERVICES, CAMPUS_MAINTENANCE, TRANSPORT, LIBRARY

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 250)
    private String description;

    @Column(nullable = false)
    private boolean active = true;

    public CampusService() {}

    public CampusService(String code, String name, String description) {
        this.code = code;
        this.name = name;
        this.description = description;
        this.active = true;
    }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
