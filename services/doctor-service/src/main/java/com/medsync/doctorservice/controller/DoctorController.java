package com.medsync.doctorservice.controller;

import com.medsync.doctorservice.dto.request.CreateDoctorRequest;
import com.medsync.doctorservice.dto.request.UpdateDoctorRequest;
import com.medsync.doctorservice.dto.response.DoctorResponse;
import com.medsync.doctorservice.service.DoctorService;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/doctor")
public class DoctorController {

    private final DoctorService doctorService;

    @GetMapping()
    public ResponseEntity<Page<DoctorResponse>> getAllDoctor(Pageable pageable) {
        return ResponseEntity.ok(doctorService.getAllDoctor(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DoctorResponse> getDoctorById(@PathVariable UUID id) {
        return ResponseEntity.ok(doctorService.getDoctorById(id));
    }

    @PostMapping()
    public ResponseEntity<DoctorResponse> createDoctor(@Valid @RequestBody CreateDoctorRequest request) {
        DoctorResponse doctor = doctorService.createDoctor(request);
        return ResponseEntity.created(ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(doctor.id())
                .toUri()).body(doctor);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DoctorResponse> updateDoctor(@PathVariable UUID id, @Valid @RequestBody UpdateDoctorRequest request) {
        return ResponseEntity.ok(doctorService.updateDoctor(id, request));
    }

}
