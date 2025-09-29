package com.morapack.mpa.scenario;

import com.morapack.mpa.domain.Airport;
import com.morapack.mpa.domain.Flight;
import com.morapack.mpa.domain.PackageOrder;
import com.morapack.mpa.graph.TEGraph;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Generador de un escenario semanal simplificado:
 * - Aeropuertos en AM/EU/AS
 * - Vuelos con duraciones y frecuencias según el caso (12h intra, 24h inter)
 * - Órdenes (paquetes) con origen/destino y plazo (48h intra, 72h inter).
 */
public class WeeklyScenario {
    public final List<Airport> airports;
    public final List<Flight> flights;
    public final TEGraph graph;

    public WeeklyScenario(List<Airport> airports, List<Flight> flights) {
        this.airports = airports;
        this.flights = flights;
        this.graph = new TEGraph(airports);
        flights.forEach(graph::addFlightWeekly);
    }

    public static WeeklyScenario demoNetwork() {
        // Sedes y algunas ciudades conectadas (ejemplo)
        Airport LIM = new Airport("LIM","Lima", Airport.Continent.AMERICAS, 900);
        Airport MIA = new Airport("MIA","Miami", Airport.Continent.AMERICAS, 800);
        Airport MEX = new Airport("MEX","Ciudad de México", Airport.Continent.AMERICAS, 900);

        Airport BRU = new Airport("BRU","Bruselas", Airport.Continent.EUROPE, 1000);
        Airport MAD = new Airport("MAD","Madrid", Airport.Continent.EUROPE, 900);
        Airport FRA = new Airport("FRA","Frankfurt", Airport.Continent.EUROPE, 1000);

        Airport GYD = new Airport("GYD","Baku", Airport.Continent.ASIA, 800);
        Airport DOH = new Airport("DOH","Doha", Airport.Continent.ASIA, 900);
        Airport DXB = new Airport("DXB","Dubai", Airport.Continent.ASIA, 900);

        List<Airport> aps = List.of(LIM,MIA,MEX,BRU,MAD,FRA,GYD,DOH,DXB);

        List<Flight> fs = new ArrayList<>();
        // Intra-continente (12h, 2-3 frecuencias)
        fs.add(new Flight("LIM-MIA", LIM, MIA, Duration.ofHours(12), 260, 2));
        fs.add(new Flight("MIA-MEX", MIA, MEX, Duration.ofHours(12), 280, 2));
        fs.add(new Flight("BRU-MAD", BRU, MAD, Duration.ofHours(12), 250, 3));
        fs.add(new Flight("MAD-FRA", MAD, FRA, Duration.ofHours(12), 230, 2));
        fs.add(new Flight("GYD-DOH", GYD, DOH, Duration.ofHours(12), 250, 2));
        fs.add(new Flight("DOH-DXB", DOH, DXB, Duration.ofHours(12), 260, 2));

        // Inter-continente (24h, >=1 frecuencia)
        fs.add(new Flight("LIM-BRU", LIM, BRU, Duration.ofHours(24), 350, 1));
        fs.add(new Flight("BRU-GYD", BRU, GYD, Duration.ofHours(24), 380, 1));
        fs.add(new Flight("MEX-MAD", MEX, MAD, Duration.ofHours(24), 320, 1));
        fs.add(new Flight("FRA-DOH", FRA, DOH, Duration.ofHours(24), 360, 1));

        return new WeeklyScenario(aps, fs);
    }

    /** Genera N órdenes pseudoaleatorias con plazos según intra (48h) o inter (72h). */
    public List<PackageOrder> generateOrders(int n, long seed) {
        Random rnd = new Random(seed);
        List<PackageOrder> orders = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            Airport o = airports.get(rnd.nextInt(airports.size()));
            Airport d = airports.get(rnd.nextInt(airports.size()));
            while (d == o) d = airports.get(rnd.nextInt(airports.size()));

            boolean sameCont = o.continent() == d.continent();
            int deadline = sameCont ? 48 : 72;
            orders.add(new PackageOrder("ORD-" + i, o, d, deadline));
        }
        return orders;
    }
}
