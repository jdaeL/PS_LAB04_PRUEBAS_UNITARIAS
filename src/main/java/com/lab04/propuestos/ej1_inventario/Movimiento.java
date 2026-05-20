package com.lab04.propuestos.ej1_inventario;

import java.time.LocalDateTime;

public class Movimiento {
    public enum Tipo { ENTRADA, SALIDA }
    private final Tipo tipo;
    private final int cantidad;
    private final LocalDateTime fecha;
    private final String productoId;
    private final String motivo;  // opcional, ej: "compra", "venta", "ajuste"

    public Movimiento(Tipo tipo, int cantidad, String productoId, String motivo) {
        if (tipo == null) {
            throw new IllegalArgumentException("Tipo de movimiento no puede ser nulo");
        }
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser positiva");
        }
        if (productoId == null || productoId.isBlank()) {
            throw new IllegalArgumentException("ProductoId no puede estar vacío");
        }
        this.tipo = tipo;
        this.cantidad = cantidad;
        this.productoId = productoId;
        this.motivo = motivo;
        this.fecha = LocalDateTime.now();
    }

    // Getters
    public Tipo getTipo() { return tipo; }
    public int getCantidad() { return cantidad; }
    public LocalDateTime getFecha() { return fecha; }
    public String getProductoId() { return productoId; }
    public String getMotivo() { return motivo; }

    @Override
    public String toString() {
        return String.format("%s - %s %d de %s (%s)", fecha, tipo, cantidad, productoId, motivo);
    }
}
