package com.lab04.propuestos.ej2_compras;

import com.lab04.propuestos.ej1_inventario.Inventario;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyDouble;
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
    @DisplayName("ItemCarrito actualizar cantidad válida funciona")
    void itemCarrito_ActualizarCantidadValida_Exitoso() {
        ItemCarrito item = new ItemCarrito(producto, 2);
        item.setCantidad(5);
        assertEquals(5, item.getCantidad());
        assertEquals(5000.0, item.getSubtotal()); // 5 unidades * $1000.0 = $5000.0
    }

    @Test
    @DisplayName("Producto equals compara por ID")
    void productoEquals_MismoId_Iguales() {
        Producto otro = new Producto("P001", "Otro nombre", 99.0, false);
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
        assertTrue(s.contains("P001"));
        assertTrue(s.contains("Laptop"));
    }
}