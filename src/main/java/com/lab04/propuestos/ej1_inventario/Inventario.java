package com.lab04.propuestos.ej1_inventario;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class Inventario {
    private final Map<String, Producto> productos = new ConcurrentHashMap<>();
    
    // uso de CopyOnWriteArrayList para que toda la clase sea Thread-Safe
    private final List<Movimiento> movimientos = new CopyOnWriteArrayList<>();

    // método privado para estandarizar IDs en las búsquedas
    private String sanitizarId(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("ID de producto no puede estar vacío o ser nulo");
        }
        return id.trim().toUpperCase();
    }

    public void agregarProducto(Producto producto) {
        if (producto == null) {
            throw new IllegalArgumentException("Producto no puede ser nulo");
        }
        
        String idSanitizado = sanitizarId(producto.getId());
        
        if (productos.containsKey(idSanitizado))
            throw new IllegalArgumentException("Ya existe un producto con ID: " + idSanitizado);
            
        productos.put(idSanitizado, producto);
        
        if (producto.getStock() > 0) {
            registrarMovimiento(Movimiento.Tipo.ENTRADA, producto.getStock(), idSanitizado, "Alta inicial");
        }
    }

    public void eliminarProducto(String productoId) {
        String idSanitizado = sanitizarId(productoId);
        
        if (!productos.containsKey(idSanitizado)) {
            throw new IllegalArgumentException("Producto no encontrado: " + idSanitizado);
        }
        productos.remove(idSanitizado);
    }

    public void entradaStock(String productoId, int cantidad, String motivo) {
        String idSanitizado = sanitizarId(productoId);
        Producto p = obtenerProducto(idSanitizado);
        p.agregarStock(cantidad);
        registrarMovimiento(Movimiento.Tipo.ENTRADA, cantidad, idSanitizado, motivo);
    }

    public void salidaStock(String productoId, int cantidad, String motivo) {
        String idSanitizado = sanitizarId(productoId);
        Producto p = obtenerProducto(idSanitizado);
        p.extraerStock(cantidad);
        registrarMovimiento(Movimiento.Tipo.SALIDA, cantidad, idSanitizado, motivo);
    }

    public int consultarStock(String productoId) {
        return obtenerProducto(productoId).consultarStock();
    }

    public Producto obtenerProducto(String productoId) {
        String idSanitizado = sanitizarId(productoId);
        Producto p = productos.get(idSanitizado);
        if (p == null) throw new IllegalArgumentException("Producto no encontrado: " + idSanitizado);
        return p;
    }

    public List<Producto> listarProductos() {
        return new ArrayList<>(productos.values());
    }

    public boolean verificarStock(String productoId, int cantidad) {
        Producto p = obtenerProducto(productoId);
        return p.consultarStock() >= cantidad;
    }

    public List<Movimiento> getMovimientos() {
        return Collections.unmodifiableList(movimientos);
    }

    private void registrarMovimiento(Movimiento.Tipo tipo, int cantidad, String productoId, String motivo) {
        movimientos.add(new Movimiento(tipo, cantidad, productoId, motivo));
    }

    public List<Producto> buscarProductoPorNombre(String nombre) {
        if (nombre == null || nombre.isBlank())
            throw new IllegalArgumentException("El nombre de búsqueda no puede estar vacío");
            
        // trim explícito para mejorar las búsquedas accidentales con espacios
        String busqueda = nombre.trim().toLowerCase();
        
        return productos.values().stream()
                .filter(p -> p.getNombre().toLowerCase().contains(busqueda))
                .collect(Collectors.toList());
    }

    public int getTotalEntradas() {
        return movimientos.stream()
                .filter(m -> m.getTipo() == Movimiento.Tipo.ENTRADA)
                .mapToInt(Movimiento::getCantidad)
                .sum();
    }

    public int getTotalSalidas() {
        return movimientos.stream()
                .filter(m -> m.getTipo() == Movimiento.Tipo.SALIDA)
                .mapToInt(Movimiento::getCantidad)
                .sum();
    }
}