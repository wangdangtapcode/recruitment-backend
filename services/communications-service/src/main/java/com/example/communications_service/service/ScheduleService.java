package com.example.communications_service.service;

import com.example.communications_service.dto.Meta;
import com.example.communications_service.dto.PaginationDTO;
import com.example.communications_service.dto.schedule.ScheduleRequest;
import com.example.communications_service.model.Schedule;
import com.example.communications_service.model.ScheduleParticipant;
import com.example.communications_service.repository.ScheduleRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@Transactional
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;

    public ScheduleService(ScheduleRepository scheduleRepository) {
        this.scheduleRepository = scheduleRepository;
    }

    public Schedule createSchedule(ScheduleRequest request) {
        Schedule schedule = new Schedule();
        schedule.setTitle(request.getTitle());
        schedule.setDescription(request.getDescription());
        schedule.setFormat(request.getFormat());
        schedule.setMeetingType(request.getMeetingType());
        schedule.setStatus(request.getStatus() != null ? request.getStatus() : "SCHEDULED");
        schedule.setLocation(request.getLocation());
        schedule.setStartTime(request.getStartTime());
        schedule.setEndTime(request.getEndTime());
        schedule.setTimezone(request.getTimezone());
        schedule.setReminderTime(request.getReminderTime());
        schedule.setCreatedById(request.getCreatedById());

        // Save schedule first to get ID
        Schedule savedSchedule = scheduleRepository.save(schedule);

        // Add participants if provided
        if (request.getParticipants() != null && !request.getParticipants().isEmpty()) {
            for (ScheduleRequest.ParticipantRequest participantRequest : request.getParticipants()) {
                ScheduleParticipant participant = new ScheduleParticipant();
                participant.setParticipantType(participantRequest.getParticipantType());
                participant.setParticipantId(participantRequest.getParticipantId());
                participant.setResponseStatus(
                        participantRequest.getResponseStatus() != null ? participantRequest.getResponseStatus()
                                : "PENDING");
                participant.setSchedule(savedSchedule);
                savedSchedule.getParticipants().add(participant);
            }
        }

        return scheduleRepository.save(savedSchedule);
    }

    public Schedule updateSchedule(Long id, ScheduleRequest request) {
        Schedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("lịch hẹn không tồn tại với id: " + id));

        schedule.setTitle(request.getTitle());
        schedule.setDescription(request.getDescription());
        schedule.setFormat(request.getFormat());
        schedule.setMeetingType(request.getMeetingType());
        schedule.setStatus(request.getStatus());
        schedule.setLocation(request.getLocation());
        schedule.setStartTime(request.getStartTime());
        schedule.setEndTime(request.getEndTime());
        schedule.setTimezone(request.getTimezone());
        schedule.setReminderTime(request.getReminderTime());
        // Update participants
        if (request.getParticipants() != null) {
            schedule.getParticipants().clear();
            for (ScheduleRequest.ParticipantRequest participantRequest : request.getParticipants()) {
                ScheduleParticipant participant = new ScheduleParticipant();
                participant.setParticipantType(participantRequest.getParticipantType());
                participant.setParticipantId(participantRequest.getParticipantId());
                participant.setResponseStatus(
                        participantRequest.getResponseStatus() != null ? participantRequest.getResponseStatus()
                                : "PENDING");
                participant.setSchedule(schedule);
                schedule.getParticipants().add(participant);
            }
        }

        return scheduleRepository.save(schedule);
    }

    public void deleteSchedule(Long id) {
        if (!scheduleRepository.existsById(id)) {
            throw new RuntimeException("lịch hẹn không tồn tại với id: " + id);
        }
        scheduleRepository.deleteById(id);
    }

    public Schedule getScheduleById(Long id) {
        return scheduleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("lịch hẹn không tồn tại với id: " + id));
    }

    public PaginationDTO getAllSchedules(int page, int limit, String sortBy, String sortOrder,
            LocalDate date, Integer year, Integer month, String status, String meetingType,
            Long participantId, String participantType) {
        // Validate pagination parameters
        if (page < 1)
            page = 1;
        if (limit < 1 || limit > 100)
            limit = 10;

        // Create sort object
        Sort.Direction direction = sortOrder.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sort = Sort.by(direction, sortBy);

        // Create pageable object
        Pageable pageable = PageRequest.of(page - 1, limit, sort);

        Page<Schedule> schedulePage;

        // Apply filters based on parameters
        if (date != null) {
            // Filter by specific date
            LocalDateTime startOfDay = date.atStartOfDay();
            LocalDateTime endOfDay = date.atTime(LocalTime.MAX);
            schedulePage = scheduleRepository.findByStartTimeBetween(startOfDay, endOfDay, pageable);
        } else if (year != null && month != null) {
            // Filter by year and month
            LocalDate startOfMonth = LocalDate.of(year, month, 1);
            LocalDate endOfMonth = startOfMonth.withDayOfMonth(startOfMonth.lengthOfMonth());
            LocalDateTime startDateTime = startOfMonth.atStartOfDay();
            LocalDateTime endDateTime = endOfMonth.atTime(LocalTime.MAX);
            schedulePage = scheduleRepository.findByStartTimeBetween(startDateTime, endDateTime, pageable);
        } else if (status != null) {
            // Filter by status
            schedulePage = scheduleRepository.findByStatus(status, pageable);
        } else if (meetingType != null) {
            // Filter by meeting type
            schedulePage = scheduleRepository.findByMeetingType(meetingType, pageable);
        } else {
            // No filters, get all
            schedulePage = scheduleRepository.findAll(pageable);
        }

        // Additional filtering for participant if specified
        if (participantId != null && participantType != null) {
            List<Schedule> filteredSchedules = schedulePage.getContent().stream()
                    .filter(schedule -> schedule.getParticipants().stream()
                            .anyMatch(p -> p.getParticipantId().equals(participantId)
                                    && p.getParticipantType().equals(participantType)))
                    .toList();

            // Create a custom page with filtered results
            schedulePage = new org.springframework.data.domain.PageImpl<>(
                    filteredSchedules, pageable, filteredSchedules.size());
        }

        Meta meta = new Meta();
        meta.setPage(page);
        meta.setPageSize(limit);
        meta.setTotal(schedulePage.getTotalElements());
        meta.setPages(schedulePage.getTotalPages());

        PaginationDTO dto = new PaginationDTO();
        dto.setResult(schedulePage.getContent());
        dto.setMeta(meta);
        return dto;
    }

    public Schedule updateScheduleStatus(Long id, String status) {
        Schedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lịch hẹn không tồn tại với id: " + id));
        schedule.setStatus(status);
        return scheduleRepository.save(schedule);
    }
}
