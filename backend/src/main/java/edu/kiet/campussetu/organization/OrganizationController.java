package edu.kiet.campussetu.organization;

import edu.kiet.campussetu.common.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/organization")
public class OrganizationController {

    private final DepartmentRepository departmentRepository;
    private final HostelRepository hostelRepository;
    private final CampusServiceRepository serviceRepository;

    public OrganizationController(DepartmentRepository departmentRepository,
                                  HostelRepository hostelRepository,
                                  CampusServiceRepository serviceRepository) {
        this.departmentRepository = departmentRepository;
        this.hostelRepository = hostelRepository;
        this.serviceRepository = serviceRepository;
    }

    @GetMapping("/departments")
    public ResponseEntity<ApiResponse<List<Department>>> getDepartments() {
        return ResponseEntity.ok(ApiResponse.ok(departmentRepository.findByActiveTrue()));
    }

    @GetMapping("/hostels")
    public ResponseEntity<ApiResponse<List<Hostel>>> getHostels() {
        return ResponseEntity.ok(ApiResponse.ok(hostelRepository.findByActiveTrue()));
    }

    @GetMapping("/services")
    public ResponseEntity<ApiResponse<List<CampusService>>> getServices() {
        return ResponseEntity.ok(ApiResponse.ok(serviceRepository.findByActiveTrue()));
    }
}
