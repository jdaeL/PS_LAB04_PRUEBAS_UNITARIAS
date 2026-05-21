package com.lab04.propuestos.ej2_compras;

import com.lab04.propuestos.ej1_inventario.Inventario;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
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

    @Test
    @DisplayName("actualizarCantidad lanza excepción si nuevo valor excede stock")
    void actualizarCantidad_ExcedeStock_LanzaExcepcion() {
        when(inventarioMock.verificarStock("P001", 1)).thenReturn(true);
        carrito.agregarProducto(producto, 1);
        when(inventarioMock.verificarStock("P001", 99)).thenReturn(false);
        when(inventarioMock.consultarStock("P001")).thenReturn(5);
        assertThrows(IllegalStateException.class,
                () -> carrito.actualizarCantidad(producto, 99));
    }

    @Test
    @DisplayName("actualizarCantidad acepta valor válido dentro del stock")
    void actualizarCantidad_DentroDeStock_Exitoso() {
        when(inventarioMock.verificarStock("P001", 1)).thenReturn(true);
        carrito.agregarProducto(producto, 1);
        when(inventarioMock.verificarStock("P001", 3)).thenReturn(true);
        carrito.actualizarCantidad(producto, 3);
        assertEquals(3, carrito.getItems().get(0).getCantidad());
    }

    @Test
    @DisplayName("finalizarCompra con carrito vacío lanza IllegalStateException")
    void finalizarCompra_CarritoVacio_LanzaExcepcion() {
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> carrito.finalizarCompra());
        assertEquals("No hay productos en el carrito", ex.getMessage());
    }

    @Test
    @DisplayName("Producto con nombre vacío lanza excepción")
    void productoNombreVacio_LanzaExcepcion() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> new Producto("P01", " ", 10.0, true));
        assertEquals("Nombre no puede estar vacío", ex.getMessage());
    }

    @Test
    @DisplayName("Producto con nombre nulo lanza excepción")
    void productoNombreNulo_LanzaExcepcion() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> new Producto("P01", null, 10.0, true));
        assertEquals("Nombre no puede estar vacío", ex.getMessage());
    }

    @Test
    @DisplayName("Producto con ID nulo lanza excepción")
    void productoIdNulo_LanzaExcepcion() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> new Producto(null, "Nombre", 10.0, true));
        assertEquals("ID no puede estar vacío", ex.getMessage());
    }

    @Test
    @DisplayName("finalizarCompra con múltiples productos llama salidaStock para cada uno")
    void finalizarCompra_MultiplesProductos_LlamaSalidaStockParaCada() {
        Producto producto2 = new Producto("P002", "Mouse", 25.0, true);

        when(inventarioMock.verificarStock("P001", 2)).thenReturn(true);
        when(inventarioMock.verificarStock("P002", 3)).thenReturn(true);

        carrito.agregarProducto(producto, 2);
        carrito.agregarProducto(producto2, 3);
        carrito.finalizarCompra();

        verify(inventarioMock).salidaStock("P001", 2, "Venta por carrito");
        verify(inventarioMock).salidaStock("P002", 3, "Venta por carrito");
    }

    @Test
    @DisplayName("finalizarCompra NO llama salidaStock si carrito está vacío")
    void finalizarCompra_Vacio_NoLlamaSalidaStock() {
        assertThrows(IllegalStateException.class, () -> carrito.finalizarCompra());
        verify(inventarioMock, never()).salidaStock(any(), anyInt(), any());
    }

    @Test
    @DisplayName("agregarProducto con cantidad 0 lanza excepción sin consultar inventario")
    void agregarProducto_Cantidad0_LanzaExcepcionSinConsultarInventario() {
        assertThrows(IllegalArgumentException.class,
                () -> carrito.agregarProducto(producto, 0));
        verify(inventarioMock, never()).verificarStock(any(), anyInt());
    }

    @Test
    @DisplayName("removerProducto no interactúa con inventario")
    void removerProducto_NoInteractuaConInventario() {
        when(inventarioMock.verificarStock("P001", 1)).thenReturn(true);
        carrito.agregarProducto(producto, 1);
        carrito.removerProducto(producto);
        verify(inventarioMock, never()).salidaStock(any(), anyInt(), any());
    }

    @Test
    @DisplayName("vaciarCarrito no interactúa con inventario")
    void vaciarCarrito_NoInteractuaConInventario() {
        when(inventarioMock.verificarStock("P001", 1)).thenReturn(true);
        carrito.agregarProducto(producto, 1);
        carrito.vaciarCarrito();
        verify(inventarioMock, never()).salidaStock(any(), anyInt(), any());
    }
}