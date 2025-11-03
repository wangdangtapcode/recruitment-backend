package com.example.communications_service.service;

import com.example.communications_service.dto.Meta;
import com.example.communications_service.dto.PaginationDTO;
import com.example.communications_service.dto.schedule.ScheduleRequest;
import com.example.communications_service.dto.schedule.ScheduleDetailDTO;
import com.example.communications_service.dto.schedule.ScheduleParticipantDTO;
import com.example.communications_service.model.Schedule;
import com.example.communications_service.model.ScheduleParticipant;
import com.example.communications_service.repository.ScheduleRepository;
import com.example.communications_service.utils.enums.MeetingType;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.IsoFields;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import com.fasterxml.jackson.databind.JsonNode;

@Service
@Transactional
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final UserService userService;
    private final CandidateService candidateService;

    public ScheduleService(ScheduleRepository scheduleRepository, UserService userService,
            CandidateService candidateService) {
        this.scheduleRepository = scheduleRepository;
        this.userService = userService;
        this.candidateService = candidateService;
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

        Schedule savedSchedule = scheduleRepository.save(schedule);

        // Build participants from new DTO fields
        savedSchedule.setParticipants(new java.util.ArrayList<>());
        if (request.getCandidateId() != null) {
            ScheduleParticipant candidate = new ScheduleParticipant();
            candidate.setParticipantType("CANDIDATE");
            candidate.setParticipantId(request.getCandidateId());
            candidate.setResponseStatus("PENDING");
            candidate.setSchedule(savedSchedule);
            savedSchedule.getParticipants().add(candidate);
        }
        if (request.getUserIds() != null) {
            for (Long uid : request.getUserIds()) {
                ScheduleParticipant userP = new ScheduleParticipant();
                userP.setParticipantType("USER");
                userP.setParticipantId(uid);
                userP.setResponseStatus("PENDING");
                userP.setSchedule(savedSchedule);
                savedSchedule.getParticipants().add(userP);
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

        // Rebuild participants per new DTO
        if (schedule.getParticipants() != null) {
            schedule.getParticipants().clear();
        } else {
            schedule.setParticipants(new java.util.ArrayList<>());
        }
        if (request.getCandidateId() != null) {
            ScheduleParticipant candidate = new ScheduleParticipant();
            candidate.setParticipantType("CANDIDATE");
            candidate.setParticipantId(request.getCandidateId());
            candidate.setResponseStatus("PENDING");
            candidate.setSchedule(schedule);
            schedule.getParticipants().add(candidate);
        }
        if (request.getUserIds() != null) {
            for (Long uid : request.getUserIds()) {
                ScheduleParticipant userP = new ScheduleParticipant();
                userP.setParticipantType("USER");
                userP.setParticipantId(uid);
                userP.setResponseStatus("PENDING");
                userP.setSchedule(schedule);
                schedule.getParticipants().add(userP);
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

    private ScheduleDetailDTO toDetailDTO(Schedule schedule, String token, Map<Long, String> userMap,
            Map<Long, String> candidateMap) {
        java.util.List<ScheduleParticipantDTO> participantDTOs = new java.util.ArrayList<>();
        if (schedule.getParticipants() != null) {
            for (var p : schedule.getParticipants()) {
                String name = "Unknown";
                if ("USER".equalsIgnoreCase(p.getParticipantType()) && userMap != null) {
                    name = userMap.getOrDefault(p.getParticipantId(), "Unknown");
                } else if ("CANDIDATE".equalsIgnoreCase(p.getParticipantType()) && candidateMap != null) {
                    name = candidateMap.getOrDefault(p.getParticipantId(), "Unknown");
                }
                participantDTOs.add(new ScheduleParticipantDTO(
                        p.getId(), p.getParticipantType(), p.getResponseStatus(), p.getParticipantId(), name));
            }
        }
        ScheduleDetailDTO dto = new ScheduleDetailDTO();
        dto.setId(schedule.getId());
        dto.setTitle(schedule.getTitle());
        dto.setDescription(schedule.getDescription());
        dto.setFormat(schedule.getFormat());
        dto.setMeetingType(schedule.getMeetingType());
        dto.setStatus(schedule.getStatus());
        dto.setLocation(schedule.getLocation());
        dto.setStartTime(schedule.getStartTime());
        dto.setEndTime(schedule.getEndTime());
        dto.setTimezone(schedule.getTimezone());
        dto.setReminderTime(schedule.getReminderTime());
        dto.setRoomId(schedule.getRoomId());
        dto.setCreatedById(schedule.getCreatedById());
        dto.setCreatedAt(schedule.getCreatedAt());
        dto.setUpdatedAt(schedule.getUpdatedAt());
        dto.setParticipants(participantDTOs);
        return dto;
    }

    public ScheduleDetailDTO getScheduleWithParticipantNames(Long id, String token) {
        Schedule schedule = getScheduleById(id);
        Set<Long> userIds = new java.util.HashSet<>();
        Set<Long> candidateIds = new java.util.HashSet<>();
        if (schedule.getParticipants() != null) {
            for (var p : schedule.getParticipants()) {
                if ("USER".equalsIgnoreCase(p.getParticipantType())) {
                    userIds.add(p.getParticipantId());
                } else if ("CANDIDATE".equalsIgnoreCase(p.getParticipantType())) {
                    candidateIds.add(p.getParticipantId());
                }
            }
        }
        Map<Long, String> userMap = new HashMap<>();
        Map<Long, String> candidateMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            var res = userService.getUserNames(new java.util.ArrayList<>(userIds), token);
            if (res.getStatusCode().is2xxSuccessful() && res.getBody() != null) {
                JsonNode map = res.getBody();
                map.fields().forEachRemaining(e -> userMap.put(Long.valueOf(e.getKey()), e.getValue().asText()));
            }
        }
        if (!candidateIds.isEmpty()) {
            var res = candidateService.getCandidateNames(new java.util.ArrayList<>(candidateIds), token);
            if (res.getStatusCode().is2xxSuccessful() && res.getBody() != null) {
                JsonNode map = res.getBody();
                map.fields().forEachRemaining(e -> candidateMap.put(Long.valueOf(e.getKey()), e.getValue().asText()));
            }
        }
        return toDetailDTO(schedule, token, userMap, candidateMap);
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
                                    && p.getParticipantType().equalsIgnoreCase(participantType)))
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

    public List<ScheduleDetailDTO> getSchedulesDetailed(LocalDate day, Integer week, Integer month,
            Integer year,
            String status, String meetingType, Long participantId, String participantType, String token,
            LocalDate startDate, LocalDate endDate) {
        List<Schedule> schedules;
        if (startDate != null && endDate != null) {
            schedules = scheduleRepository.findByStartTimeBetween(startDate.atStartOfDay(),
                    endDate.atTime(LocalTime.MAX),
                    Sort.by("startTime"));
        } else if (day != null) {
            LocalDateTime start = day.atStartOfDay();
            LocalDateTime end = day.atTime(LocalTime.MAX);
            schedules = scheduleRepository.findByStartTimeBetween(start, end, Sort.by("startTime"));
        } else if (week != null && year != null) {
            LocalDate start = LocalDate.ofYearDay(year, 1).with(IsoFields.WEEK_OF_WEEK_BASED_YEAR,
                    week);
            LocalDate end = start.plusDays(6);
            schedules = scheduleRepository.findByStartTimeBetween(start.atStartOfDay(), end.atTime(LocalTime.MAX),
                    Sort.by("startTime"));
        } else if (month != null && year != null) {
            LocalDate first = LocalDate.of(year, month, 1);
            LocalDate last = first.withDayOfMonth(first.lengthOfMonth());
            schedules = scheduleRepository.findByStartTimeBetween(first.atStartOfDay(), last.atTime(LocalTime.MAX),
                    Sort.by("startTime"));
        } else if (year != null) {
            LocalDate first = LocalDate.of(year, 1, 1);
            LocalDate last = LocalDate.of(year, 12, 31);
            schedules = scheduleRepository.findByStartTimeBetween(first.atStartOfDay(), last.atTime(LocalTime.MAX),
                    Sort.by("startTime"));
        } else {
            schedules = scheduleRepository.findAll(Sort.by("startTime"));
        }
        if (status != null) {
            schedules = schedules.stream().filter(s -> status.equalsIgnoreCase(s.getStatus())).toList();
        }
        if (meetingType != null) {
            schedules = schedules.stream().filter(s -> meetingType.equalsIgnoreCase(s.getMeetingType().name()))
                    .toList();
        }
        if (participantId != null && participantType != null) {
            schedules = schedules.stream().filter(s -> s.getParticipants() != null &&
                    s.getParticipants().stream().anyMatch(p -> participantId.equals(p.getParticipantId())
                            && participantType.equalsIgnoreCase(p.getParticipantType())))
                    .toList();
        }
        // Batch user/candidate names resolve
        Set<Long> allUserIds = new java.util.HashSet<>();
        Set<Long> allCandidateIds = new java.util.HashSet<>();
        for (Schedule s : schedules) {
            if (s.getParticipants() == null)
                continue;
            for (var p : s.getParticipants()) {
                if ("USER".equalsIgnoreCase(p.getParticipantType())) {
                    allUserIds.add(p.getParticipantId());
                } else if ("CANDIDATE".equalsIgnoreCase(p.getParticipantType())) {
                    allCandidateIds.add(p.getParticipantId());
                }
            }
        }
        Map<Long, String> userMap = new HashMap<>();
        Map<Long, String> candidateMap = new HashMap<>();
        if (!allUserIds.isEmpty()) {
            var res = userService.getUserNames(new java.util.ArrayList<>(allUserIds), token);
            if (res.getStatusCode().is2xxSuccessful() && res.getBody() != null) {
                JsonNode map = res.getBody();
                map.fields().forEachRemaining(e -> userMap.put(Long.valueOf(e.getKey()), e.getValue().asText()));
            }
        }
        if (!allCandidateIds.isEmpty()) {
            var res = candidateService.getCandidateNames(new java.util.ArrayList<>(allCandidateIds), token);
            if (res.getStatusCode().is2xxSuccessful() && res.getBody() != null) {
                JsonNode map = res.getBody();
                map.fields().forEachRemaining(e -> candidateMap.put(Long.valueOf(e.getKey()), e.getValue().asText()));
            }
        }
        return schedules.stream().map(s -> toDetailDTO(s, token, userMap, candidateMap)).toList();
    }
}
