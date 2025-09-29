package com.morapack.mpa.planner;

import com.morapack.mpa.domain.PackageOrder;

import java.util.ArrayList;
import java.util.List;

/**
 * Solución construida por el decodificador:
 * - Órdenes entregadas y sus tiempos de llegada
 * - Backlog (no entregadas antes de deadline)
 * - Fitness (maximizar)
 */
public class Solution {
    public static class Delivery {
        public final PackageOrder order;
        public final int arrivalHour; // hora de llegada
        public Delivery(PackageOrder order, int arrivalHour) {
            this.order = order; this.arrivalHour = arrivalHour;
        }
    }

    public final List<Delivery> deliveries = new ArrayList<>();
    public final List<PackageOrder> backlog = new ArrayList<>();
    public double fitness;  // mayor es mejor
    public double percentDelivered;

    public int deliveredCount() { return deliveries.size(); }
}
