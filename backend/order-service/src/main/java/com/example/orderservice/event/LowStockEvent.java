package com.example.orderservice.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LowStockEvent {
    private Long productId;
    private String productName;
    private int remainingStock;
    private static final int LOW_STOCK_THRESHOLD = 5;

    public static boolean isLowStock(int stock) {
        return stock <= LOW_STOCK_THRESHOLD;
    }
}
