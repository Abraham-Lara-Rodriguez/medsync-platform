package com.medsync.appointmentservice.controller;

import com.medsync.appointmentservice.dto.request.CreateAppointmentRequest;
import com.medsync.appointmentservice.dto.request.UpdateAppointmentRequest;
import com.medsync.appointmentservice.dto.request.UpdateAppointmentStatusRequest;
import com.medsync.appointmentservice.dto.response.AppointmentResponse;
import com.medsync.appointmentservice.service.AppointmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;

    @GetMapping
    @PreAuthorize("hasAuthority('APPOINTMENT_READ')")
    public ResponseEntity<Page<AppointmentResponse>> getAllAppointments(Pageable pageable) {
        return ResponseEntity.ok(appointmentService.getAllAppointments(pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('APPOINTMENT_READ')")
    public ResponseEntity<AppointmentResponse> getAppointmentById(@PathVariable UUID id) {
        return ResponseEntity.ok(appointmentService.getAppointmentById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('APPOINTMENT_CREATE')")
    public ResponseEntity<AppointmentResponse> createAppointment(@Valid @RequestBody CreateAppointmentRequest request) {
        AppointmentResponse create = appointmentService.createAppointment(request);
        return ResponseEntity.created(URI.create("/api/v1/appointments/" + create.id())).body(create);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('APPOINTMENT_UPDATE')")
    public ResponseEntity<?> updateAppointment(@Valid @RequestBody UpdateAppointmentRequest request, @PathVariable UUID id) {
        return ResponseEntity.ok(appointmentService.updateAppointment(request, id));
    }

    @PatchMapping("/status/{id}")
    @PreAuthorize("hasAuthority('APPOINTMENT_CANCEL')")
    public ResponseEntity<?> updateAppointmentStatus(@Valid @RequestBody UpdateAppointmentStatusRequest request, @PathVariable UUID id) {
        return ResponseEntity.ok(appointmentService.updateAppointmentStatus(request, id));
    }

}
