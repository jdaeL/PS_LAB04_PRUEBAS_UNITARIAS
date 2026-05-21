package com.lab04.propuestos.ej2_compras;

import com.lab04.propuestos.ej1_inventario.Inventario;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("Pruebas de CarritoCompra con Inventario mock")
class CarritoCompraInventarioTest {

    private ServicioPrecio servicioPrecio;
    private Inventario inventarioMock;
    private CarritoCompra carrito;
    private Producto producto;

    @BeforeEach
    void setUp() {
        servicioPrecio = mock(ServicioPrecio.class);
        inventarioMock = mock(Inventario.class);
        carrito = new CarritoCompra(servicioPrecio, inventarioMock);
        producto = new Producto("P001", "Laptop", 1000.0, true);

        when(servicioPrecio.calcularDescuento(anyDouble())).thenReturn(0.0);
        when(servicioPrecio.calcularImpuesto(anyDouble())).thenReturn(0.0);
    }

    @Test
    @DisplayName("finalizarCompra llama salidaStock por cada item")
    void finalizarCompra_LlamaSalidaStockPorCadaItem() {
        when(inventarioMock.verificarStock("P001", 2)).thenReturn(true);
        carrito.agregarProducto(producto, 2);
        carrito.finalizarCompra();
        verify(inventarioMock).salidaStock("P001", 2, "Venta por carrito");
    }

    @Test
    @DisplayName("finalizarCompra vacía el carrito después de ejecutarse")
    void finalizarCompra_VaciaElCarrito() {
        when(inventarioMock.verificarStock("P001", 1)).thenReturn(true);
        carrito.agregarProducto(producto, 1);
        carrito.finalizarCompra();
        assertTrue(carrito.getItems().isEmpty());
    }

    @Test
    @DisplayName("finalizarCompra registra operación en historial")
    void finalizarCompra_RegistraEnHistorial() {
        when(inventarioMock.verificarStock("P001", 1)).thenReturn(true);
        carrito.agregarProducto(producto, 1);
        carrito.finalizarCompra();
        assertTrue(carrito.getHistorialOperaciones().stream()
                .anyMatch(h -> h.contains("Compra finalizada")));
    }

    @Test
    @DisplayName("agregarProducto lanza excepción si stock insuficiente en inventario")
    void agregarProducto_StockInsuficiente_LanzaExcepcion() {
        when(inventarioMock.verificarStock("P001", 5)).thenReturn(false);
        when(inventarioMock.consultarStock("P001")).thenReturn(2);
        assertThrows(IllegalStateException.class,
                () -> carrito.agregarProducto(producto, 5));
    }

    @Test
    @DisplayName("agregarProducto permite cantidad dentro del stock disponible")
    void agregarProducto_DentroDeStock_Exitoso() {
        when(inventarioMock.verificarStock("P001", 2)).thenReturn(true);
        carrito.agregarProducto(producto, 2);
        assertEquals(1, carrito.getItems().size());
        assertEquals(2, carrito.getItems().get(0).getCantidad());
    }
}