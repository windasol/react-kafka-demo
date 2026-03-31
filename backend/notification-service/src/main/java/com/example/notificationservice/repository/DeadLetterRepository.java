package com.example.notificationservice.repository;

import com.example.notificationservice.entity.DeadLetterMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DeadLetterRepository extends JpaRepository<DeadLetterMessage, Long> {

    List<DeadLetterMessage> findByRetriedFalseOrderByFailedAtDesc();
}
