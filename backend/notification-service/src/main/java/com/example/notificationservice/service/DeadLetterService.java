package com.example.notificationservice.service;

import com.example.notificationservice.entity.DeadLetterMessage;
import com.example.notificationservice.repository.DeadLetterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeadLetterService {

    private final DeadLetterRepository deadLetterRepository;

    @Transactional(readOnly = true)
    public List<DeadLetterMessage> getPendingMessages() {
        return deadLetterRepository.findByRetriedFalseOrderByFailedAtDesc();
    }

    @Transactional
    public void markRetried(Long id) {
        DeadLetterMessage msg = deadLetterRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("DLQ 메시지를 찾을 수 없습니다. id=" + id));
        msg.markRetried();
    }
}
