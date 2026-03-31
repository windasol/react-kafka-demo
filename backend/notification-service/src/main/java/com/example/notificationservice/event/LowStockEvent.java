package com.example.notificationservice.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LowStockEvent {

    private Long productId;
    private String productName;
    private Integer remainingStock;
}
