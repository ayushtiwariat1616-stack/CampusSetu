package edu.kiet.campussetu.organization;

import jakarta.persistence.*;

@Entity
@Table(name = "departments")
public class Department {

    @Id
    @Column(length = 50)
    private String code; // e.g. "AIML", "CSE", "IT", "ECE", "EN", "ME", "CE", "MCA"

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 100)
    private String block;

    @Column(nullable = false)
    private boolean active = true;

    public Department() {}

    public Department(String code, String name, String block) {
        this.code = code;
        this.name = name;
        this.block = block;
        this.active = true;
    }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getBlock() { return block; }
    public void setBlock(String block) { this.block = block; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
