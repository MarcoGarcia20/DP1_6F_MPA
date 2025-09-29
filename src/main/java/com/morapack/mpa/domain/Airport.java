package com.morapack.mpa.domain;

/**
 * Aeropuerto único por ciudad (supuesto del caso).
 * Mantiene continente y capacidad de almacén (simplificado).
 */
public class Airport {
    public enum Continent { AMERICAS, EUROPE, ASIA }

    private final String code;         // e.g., LIM, BRU, GYD
    private final String city;
    private final Continent continent;
    private final int warehouseCapacity; // paquetes simultáneos

    public Airport(String code, String city, Continent continent, int warehouseCapacity) {
        this.code = code;
        this.city = city;
        this.continent = continent;
        this.warehouseCapacity = warehouseCapacity;
    }

    public String code() { return code; }
    public String city() { return city; }
    public Continent continent() { return continent; }
    public int warehouseCapacity() { return warehouseCapacity; }

    @Override public String toString() {
        return code + " (" + city + ", " + continent + ")";
    }
}