package com.vsms.authservice.service;

import com.vsms.authservice.dto.request.TechnicianCreateRequest;
import com.vsms.authservice.dto.response.TechnicianResponse;
import com.vsms.authservice.entity.AppUser;
import com.vsms.authservice.entity.Technician;
import com.vsms.authservice.enums.Role;
import com.vsms.authservice.enums.Specialization;
import com.vsms.authservice.enums.UserStatus;
import com.vsms.authservice.exception.BadRequestException;
import com.vsms.authservice.exception.DuplicateResourceException;
import com.vsms.authservice.exception.ResourceNotFoundException;
import com.vsms.authservice.repository.AppUserRepository;
import com.vsms.authservice.repository.TechnicianRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class TechnicianService {

    private final TechnicianRepository technicianRepository;
    private final AppUserRepository appUserRepository;

    /**
     * Register a new technician (status = PENDING until admin approves)
     */
    public TechnicianResponse createTechnician(TechnicianCreateRequest request) {
        if (appUserRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("User", "email", request.getEmail());
        }

        AppUser appUser = AppUser.builder()
                .email(request.getEmail())
                .passwordHash(request.getPassword()) // TODO: Hash when auth ready
                .phone(request.getPhone())
                .role(Role.TECHNICIAN)
                .status(UserStatus.PENDING) // Requires admin approval
                .build();

        Technician technician = Technician.builder()
                .user(appUser)
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .specialization(request.getSpecialization())
                .experienceYears(request.getExperienceYears() != null ? request.getExperienceYears() : 0)
                .onDuty(false) // Default off duty until approved
                .currentWorkload(0)
                .maxCapacity(5)
                .build();

        Technician saved = technicianRepository.save(technician);
        return mapToResponse(saved);
    }

    /**
     * Get technician by ID
     */
    @Transactional(readOnly = true)
    public TechnicianResponse getTechnicianById(Integer id) {
        Technician technician = technicianRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Technician", "id", id));
        return mapToResponse(technician);
    }

    /**
     * Get all technicians
     */
    @Transactional(readOnly = true)
    public List<TechnicianResponse> getAllTechnicians() {
        return technicianRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get available technicians (on duty + has capacity)
     */
    @Transactional(readOnly = true)
    public List<TechnicianResponse> getAvailableTechnicians() {
        return technicianRepository.findAvailableTechnicians()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get available technicians by specialization (for assignment)
     */
    @Transactional(readOnly = true)
    public List<TechnicianResponse> getAvailableBySpecialization(Specialization specialization) {
        return technicianRepository.findAvailableBySpecialization(specialization)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get technicians pending approval
     */
    @Transactional(readOnly = true)
    public List<TechnicianResponse> getPendingTechnicians() {
        return technicianRepository.findPendingApproval()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Admin approves technician registration
     */
    public TechnicianResponse approveTechnician(Integer id) {
        Technician technician = technicianRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Technician", "id", id));

        if (technician.getUser().getStatus() != UserStatus.PENDING) {
            throw new BadRequestException("Technician is not pending approval");
        }

        technician.getUser().setStatus(UserStatus.ACTIVE);
        technician.setOnDuty(true);

        // Generate employee ID
        String employeeId = "TECH-" + String.format("%05d", technician.getId());
        technician.setEmployeeId(employeeId);

        Technician updated = technicianRepository.save(technician);
        return mapToResponse(updated);
    }

    /**
     * Admin rejects technician registration
     */
    public void rejectTechnician(Integer id) {
        Technician technician = technicianRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Technician", "id", id));

        if (technician.getUser().getStatus() != UserStatus.PENDING) {
            throw new BadRequestException("Technician is not pending approval");
        }

        technician.getUser().setStatus(UserStatus.INACTIVE);
        technicianRepository.save(technician);
    }

    /**
     * Toggle technician duty status
     */
    public TechnicianResponse toggleDutyStatus(Integer id) {
        Technician technician = technicianRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Technician", "id", id));

        technician.setOnDuty(!technician.getOnDuty());
        Technician updated = technicianRepository.save(technician);
        return mapToResponse(updated);
    }

    /**
     * Increment workload (when assigned a task)
     */
    public void incrementWorkload(Integer id) {
        Technician technician = technicianRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Technician", "id", id));

        if (technician.getCurrentWorkload() >= technician.getMaxCapacity()) {
            throw new BadRequestException("Technician has reached maximum capacity");
        }

        technicianRepository.incrementWorkload(id);
    }

    /**
     * Decrement workload (when task completed)
     */
    public void decrementWorkload(Integer id) {
        technicianRepository.decrementWorkload(id);
    }

    /**
     * Get technician count
     */
    @Transactional(readOnly = true)
    public long getTechnicianCount() {
        return technicianRepository.count();
    }

    /**
     * Get pending count for admin dashboard
     */
    @Transactional(readOnly = true)
    public long getPendingCount() {
        return technicianRepository.countPending();
    }

    /**
     * Deactivate technician
     */
    public void deleteTechnician(Integer id) {
        Technician technician = technicianRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Technician", "id", id));
        technician.getUser().setStatus(UserStatus.INACTIVE);
        technician.setOnDuty(false);
        technicianRepository.save(technician);
    }

    /**
     * Map entity to response DTO
     */
    private TechnicianResponse mapToResponse(Technician technician) {
        return TechnicianResponse.builder()
                .id(technician.getId())
                .userId(technician.getUser().getId())
                .email(technician.getUser().getEmail())
                .phone(technician.getUser().getPhone())
                .firstName(technician.getFirstName())
                .lastName(technician.getLastName())
                .employeeId(technician.getEmployeeId())
                .specialization(technician.getSpecialization())
                .experienceYears(technician.getExperienceYears())
                .onDuty(technician.getOnDuty())
                .currentWorkload(technician.getCurrentWorkload())
                .maxCapacity(technician.getMaxCapacity())
                .rating(technician.getRating())
                .status(technician.getUser().getStatus())
                .createdAt(technician.getCreatedAt())
                .build();
    }
}
