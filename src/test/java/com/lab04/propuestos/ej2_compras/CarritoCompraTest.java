package com.lab04.propuestos.ej2_compras;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Pruebas del Carrito de Compras")
class CarritoCompraTest {

    private ServicioPrecio servicioPrecioMock;
    private ServicioPrecio servicioPrecioSinImpuestos;
    private ServicioPrecioReal servicioPrecioReal;
    private CarritoCompra carrito;
    private Producto producto1, producto2, productoSinStock;

    @BeforeEach
    void setUpBase() {
        servicioPrecioMock = mock(ServicioPrecio.class);
        servicioPrecioSinImpuestos = new ServicioPrecio() {
            @Override
            public double calcularDescuento(double total) {
                return 0;
            }

            @Override
            public double calcularImpuesto(double total) {
                return 0;
            }
        };
        servicioPrecioReal = new ServicioPrecioReal();
        producto1 = new Producto("P001", "Laptop", 1000.0, true);
        producto2 = new Producto("P002", "Mouse", 25.0, true);
        productoSinStock = new Producto("P003", "Teclado", 50.0, false);
    }

    @Nested
    @DisplayName("Operaciones básicas del carrito (sin mocks)")
    class OperacionesBasicas {

        @BeforeEach
        void setUpCarrito() {
            carrito = new CarritoCompra(servicioPrecioSinImpuestos);
        }

        @Test
        @DisplayName("Agregar producto aumenta la lista de items")
        void agregarProducto_DeberiaIncrementarItems() {
            carrito.agregarProducto(producto1, 2);
            assertEquals(1, carrito.getItems().size());
            assertEquals(2, carrito.getItems().get(0).getCantidad());
        }

        @Test
        @DisplayName("Agregar producto no disponible lanza excepción")
        void agregarProductoNoDisponible_LanzaExcepcion() {
            assertThrows(IllegalStateException.class,
                    () -> carrito.agregarProducto(productoSinStock, 1));
        }

        @Test
        @DisplayName("Agregar cantidad negativa lanza excepción")
        void agregarCantidadNegativa_LanzaExcepcion() {
            assertThrows(IllegalArgumentException.class,
                    () -> carrito.agregarProducto(producto1, -5));
        }

        @Test
        @DisplayName("Agregar el mismo producto incrementa su cantidad (duplicado)")
        void agregarProductoDuplicado_IncrementaCantidad() {
            carrito.agregarProducto(producto1, 1);
            carrito.agregarProducto(producto1, 2);
            assertEquals(1, carrito.getItems().size());
            assertEquals(3, carrito.getItems().get(0).getCantidad());
        }

        @Test
        @DisplayName("Actualizar cantidad de producto existente")
        void actualizarCantidad_ModificaCorrectamente() {
            carrito.agregarProducto(producto1, 2);
            carrito.actualizarCantidad(producto1, 5);
            assertEquals(5, carrito.getItems().get(0).getCantidad());
        }

        @Test
        @DisplayName("Actualizar cantidad de producto no existente lanza excepción")
        void actualizarCantidadProductoNoExistente_LanzaExcepcion() {
            assertThrows(IllegalArgumentException.class,
                    () -> carrito.actualizarCantidad(producto1, 3));
        }

        @Test
        @DisplayName("Remover producto existente")
        void removerProducto_EliminaItem() {
            carrito.agregarProducto(producto1, 1);
            carrito.agregarProducto(producto2, 1);
            carrito.removerProducto(producto1);
            assertEquals(1, carrito.getItems().size());
            assertEquals(producto2, carrito.getItems().get(0).getProducto());
        }

        @Test
        @DisplayName("Remover producto inexistente lanza excepción")
        void removerProductoNoExistente_LanzaExcepcion() {
            assertThrows(IllegalArgumentException.class,
                    () -> carrito.removerProducto(producto1));
        }

