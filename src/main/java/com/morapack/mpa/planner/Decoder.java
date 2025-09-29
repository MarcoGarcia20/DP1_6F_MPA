package com.morapack.mpa.planner;

import com.morapack.mpa.domain.PackageOrder;
import com.morapack.mpa.graph.PathFinder;
import com.morapack.mpa.graph.TEGraph;

import java.util.*;

/**
 * Decodifica un vector continuo de prioridades -> orden de ruteo de paquetes.
 * Rutea secuencialmente cada paquete con Dijkstra respetando capacidad/tiempo.
 * Si no hay ruta antes del deadline, la orden pasa a backlog.
 */
public class Decoder {
    private final TEGraph graph;
    private final PathFinder dijkstra = new PathFinder();

    public Decoder(TEGraph graph) { this.graph = graph; }

    public Solution decode(double[] priorities, List<PackageOrder> orders) {
        // 1) Ordenar órdenes por prioridad (mayor primero)
        Integer[] idx = new Integer[orders.size()];
        for (int i = 0; i < orders.size(); i++) idx[i] = i;
        Arrays.sort(idx, Comparator.comparingDouble((Integer i) -> priorities[i]).reversed());

        Solution sol = new Solution();

        // 2) Ruteo secuencial desde hora 0
        for (int k = 0; k < idx.length; k++) {
            int i = idx[k];
            PackageOrder ord = orders.get(i);
            TEGraph.Node start = graph.node(ord.origin().code(), 0);
            PathFinder.PathResult pr = dijkstra.find(graph, start, ord.destination().code(), ord.deadlineHour());
            if (pr == null) {
                sol.backlog.add(ord);
            } else {
                boolean reserved = PathFinder.reservePath(pr.edges, ord.sizeUnits());
                if (reserved) {
                    sol.deliveries.add(new Solution.Delivery(ord, pr.arrivalHour));
                } else {
                    sol.backlog.add(ord);
                }
            }
        }

        // 3) Evaluación simple: entregas y retraso (si llegara al límite exacto, no penaliza)
        int delivered = sol.deliveredCount();
        int total = orders.size();
        sol.percentDelivered = (100.0 * delivered) / Math.max(1, total);

        double penalty = 0.0; // se puede extender con retraso esperado
        sol.fitness = delivered * 1000.0 - penalty; // escala para priorizar entregas
        return sol;
    }
}
