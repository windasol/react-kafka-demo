package com.example.notificationservice.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusChangedEvent {

    private Long orderId;
    private String productName;
    private String previousStatus;
    private String newStatus;
}
