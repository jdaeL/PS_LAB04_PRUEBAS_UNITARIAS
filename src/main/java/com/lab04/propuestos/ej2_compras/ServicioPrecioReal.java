package com.lab04.propuestos.ej2_compras;

public class ServicioPrecioReal implements ServicioPrecio {
    @Override
    public double calcularDescuento(double total) {
        return total > 500 ? total * 0.10 : 0;
    }
    @Override
    public double calcularImpuesto(double total) {
        return total * 0.19;
    }
}