package com.lab04.propuestos.ej2_compras;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@DisplayName("Pruebas de Producto e ItemCarrito")
class ProductoItemCarritoTest {

    private Producto producto;

    @BeforeEach
    void setUp() {
        producto = new Producto("P900", "Producto Base", 10.0, true);
    }

    @Test
    @DisplayName("Producto con ID vacío lanza excepción")
    void productoIdVacio_LanzaExcepcion() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> new Producto(" ", "Nombre", 10.0, true));
        assertEquals("ID no puede estar vacío", ex.getMessage());
    }

    @Test
    @DisplayName("Producto con precio no positivo lanza excepción")
    void productoPrecioNoPositivo_LanzaExcepcion() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> new Producto("P01", "Nombre", 0.0, true));
        assertEquals("El precio debe ser positivo", ex.getMessage());
    }

    @Test
    @DisplayName("Disponibilidad del producto puede actualizarse")
    void productoSetDisponible_ActualizaEstado() {
        producto.setDisponible(false);
        assertFalse(producto.isDisponible());
    }

    @Test
    @DisplayName("ItemCarrito calcula subtotal correctamente")
    void itemCarrito_SubtotalCorrecto() {
        ItemCarrito item = new ItemCarrito(producto, 3);
        assertEquals(30.0, item.getSubtotal());
    }

    @ParameterizedTest
    @CsvSource({"0", "-2"})
    @DisplayName("ItemCarrito no acepta cantidades inválidas")
    void itemCarrito_CantidadInvalida_LanzaExcepcion(int cantidad) {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> new ItemCarrito(producto, cantidad));
        assertEquals("Cantidad positiva", ex.getMessage());
    }

    @Test
    @DisplayName("ItemCarrito no acepta producto nulo")
    void itemCarrito_ProductoNulo_LanzaExcepcion() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> new ItemCarrito(null, 1));
        assertEquals("Producto nulo", ex.getMessage());
    }

    @Test
    @DisplayName("Actualizar cantidad inválida en ItemCarrito lanza excepción")
    void itemCarrito_ActualizarCantidadInvalida_LanzaExcepcion() {
        ItemCarrito item = new ItemCarrito(producto, 1);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> item.setCantidad(0));
        assertEquals("Cantidad positiva", ex.getMessage());
    }

    @Test
    @DisplayName("Producto disponible inicial es verdadero")
    void productoDisponible_TruePorDefecto() {
        assertTrue(producto.isDisponible());
    }

    @Test
    @DisplayName("toString de ItemCarrito contiene nombre, cantidad y subtotal")
    void itemCarritoToString_ContieneDatos() {
        ItemCarrito item = new ItemCarrito(producto, 3);
        String resultado = item.toString();
        assertTrue(resultado.contains("Producto Base"));
        assertTrue(resultado.contains("3"));
        assertTrue(resultado.contains("30"));
    }

    @Test
    @DisplayName("ItemCarrito actualizar cantidad válida funciona")
    void itemCarrito_ActualizarCantidadValida_Exitoso() {
        ItemCarrito item = new ItemCarrito(producto, 2);
        item.setCantidad(5);
        assertEquals(5, item.getCantidad());
        assertEquals(50.0, item.getSubtotal()); // recalcula subtotal
    }

    @Test
    @DisplayName("Producto equals compara por ID")
    void productoEquals_MismoId_Iguales() {
        Producto otro = new Producto("P900", "Otro nombre", 99.0, false);
        assertEquals(producto, otro);
    }

    @Test
    @DisplayName("Producto equals con null es false")
    void productoEquals_ConNull_EsFalse() {
        assertFalse(producto.equals(null));
    }

    @Test
    @DisplayName("Producto equals con otro tipo es false")
    void productoEquals_OtroTipo_EsFalse() {
        assertFalse(producto.equals("P900"));
    }

    @Test
    @DisplayName("Producto con precio negativo lanza excepción")
    void productoPrecioNegativo_LanzaExcepcion() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> new Producto("P01", "Nombre", -5.0, true));
        assertEquals("El precio debe ser positivo", ex.getMessage());
    }

    @Test
    @DisplayName("toString de Producto contiene ID, nombre y precio")
    void productoToString_ContieneDatos() {
        String s = producto.toString();
        assertTrue(s.contains("P900"));
        assertTrue(s.contains("Producto Base"));
    }
    
}
