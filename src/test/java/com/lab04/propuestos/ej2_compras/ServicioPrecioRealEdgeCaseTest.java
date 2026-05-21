package com.lab04.propuestos.ej2_compras;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Casos límite de ServicioPrecioReal")
class ServicioPrecioRealEdgeCaseTest {

    private ServicioPrecioReal servicio;

    @BeforeEach
    void setUp() {
        servicio = new ServicioPrecioReal();
    }

    @Test
    @DisplayName("Descuento con total exactamente 500 NO aplica")
    void descuento_Total500Exacto_NoAplica() {
        assertEquals(0.0, servicio.calcularDescuento(500.0), 0.001);
    }

    @Test
    @DisplayName("Descuento con total 500.01 SÍ aplica")
    void descuento_Total500punto01_SiAplica() {
        assertTrue(servicio.calcularDescuento(500.01) > 0);
    }

    @Test
    @DisplayName("Impuesto con total 0 es 0")
    void impuesto_Total0_EsCero() {
        assertEquals(0.0, servicio.calcularImpuesto(0.0), 0.001);
    }

    @Test
    @DisplayName("Descuento con total negativo lanza excepción o devuelve 0")
    void descuento_TotalNegativo_ComportamientoDefinido() {
        try {
            double resultado = servicio.calcularDescuento(-100.0);
            assertEquals(0.0, resultado, 0.001);
        } catch (IllegalArgumentException e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    @DisplayName("Impuesto con total negativo lanza excepción o devuelve 0")
    void impuesto_TotalNegativo_ComportamientoDefinido() {
        try {
            double resultado = servicio.calcularImpuesto(-100.0);
            assertEquals(0.0, resultado, 0.001);
        } catch (IllegalArgumentException e) {
            assertNotNull(e.getMessage());
        }
    }
}