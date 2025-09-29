package com.morapack.mpa.graph;

import com.morapack.mpa.domain.Airport;
import com.morapack.mpa.domain.Flight;
import com.morapack.mpa.domain.FlightInstance;

import java.util.*;

/**
 * Grafo tiempo-expandido semanal:
 * - Nodo (airportCode, hour)
 * - Aristas de espera (hora t -> t+1 en el mismo aeropuerto)
 * - Aristas de vuelo: t(departure) -> t+dur(arrival) si hay FlightInstance
 *
 * Para enrutamiento, delegamos a PathFinder (Dijkstra creciente en tiempo).
 */
public class TEGraph {
    public static class Node {
        public final Airport airport;
        public final int hour; // 0..167
        public Node(Airport airport, int hour) { this.airport = airport; this.hour = hour; }
        @Override public String toString() { return airport.code() + "@" + hour; }
    }

    /** Representa una arista con costo en horas y opción de "capacidad" si es vuelo. */
    public static class Edge {
        public final Node from;
        public final Node to;
        public final boolean isFlight;
        public final FlightInstance instance; // null si es espera
        public Edge(Node from, Node to, boolean isFlight, FlightInstance instance) {
            this.from = from; this.to = to; this.isFlight = isFlight; this.instance = instance;
        }
    }

    private final Map<String, Airport> airportsByCode;
    // indexación: airportCode -> lista de nodos por hora
    private final Map<String, Node[]> nodesByAirport = new HashMap<>();
    // adjacency: Node -> edges
    private final Map<Node, List<Edge>> adj = new HashMap<>();
    // catálogo de instancias de vuelo: para aplicar reservas
    private final List<FlightInstance> allInstances = new ArrayList<>();

    public TEGraph(Collection<Airport> airports) {
        this.airportsByCode = new HashMap<>();
        for (Airport a: airports) airportsByCode.put(a.code(), a);
        buildNodes();
        buildWaitEdges();
    }

    private void buildNodes() {
        for (Airport a: airportsByCode.values()) {
            Node[] arr = new Node[168];
            for (int h = 0; h < 168; h++) {
                arr[h] = new Node(a, h);
                adj.put(arr[h], new ArrayList<>());
            }
            nodesByAirport.put(a.code(), arr);
        }
    }

    private void buildWaitEdges() {
        for (Node[] arr: nodesByAirport.values()) {
            for (int h = 0; h < 167; h++) {
                Node u = arr[h];
                Node v = arr[h+1];
                adj.get(u).add(new Edge(u, v, false, null)); // esperar 1h
            }
        }
    }

    /** Agrega instancias de un vuelo recurrente según frecuencia por día. */
    public void addFlightWeekly(Flight flight) {
        int perDay = flight.frequencyPerDay();
        int step = 24 / Math.max(1, perDay); // ej: 2 vuelos -> cada 12h
        int durH = (int) flight.duration().toHours();

        Node[] originNodes = nodesByAirport.get(flight.origin().code());
        Node[] destNodes = nodesByAirport.get(flight.destination().code());

        for (int day = 0; day < 7; day++) {
            for (int k = 0; k < perDay; k++) {
                int depHour = day*24 + k*step;
                int arrHour = depHour + durH;
                if (arrHour >= 168) continue; // no cruzamos semana en este modelo

                FlightInstance inst = new FlightInstance(flight, depHour, arrHour);
                allInstances.add(inst);

                Node u = originNodes[depHour];
                Node v = destNodes[arrHour];
                adj.get(u).add(new Edge(u, v, true, inst));
            }
        }
    }

    public Node node(String airportCode, int hour) { return nodesByAirport.get(airportCode)[hour]; }
    public List<Edge> edges(Node node) { return adj.getOrDefault(node, List.of()); }
    public Collection<Node[]> nodesByAirport() { return nodesByAirport.values(); }
    public List<FlightInstance> allInstances() { return allInstances; }
}
