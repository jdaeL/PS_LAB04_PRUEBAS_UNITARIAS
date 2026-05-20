package com.lab04.propuestos.ej2_compras;

public class ItemCarrito {
    private final Producto producto;
    private int cantidad;

    public ItemCarrito(Producto producto, int cantidad) {
        if (producto == null)
            throw new IllegalArgumentException("Producto no puede ser nulo");
        if (cantidad <= 0)
            throw new IllegalArgumentException("La cantidad debe ser positiva");
        this.producto = producto;
        this.cantidad = cantidad;
    }

    public Producto getProducto() { return producto; }
    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) {
        if (cantidad <= 0)
            throw new IllegalArgumentException("La cantidad debe ser positiva");
        this.cantidad = cantidad;
    }
    public double getSubtotal() { return producto.getPrecio() * cantidad; }
}