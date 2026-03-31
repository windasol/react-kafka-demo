package com.example.orderservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.List;

@Getter
@AllArgsConstructor
public class OrderStatsSummary {
    private long totalOrders;
    private long pendingOrders;
    private long completedOrders;
    private long cancelledOrders;
    private double totalRevenue;
    private List<DailyStat> dailyStats;

    @Getter
    @AllArgsConstructor
    public static class DailyStat {
        private String date;
        private long count;
        private double revenue;
    }
}