        @Test
        @DisplayName("Vaciar carrito elimina todos los items")
        void vaciarCarrito_DejaCarritoVacio() {
            carrito.agregarProducto(producto1, 1);
            carrito.agregarProducto(producto2, 2);
            carrito.vaciarCarrito();
            assertTrue(carrito.getItems().isEmpty());
        }

        @Test
        @DisplayName("Obtener resumen del carrito (formato esperado)")
        void obtenerResumenCompra_NoNulo() {
            carrito.agregarProducto(producto1, 2); // subtotal 2000
            String resumen = carrito.obtenerResumenCompra();
            
            System.out.println("DEBUG RESUMEN: " + resumen);
            assertTrue(resumen.contains("Laptop x2 = 2000,00"));
            assertTrue(resumen.contains("TOTAL: 2000,00"));
        }

        @Test
        @DisplayName("Historial de operaciones registra acciones")
        void historial_RegistraOperaciones() {
            carrito.agregarProducto(producto1, 1);
            carrito.actualizarCantidad(producto1, 3);
            carrito.removerProducto(producto1);
            List<String> historial = carrito.getHistorialOperaciones();
            assertTrue(historial.stream().anyMatch(h -> h.contains("Producto agregado: Laptop")));
            assertTrue(historial.stream().anyMatch(h -> h.contains("Cantidad actualizada")));
            assertTrue(historial.stream().anyMatch(h -> h.contains("Producto removido")));
        }

        @Test
        @DisplayName("Resumen con múltiples productos incluye todos los nombres")
        void resumenCompra_ConVariosProductos_ContieneNombres() {
            carrito.agregarProducto(producto1, 1);
            carrito.agregarProducto(producto2, 4);
            String resumen = carrito.obtenerResumenCompra();
            assertTrue(resumen.contains("Laptop"));
            assertTrue(resumen.contains("Mouse"));
            assertTrue(resumen.contains("TOTAL"));
        }

        @Test
        @DisplayName("Resumen con carrito vacío solo muestra encabezado y total cero")
        void resumenCompra_CarritoVacio_MuestraEncabezado() {
            String resumen = carrito.obtenerResumenCompra();
            assertTrue(resumen.contains("Resumen"));
            assertTrue(resumen.contains("0,00") || resumen.contains("0.00"));
        }
    }

    @Nested
    @DisplayName("Cálculos de total con Mock de ServicioPrecio")
    class CalculosConMock {

        @BeforeEach
        void setUpCarrito() {
            carrito = new CarritoCompra(servicioPrecioMock);
        }

        @Test
        @DisplayName("Carrito vacío total = 0")
        void carritoVacio_TotalCero() {
            when(servicioPrecioMock.calcularDescuento(0.0)).thenReturn(0.0);
            when(servicioPrecioMock.calcularImpuesto(0.0)).thenReturn(0.0);
            assertEquals(0.0, carrito.calcularTotal());
        }

        @Test
        @DisplayName("Total sin descuento ni impuesto")
        void totalSinDescuentoNiImpuesto() {
            carrito.agregarProducto(producto1, 1); // 1000
            when(servicioPrecioMock.calcularDescuento(1000)).thenReturn(0.0);
            when(servicioPrecioMock.calcularImpuesto(1000)).thenReturn(0.0);
            assertEquals(1000.0, carrito.calcularTotal());
        }

        @Test
        @DisplayName("Total con descuento pero sin impuesto")
        void totalConDescuentoSinImpuesto() {
            carrito.agregarProducto(producto1, 1); // 1000
            when(servicioPrecioMock.calcularDescuento(1000)).thenReturn(100.0);
            when(servicioPrecioMock.calcularImpuesto(900)).thenReturn(0.0);
            assertEquals(900.0, carrito.calcularTotal());
        }

        @Test
        @DisplayName("Total con impuesto pero sin descuento")
        void totalConImpuestoSinDescuento() {
            carrito.agregarProducto(producto2, 4); // 100
            when(servicioPrecioMock.calcularDescuento(100)).thenReturn(0.0);
            when(servicioPrecioMock.calcularImpuesto(100)).thenReturn(19.0);
            assertEquals(119.0, carrito.calcularTotal());
        }

