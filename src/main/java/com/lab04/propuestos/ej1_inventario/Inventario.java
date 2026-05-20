package com.lab04.propuestos.ej1_inventario;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class Inventario {
    private final Map<String, Producto> productos = new ConcurrentHashMap<>();
    private final List<Movimiento> movimientos = new ArrayList<>();

    // Agregar un nuevo producto (stock inicial se asigna en el constructor)
    public void agregarProducto(Producto producto) {
        if (productos.containsKey(producto.getId()))
            throw new IllegalArgumentException("Ya existe un producto con ID: " + producto.getId());
        productos.put(producto.getId(), producto);
        registrarMovimiento(Movimiento.Tipo.ENTRADA, producto.getStock(), producto.getId(), "Alta inicial");
    }

    // Aumentar stock de un producto existente (entrada)
    public void entradaStock(String productoId, int cantidad, String motivo) {
        Producto p = obtenerProducto(productoId);
        p.agregarStock(cantidad);
        registrarMovimiento(Movimiento.Tipo.ENTRADA, cantidad, productoId, motivo);
    }

    // Disminuir stock (salida)
    public void salidaStock(String productoId, int cantidad, String motivo) {
        Producto p = obtenerProducto(productoId);
        p.extraerStock(cantidad);
        registrarMovimiento(Movimiento.Tipo.SALIDA, cantidad, productoId, motivo);
    }

    // Consultar stock actual
    public int consultarStock(String productoId) {
        return obtenerProducto(productoId).consultarStock();
    }

    // Obtener producto por ID
    public Producto obtenerProducto(String productoId) {
        Producto p = productos.get(productoId);
        if (p == null) throw new IllegalArgumentException("Producto no encontrado: " + productoId);
        return p;
    }

    // Listar todos los productos
    public List<Producto> listarProductos() {
        return new ArrayList<>(productos.values());
    }

    // Verificar si hay suficiente stock para una cantidad
    public boolean verificarStock(String productoId, int cantidad) {
        Producto p = obtenerProducto(productoId);
        return p.consultarStock() >= cantidad;
    }

    // Obtener historial de movimientos
    public List<Movimiento> getMovimientos() {
        return Collections.unmodifiableList(movimientos);
    }

    private void registrarMovimiento(Movimiento.Tipo tipo, int cantidad, String productoId, String motivo) {
        movimientos.add(new Movimiento(tipo, cantidad, productoId, motivo));
    }
}