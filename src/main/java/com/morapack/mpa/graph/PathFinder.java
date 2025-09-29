package com.morapack.mpa.graph;

import com.morapack.mpa.domain.FlightInstance;

import java.util.*;

/**
 * Dijkstra en tiempo discreto (0..167):
 * - Estado = Node (airport@hour)
 * - Costo = hora de llegada (minimizar)
 * - Restricción de conexión mínima: sólo se puede tomar arista de vuelo
 *   si la hora actual cumple con la salida exacta del vuelo (modelado por arista).
 * - Check de capacidad: sólo consideramos aristas de vuelo cuya instancia tenga cupo.
 *
 * Retorna la lista de aristas a "consumir" (para poder reservar cupos).
 */
public class PathFinder {

    public static class PathResult {
        public final List<TEGraph.Edge> edges;
        public final int arrivalHour;
        public PathResult(List<TEGraph.Edge> edges, int arrivalHour) {
            this.edges = edges; this.arrivalHour = arrivalHour;
        }
    }

    /** Búsqueda desde (origen, startHour) hasta primer nodo dest con hora <= deadline. */
    public PathResult find(TEGraph g, TEGraph.Node start, String destAirport, int deadlineHour) {
        // Dijkstra por niveles de hora
        Map<TEGraph.Node, Integer> dist = new HashMap<>();
        Map<TEGraph.Node, TEGraph.Edge> prev = new HashMap<>();
        PriorityQueue<TEGraph.Node> pq = new PriorityQueue<>(Comparator.comparingInt(dist::get));

        dist.put(start, start.hour);
        pq.add(start);

        while (!pq.isEmpty()) {
            TEGraph.Node u = pq.poll();
            int du = dist.get(u);
            if (u.airport.code().equals(destAirport) && du <= deadlineHour) {
                // reconstruir
                List<TEGraph.Edge> edges = new ArrayList<>();
                TEGraph.Node cur = u;
                while (prev.containsKey(cur)) {
                    TEGraph.Edge e = prev.get(cur);
                    edges.add(e);
                    cur = e.from;
                }
                Collections.reverse(edges);
                return new PathResult(edges, du);
            }
            if (du > deadlineHour) continue;

            for (TEGraph.Edge e: g.edges(u)) {
                // Si es vuelo y no hay capacidad, lo ignoramos
                if (e.isFlight) {
                    FlightInstance inst = e.instance;
                    if (inst.remainingCapacity() <= 0) continue;
                }
                int dv = e.to.hour;
                if (!dist.containsKey(e.to) || dv < dist.get(e.to)) {
                    dist.put(e.to, dv);
                    prev.put(e.to, e);
                    pq.add(e.to);
                }
            }
        }
        return null; // no hay ruta factible
    }

    /** Aplica reservas de capacidad a las aristas de vuelo de la ruta. */
    public static boolean reservePath(List<TEGraph.Edge> edges, int units) {
        // Verificamos otra vez (evita condición de carrera si planificas en paralelo)
        for (TEGraph.Edge e: edges) {
            if (e.isFlight && e.instance.remainingCapacity() < units) return false;
        }
        for (TEGraph.Edge e: edges) {
            if (e.isFlight) e.instance.book(units);
        }
        return true;
    }
}
