package com.morapack.mpa.domain;

/**
 * Orden de envío de un paquete MPE.
 * Para simplificar, cada orden tiene tamaño 1 unidad.
 */
public class PackageOrder {
    private final String id;
    private final Airport origin;
    private final Airport destination;
    private final int deadlineHour; // límite de llegada desde hora cero (0..167)
    private final int sizeUnits;

    public PackageOrder(String id, Airport origin, Airport destination, int deadlineHour) {
        this(id, origin, destination, deadlineHour, 1);
    }

    public PackageOrder(String id, Airport origin, Airport destination, int deadlineHour, int sizeUnits) {
        this.id = id;
        this.origin = origin;
        this.destination = destination;
        this.deadlineHour = deadlineHour;
        this.sizeUnits = sizeUnits;
    }

    public String id() { return id; }
    public Airport origin() { return origin; }
    public Airport destination() { return destination; }
    public int deadlineHour() { return deadlineHour; }
    public int sizeUnits() { return sizeUnits; }
}