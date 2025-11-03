package com.example.communications_service.repository;

import com.example.communications_service.model.Schedule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    Page<Schedule> findByStatus(String status, Pageable pageable);

    Page<Schedule> findByMeetingType(String meetingType, Pageable pageable);

    Page<Schedule> findByStartTimeBetween(LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);

    List<Schedule> findByStartTimeBetween(LocalDateTime startTime, LocalDateTime endTime, Sort sort);

    List<Schedule> findAll(Sort sort);
}
