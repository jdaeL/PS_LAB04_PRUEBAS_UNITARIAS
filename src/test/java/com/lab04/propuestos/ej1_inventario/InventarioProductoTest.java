package com.lab04.propuestos.ej1_inventario;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("Pruebas de Inventario y Producto")
class InventarioProductoTest {

    private Producto producto;
    private Inventario inventario;

    @BeforeEach
    void setUp() {
        inventario = new Inventario();
        producto = new Producto("P001", "Producto Base", 10.0, 5);
    }

    @Nested
    @DisplayName("Validaciones y operaciones de Producto")
    class ProductoTests {

        @Test
        @DisplayName("Constructor válido asigna atributos")
        void constructorValido_AsignaAtributos() {
            assertEquals("P001", producto.getCodigo());
            assertEquals("P001", producto.getId());
            assertEquals("Producto Base", producto.getNombre());
            assertEquals(10.0, producto.getPrecio());
            assertEquals(5, producto.getStock());
            assertEquals(5, producto.getCantidad());
            assertTrue(producto.isDisponible());
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" "})
        @DisplayName("Código inválido lanza excepción")
        void constructorCodigoInvalido_LanzaExcepcion(String codigo) {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> new Producto(codigo, "Nombre", 10.0, 1));
            assertEquals("Código no puede estar vacío", ex.getMessage());
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" "})
        @DisplayName("Nombre inválido lanza excepción")
        void constructorNombreInvalido_LanzaExcepcion(String nombre) {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> new Producto("P100", nombre, 10.0, 1));
            assertEquals("Nombre no puede estar vacío", ex.getMessage());
        }

        @ParameterizedTest
        @CsvSource({"0", "-1"})
        @DisplayName("Precio no positivo lanza excepción")
        void constructorPrecioNoPositivo_LanzaExcepcion(double precio) {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> new Producto("P100", "Nombre", precio, 1));
            assertEquals("El precio debe ser positivo", ex.getMessage());
        }

        @Test
        @DisplayName("Stock inicial negativo lanza excepción")
        void constructorStockNegativo_LanzaExcepcion() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> new Producto("P100", "Nombre", 10.0, -1));
            assertEquals("El stock inicial no puede ser negativo", ex.getMessage());
        }

        @Test
        @DisplayName("Agregar stock incrementa cantidad")
        void agregarStock_IncrementaCantidad() {
            producto.agregarStock(3);
            assertEquals(8, producto.getStock());
        }

        @ParameterizedTest
        @CsvSource({"0", "-2"})
        @DisplayName("Agregar stock inválido lanza excepción")
        void agregarStock_Invalido_LanzaExcepcion(int cantidad) {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> producto.agregarStock(cantidad));
            assertEquals("La cantidad a agregar debe ser positiva", ex.getMessage());
        }

        @Test
        @DisplayName("Extraer stock decrementa cantidad")
        void extraerStock_DecrementaCantidad() {
            producto.extraerStock(2);
            assertEquals(3, producto.getStock());
        }

        @ParameterizedTest
        @CsvSource({"0", "-1"})
        @DisplayName("Extraer stock inválido lanza excepción")
        void extraerStock_Invalido_LanzaExcepcion(int cantidad) {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> producto.extraerStock(cantidad));
            assertEquals("La cantidad a extraer debe ser positiva", ex.getMessage());
        }

        @Test
        @DisplayName("Extraer más stock del disponible lanza excepción")
        void extraerStock_Insuficiente_LanzaExcepcion() {
            IllegalStateException ex = assertThrows(IllegalStateException.class,
                    () -> producto.extraerStock(10));
            assertEquals("Stock insuficiente. Disponible: 5", ex.getMessage());
        }

        @Test
        @DisplayName("Obtener valor total usa precio por cantidad")
        void obtenerValorTotal_CalculaCorrectamente() {
            assertEquals(50.0, producto.obtenerValorTotal());
        }

        @Test
        @DisplayName("Producto sin stock no está disponible")
        void productoSinStock_NoDisponible() {
            Producto sinStock = new Producto("P002", "Sin stock", 5.0, 0);
            assertFalse(sinStock.isDisponible());
        }

        @Test
        @DisplayName("Equals y hashCode comparan por código")
        void equalsHashCode_ComparaPorCodigo() {
            Producto mismoId = new Producto("P001", "Otro", 9.0, 1);
            assertEquals(producto, mismoId);
            assertEquals(producto.hashCode(), mismoId.hashCode());
        }

        @Test
        @DisplayName("toString incluye datos básicos")
        void toString_ContieneDatos() {
            assertTrue(producto.toString().contains("Producto Base"));
            assertTrue(producto.toString().contains("P001"));
        }
    }

    @Nested
    @DisplayName("Operaciones de Inventario")
    class InventarioTests {

        @Test
        @DisplayName("Agregar producto registra movimiento de entrada")
        void agregarProducto_RegistraMovimiento() {
            inventario.agregarProducto(producto);
            List<Movimiento> movimientos = inventario.getMovimientos();
            assertEquals(1, movimientos.size());
            assertEquals(Movimiento.Tipo.ENTRADA, movimientos.get(0).getTipo());
            assertEquals(5, movimientos.get(0).getCantidad());
        }

        @Test
        @DisplayName("Agregar producto con stock cero no registra movimiento")
        void agregarProducto_StockCero_NoRegistraMovimiento() {
            Producto sinStock = new Producto("P002", "Sin stock", 5.0, 0);
            inventario.agregarProducto(sinStock);
            assertTrue(inventario.getMovimientos().isEmpty());
        }

        @Test
        @DisplayName("Agregar producto duplicado lanza excepción")
        void agregarProducto_Duplicado_LanzaExcepcion() {
            inventario.agregarProducto(producto);
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> inventario.agregarProducto(producto));
            assertEquals("Ya existe un producto con ID: P001", ex.getMessage());
        }

        @Test
        @DisplayName("Entrada de stock actualiza cantidad y registra movimiento")
        void entradaStock_ActualizaYRegistra() {
            inventario.agregarProducto(producto);
            inventario.entradaStock("P001", 2, "Compra");
            assertEquals(7, inventario.consultarStock("P001"));
            assertEquals(2, inventario.getMovimientos().size());
        }

        @Test
        @DisplayName("Salida de stock actualiza cantidad y registra movimiento")
        void salidaStock_ActualizaYRegistra() {
            inventario.agregarProducto(producto);
            inventario.salidaStock("P001", 1, "Venta");
            assertEquals(4, inventario.consultarStock("P001"));
            assertEquals(2, inventario.getMovimientos().size());
        }

        @Test
        @DisplayName("Obtener producto inexistente lanza excepción")
        void obtenerProducto_Inexistente_LanzaExcepcion() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> inventario.obtenerProducto("X"));
            assertEquals("Producto no encontrado: X", ex.getMessage());
        }

        @Test
        @DisplayName("Listar productos devuelve los agregados")
        void listarProductos_DevuelveAgregados() {
            inventario.agregarProducto(producto);
            assertEquals(1, inventario.listarProductos().size());
        }

        @Test
        @DisplayName("Verificar stock responde correctamente")
        void verificarStock_RespondeCorrectamente() {
            inventario.agregarProducto(producto);
            assertTrue(inventario.verificarStock("P001", 3));
            assertFalse(inventario.verificarStock("P001", 10));
        }

        @Test
        @DisplayName("Historial de movimientos es inmodificable")
        void movimientos_Inmodificables() {
            inventario.agregarProducto(producto);
            List<Movimiento> movimientos = inventario.getMovimientos();
            assertThrows(UnsupportedOperationException.class,
                    () -> movimientos.add(new Movimiento(Movimiento.Tipo.ENTRADA, 1, "P001", "Test")));
        }

        @Test
        @DisplayName("buscarProductoPorNombre devuelve coincidencias parciales")
        void buscarPorNombre_DevuelveCoincidencias() {
            inventario.agregarProducto(producto);
            inventario.agregarProducto(new Producto("P002", "Producto Extra", 5.0, 2));
            List<Producto> resultado = inventario.buscarProductoPorNombre("Producto");
            assertEquals(2, resultado.size());
        }

        @Test
        @DisplayName("buscarProductoPorNombre sin coincidencias devuelve lista vacía")
        void buscarPorNombre_SinCoincidencias_DevuelveVacio() {
            inventario.agregarProducto(producto);
            List<Producto> resultado = inventario.buscarProductoPorNombre("XYZ");
            assertTrue(resultado.isEmpty());
        }

        @Test
        @DisplayName("buscarProductoPorNombre con nombre vacío lanza excepción")
        void buscarPorNombre_NombreVacio_LanzaExcepcion() {
            assertThrows(IllegalArgumentException.class,
                    () -> inventario.buscarProductoPorNombre("  "));
        }

        @Test
        @DisplayName("buscarProductoPorNombre con null lanza excepción")
        void buscarPorNombre_Null_LanzaExcepcion() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> inventario.buscarProductoPorNombre(null));
            assertEquals("El nombre de búsqueda no puede estar vacío", ex.getMessage());
        }

        @Test
        @DisplayName("Agregar producto nulo lanza excepción")
        void agregarProducto_Nulo_LanzaExcepcion() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> inventario.agregarProducto(null));
            assertEquals("Producto no puede ser nulo", ex.getMessage());
        }

        @Test
        @DisplayName("getTotalEntradas suma todas las entradas registradas")
        void getTotalEntradas_SumaEntradas() {
            inventario.agregarProducto(producto); // entrada inicial 5
            inventario.entradaStock("P001", 3, "Reposición");
            assertEquals(8, inventario.getTotalEntradas());
        }

        @Test
        @DisplayName("getTotalSalidas suma todas las salidas registradas")
        void getTotalSalidas_SumaSalidas() {
            inventario.agregarProducto(producto);
            inventario.salidaStock("P001", 2, "Venta");
            inventario.salidaStock("P001", 1, "Venta");
            assertEquals(3, inventario.getTotalSalidas());
        }

        @Test
        @DisplayName("getTotalEntradas con inventario vacío devuelve 0")
        void getTotalEntradas_SinMovimientos_DevuelveCero() {
            assertEquals(0, inventario.getTotalEntradas());
        }

        @Test
        @DisplayName("entradaStock en producto inexistente lanza excepción")
        void entradaStock_Inexistente_LanzaExcepcion() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> inventario.entradaStock("X", 1, "Test"));
            assertEquals("Producto no encontrado: X", ex.getMessage());
        }

        @Test
        @DisplayName("salidaStock en producto inexistente lanza excepción")
        void salidaStock_Inexistente_LanzaExcepcion() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> inventario.salidaStock("X", 1, "Test"));
            assertEquals("Producto no encontrado: X", ex.getMessage());
        }

        @Test
        @DisplayName("verificarStock en producto inexistente lanza excepción")
        void verificarStock_Inexistente_LanzaExcepcion() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> inventario.verificarStock("X", 1));
            assertEquals("Producto no encontrado: X", ex.getMessage());
        }
    }

    @Nested
    @DisplayName("Validaciones de Movimiento")
    class MovimientoTests {

        @Test
        @DisplayName("Movimiento válido asigna fecha y datos")
        void movimientoValido_AsignaDatos() {
            Movimiento movimiento = new Movimiento(Movimiento.Tipo.ENTRADA, 2, "P001", "Compra");
            assertNotNull(movimiento.getFecha());
            assertEquals(Movimiento.Tipo.ENTRADA, movimiento.getTipo());
            assertEquals(2, movimiento.getCantidad());
            assertEquals("P001", movimiento.getProductoId());
        }

        @Test
        @DisplayName("Movimiento con tipo nulo lanza excepción")
        void movimientoTipoNulo_LanzaExcepcion() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> new Movimiento(null, 1, "P001", "Test"));
            assertEquals("Tipo de movimiento no puede ser nulo", ex.getMessage());
        }

        @Test
        @DisplayName("Movimiento con cantidad no positiva lanza excepción")
        void movimientoCantidadNoPositiva_LanzaExcepcion() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> new Movimiento(Movimiento.Tipo.ENTRADA, 0, "P001", "Test"));
            assertEquals("La cantidad debe ser positiva", ex.getMessage());
        }

        @Test
        @DisplayName("Movimiento con productoId vacío lanza excepción")
        void movimientoProductoIdVacio_LanzaExcepcion() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> new Movimiento(Movimiento.Tipo.SALIDA, 1, " ", "Test"));
            assertEquals("ProductoId no puede estar vacío", ex.getMessage());
        }

        @Test
        @DisplayName("Movimiento con productoId null lanza excepción")
        void movimientoProductoIdNull_LanzaExcepcion() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> new Movimiento(Movimiento.Tipo.SALIDA, 1, null, "Test"));
            assertEquals("ProductoId no puede estar vacío", ex.getMessage());
        }

        @Test
        @DisplayName("Movimiento con cantidad negativa lanza excepción")
        void movimientoCantidadNegativa_LanzaExcepcion() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> new Movimiento(Movimiento.Tipo.ENTRADA, -5, "P001", "Test"));
            assertEquals("La cantidad debe ser positiva", ex.getMessage());
        }

        @Test
        @DisplayName("toString incluye datos relevantes")
        void movimientoToString_ContieneDatos() {
            Movimiento movimiento = new Movimiento(Movimiento.Tipo.SALIDA, 1, "P001", "Venta");
            assertTrue(movimiento.toString().contains("SALIDA"));
            assertTrue(movimiento.toString().contains("P001"));
        }

        @Test
        @DisplayName("Movimiento con motivo null es aceptado y toString muestra null")
        void movimientoMotivoNull_Aceptado() {
            Movimiento movimiento = new Movimiento(Movimiento.Tipo.ENTRADA, 2, "P010", null);
            assertTrue(movimiento.toString().contains("null"));
            assertEquals(null, movimiento.getMotivo());
            assertEquals(Movimiento.Tipo.ENTRADA, movimiento.getTipo());
            assertEquals(2, movimiento.getCantidad());
            assertEquals("P010", movimiento.getProductoId());
            assertNotNull(movimiento.getFecha());
        }

        @Test
        @DisplayName("Movimiento almacena y devuelve el motivo correctamente")
        void movimientoGetMotivo_RetornaValor() {
            Movimiento movimiento = new Movimiento(Movimiento.Tipo.ENTRADA, 3, "P011", "Ajuste");
            assertEquals("Ajuste", movimiento.getMotivo());
        }
    }
}