        @Test
        @DisplayName("Total con descuento e impuesto")
        void totalConDescuentoEImpuesto() {
            carrito.agregarProducto(producto1, 1); // 1000
            when(servicioPrecioMock.calcularDescuento(1000)).thenReturn(150.0);
            when(servicioPrecioMock.calcularImpuesto(850)).thenReturn(85.0);
            assertEquals(935.0, carrito.calcularTotal());
        }

        @ParameterizedTest
        @CsvSource({
                "1, 1000, 0, 0, 1000",
                "2, 2000, 0, 0, 2000",
                "1, 1000, 100, 50, 950",
                "3, 75, 0, 15, 90"
        })
        @DisplayName("Pruebas parametrizadas de total con diferentes montos")
        void totalParametrizado(int cantidad, double precioEsperadoSubtotal,
                                double descuentoMock, double impuestoMock,
                                double totalEsperado) {
            Producto p = new Producto("P100", "ProductoTest", precioEsperadoSubtotal / cantidad, true);
            carrito.agregarProducto(p, cantidad);
            when(servicioPrecioMock.calcularDescuento(precioEsperadoSubtotal)).thenReturn(descuentoMock);
            when(servicioPrecioMock.calcularImpuesto(precioEsperadoSubtotal - descuentoMock)).thenReturn(impuestoMock);
            assertEquals(totalEsperado, carrito.calcularTotal());
        }

        @Test
        @DisplayName("Verificar que ServicioPrecio es invocado correctamente")
        void verificarLlamadasAServicioPrecio() {
            carrito.agregarProducto(producto1, 1);
            carrito.calcularTotal();
            verify(servicioPrecioMock).calcularDescuento(1000.0);
            verify(servicioPrecioMock).calcularImpuesto(anyDouble());
        }
    }

    @Nested
    @DisplayName("Casos límite")
    class CasosLimite {

        @BeforeEach
        void setUpCarrito() {
            carrito = new CarritoCompra(servicioPrecioSinImpuestos);
        }

        @Test
        @DisplayName("Carrito con 1 producto")
        void unSoloProducto() {
            carrito.agregarProducto(producto1, 1);
            assertEquals(1000.0, carrito.calcularTotal());
            assertEquals(1, carrito.getItems().size());
        }

        @Test
        @DisplayName("Carrito con 100 productos distintos")
        void cienProductos() {
            for (int i = 0; i < 100; i++) {
                Producto p = new Producto("P" + i, "Producto" + i, 0.50, true);
                carrito.agregarProducto(p, 1);
            }
            assertEquals(50.0, carrito.calcularTotal());
            assertEquals(100, carrito.getItems().size());
        }

        @Test
        @DisplayName("Cantidad máxima en un solo ítem")
        void cantidadMuyGrande() {
            carrito.agregarProducto(producto1, 1000);
            assertEquals(1_000_000.0, carrito.calcularTotal());
            assertEquals(1000, carrito.getItems().get(0).getCantidad());
        }

        @Test
        @DisplayName("Actualizar cantidad después de agregar duplicados")
        void actualizarCantidadConDuplicados() {
            carrito.agregarProducto(producto1, 1);
            carrito.agregarProducto(producto1, 2); // ahora 3
            carrito.actualizarCantidad(producto1, 10);
            assertEquals(10, carrito.getItems().get(0).getCantidad());
        }
    }

    @Nested
    @DisplayName("ServicioPrecioReal")
    class ServicioPrecioRealTests {

        @Test
        @DisplayName("Descuento aplica 10% si total es mayor a 500")
        void descuento_SeAplicaSobreTotalMayorA500() {
            assertEquals(100.0, servicioPrecioReal.calcularDescuento(1000.0));
        }

        @Test
        @DisplayName("Impuesto aplica 19% sobre el total")
        void impuesto_SeCalculaSobreTotal() {
            assertEquals(19.0, servicioPrecioReal.calcularImpuesto(100.0));
        }
    }
}
