package com.lab04.propuestos.ej2_compras;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class CarritoCompra {
    private final List<ItemCarrito> items;
    private final ServicioPrecio servicioPrecio;
    private final List<String> historialOperaciones;

    public CarritoCompra(ServicioPrecio servicioPrecio) {
        this.servicioPrecio = Objects.requireNonNull(servicioPrecio, "ServicioPrecio no puede ser nulo");
        this.items = new ArrayList<>();
        this.historialOperaciones = new ArrayList<>();
        registrarOperacion("Carrito creado");
    }

    public void agregarProducto(Producto producto, int cantidad) {
        if (producto == null)
            throw new IllegalArgumentException("Producto no puede ser nulo");
        if (!producto.isDisponible())
            throw new IllegalStateException("Producto no disponible: " + producto.getNombre());
        if (cantidad <= 0)
            throw new IllegalArgumentException("La cantidad debe ser positiva");

        Optional<ItemCarrito> existente = buscarItem(producto);
        if (existente.isPresent()) {
            ItemCarrito item = existente.get();
            item.setCantidad(item.getCantidad() + cantidad);
            registrarOperacion(String.format("Cantidad aumentada: %s +%d (nueva: %d)",
                    producto.getNombre(), cantidad, item.getCantidad()));
        } else {
            items.add(new ItemCarrito(producto, cantidad));
            registrarOperacion(String.format("Producto agregado: %s x%d", producto.getNombre(), cantidad));
        }
    }

    public void actualizarCantidad(Producto producto, int nuevaCantidad) {
        if (producto == null) throw new IllegalArgumentException("Producto nulo");
        if (nuevaCantidad <= 0)
            throw new IllegalArgumentException("La nueva cantidad debe ser positiva");

        ItemCarrito item = buscarItem(producto)
                .orElseThrow(() -> new IllegalArgumentException("Producto no está en el carrito"));
        int viejaCantidad = item.getCantidad();
        item.setCantidad(nuevaCantidad);
        registrarOperacion(String.format("Cantidad actualizada: %s %d -> %d",
                producto.getNombre(), viejaCantidad, nuevaCantidad));
    }

    public void removerProducto(Producto producto) {
        if (producto == null) throw new IllegalArgumentException("Producto nulo");
        boolean removido = items.removeIf(item -> item.getProducto().equals(producto));
        if (removido) {
            registrarOperacion("Producto removido: " + producto.getNombre());
        } else {
            throw new IllegalArgumentException("Producto no encontrado en el carrito");
        }
    }

    public void vaciarCarrito() {
        items.clear();
        registrarOperacion("Carrito vaciado");
    }

    public double calcularTotal() {
        double subtotal = items.stream().mapToDouble(ItemCarrito::getSubtotal).sum();
        double descuento = servicioPrecio.calcularDescuento(subtotal);
        double conDescuento = subtotal - descuento;
        double impuesto = servicioPrecio.calcularImpuesto(conDescuento);
        double total = conDescuento + impuesto;
        registrarOperacion(String.format("Total calculado: subtotal=%.2f, descuento=%.2f, impuesto=%.2f, total=%.2f",
                subtotal, descuento, impuesto, total));
        return total;
    }

    public String obtenerResumenCompra() {
        StringBuilder sb = new StringBuilder("=== Resumen del carrito ===\n");
        for (ItemCarrito item : items) {
            sb.append(String.format("%s x%d = %.2f\n",
                    item.getProducto().getNombre(),
                    item.getCantidad(),
                    item.getSubtotal()));
        }
        double total = calcularTotal();
        sb.append(String.format("TOTAL: %.2f", total));
        return sb.toString();
    }

    public List<String> getHistorialOperaciones() {
        return Collections.unmodifiableList(historialOperaciones);
    }

    public List<ItemCarrito> getItems() {
        return Collections.unmodifiableList(items);
    }

    private Optional<ItemCarrito> buscarItem(Producto producto) {
        return items.stream().filter(i -> i.getProducto().equals(producto)).findFirst();
    }

    private void registrarOperacion(String operacion) {
        historialOperaciones.add(LocalDateTime.now() + " - " + operacion);
    }
}