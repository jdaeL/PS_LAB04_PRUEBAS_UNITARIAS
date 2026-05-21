package com.lab04.propuestos.ej1_inventario;

import java.util.Objects;

public class Producto {
    private final String codigo;
    private final String nombre;
    private final double precio;
    private int stock;

    public Producto(String codigo, String nombre, double precio, int stockInicial) {
        if (codigo == null || codigo.isBlank())
            throw new IllegalArgumentException("Código no puede estar vacío");
        if (nombre == null || nombre.isBlank())
            throw new IllegalArgumentException("Nombre no puede estar vacío");
        if (precio <= 0)
            throw new IllegalArgumentException("El precio debe ser positivo");
        if (stockInicial < 0)
            throw new IllegalArgumentException("El stock inicial no puede ser negativo");
        
        // Sanitización de datos de entrada
        // Quitamos espacios extra y forzamos el código a mayúsculas para evitar duplicados accidentales
        this.codigo = codigo.trim().toUpperCase();
        
        // Quitamos espacios extra al inicio y final del nombre
        this.nombre = nombre.trim();
        
        this.precio = precio;
        this.stock = stockInicial;
    }

    // Getters
    public String getCodigo() { return codigo; }
    public String getId() { return codigo; }
    public String getNombre() { return nombre; }
    public double getPrecio() { return precio; }
    public int getStock() { return stock; }
    public int getCantidad() { return stock; }

    public void agregarStock(int cantidad) {
        if (cantidad <= 0)
            throw new IllegalArgumentException("La cantidad a agregar debe ser positiva");
        this.stock += cantidad;
    }

    public void extraerStock(int cantidad) {
        if (cantidad <= 0)
            throw new IllegalArgumentException("La cantidad a extraer debe ser positiva");
        if (cantidad > this.stock)
            throw new IllegalStateException("Stock insuficiente. Disponible: " + this.stock);
        this.stock -= cantidad;
    }

    public int consultarStock() {
        return this.stock;
    }

    public double obtenerValorTotal() {
        return precio * stock;
    }

    public boolean isDisponible() {
        return stock > 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Producto)) return false;
        Producto producto = (Producto) o;
        return codigo.equals(producto.codigo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(codigo);
    }

    @Override
    public String toString() {
        return String.format("%s (%s) - $%.2f - Stock: %d", nombre, codigo, precio, stock);
    }
}