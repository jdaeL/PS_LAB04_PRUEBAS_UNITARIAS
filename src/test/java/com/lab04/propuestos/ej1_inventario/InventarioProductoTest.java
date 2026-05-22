package com.lab04.propuestos.ej1_inventario;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
            // Uso de assertAll según la sección 1.6 de la guía
            assertAll("Verificación de atributos del Producto",
                () -> assertEquals("P001", producto.getCodigo(), "El código no coincide"),
                () -> assertEquals("P001", producto.getId(), "El ID no coincide"),
                () -> assertEquals("Producto Base", producto.getNombre(), "El nombre no coincide"),
                () -> assertEquals(10.0, producto.getPrecio(), "El precio no coincide"),
                () -> assertEquals(5, producto.getStock(), "El stock no coincide"),
                () -> assertEquals(5, producto.getCantidad(), "La cantidad no coincide"),
                () -> assertTrue(producto.isDisponible(), "El producto debería estar disponible")
            );
        }

        // Nueva prueba para garantizar que la sanitización de datos (trim y uppercase) funciona
        @Test
        @DisplayName("Constructor sanitiza código y nombre (Trim y UpperCase)")
        void constructor_SanitizaEntradas() {
            Producto pSucio = new Producto(" p002  ", "  Teclado Mecánico  ", 50.0, 10);
            assertAll("Verificación de sanitización",
                () -> assertEquals("P002", pSucio.getCodigo(), "El código debe estar sin espacios y en mayúsculas"),
                () -> assertEquals("P002", pSucio.getId(), "El ID debe estar sin espacios y en mayúsculas"),
                () -> assertEquals("Teclado Mecánico", pSucio.getNombre(), "El nombre debe estar sin espacios extra")
            );
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
            
            // Combinar comprobaciones de estado después de una operación
            assertAll("Verificación de stock tras agregado",
                () -> assertEquals(8, producto.getStock(), "El stock total debe ser 8"),
                () -> assertTrue(producto.isDisponible(), "El producto debe seguir disponible")
            );
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
            
            // Uso de assertAll
            assertAll("Verificación de equals y hashCode",
                () -> assertEquals(producto, mismoId, "Los productos deberían ser iguales por ID"),
                () -> assertEquals(producto.hashCode(), mismoId.hashCode(), "Los hashCodes deberían coincidir")
            );
        }

        @Test
        @DisplayName("toString incluye datos básicos")
        void toString_ContieneDatos() {
            String s = producto.toString();
            assertAll("Verificación de toString",
                () -> assertTrue(s.contains("Producto Base"), "Debería contener el nombre"),
                () -> assertTrue(s.contains("P001"), "Debería contener el código")
            );
        }

        @Test
        @DisplayName("Getters retornan los valores esperados")
        void getters_RetornanValores() {
            // Uso de assertAll para validar el estado completo
            assertAll("Verificación de getters",
                () -> assertEquals("P001", producto.getCodigo()),
                () -> assertEquals("P001", producto.getId()),
                () -> assertEquals("Producto Base", producto.getNombre()),
                () -> assertEquals(10.0, producto.getPrecio()),
                () -> assertEquals(5, producto.getStock()),
                () -> assertEquals(5, producto.getCantidad()),
                () -> assertEquals(5, producto.consultarStock())
            );
        }

        @Test
        @DisplayName("equals con la misma referencia es true y con otro tipo es false")
        void equals_ReferenciaYOtroTipo() {
            assertAll("Verificación avanzada de equals",
                () -> assertTrue(producto.equals(producto), "Misma referencia debe ser true"),
                () -> assertFalse(producto.equals("algo"), "Comparar con otro tipo debe ser false"),
                () -> assertFalse(producto.equals(null), "Comparar con null debe ser false")
            );
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
            
            // Uso de assertAll
            assertAll("Registro de movimiento al agregar",
                () -> assertEquals(1, movimientos.size(), "Debe haber 1 movimiento"),
                () -> assertEquals(Movimiento.Tipo.ENTRADA, movimientos.get(0).getTipo(), "El tipo debe ser ENTRADA"),
                () -> assertEquals(5, movimientos.get(0).getCantidad(), "La cantidad debe coincidir con el stock")
            );
        }

        @Test
        @DisplayName("Agregar producto con stock cero no registra movimiento")
        void agregarProducto_StockCero_NoRegistraMovimiento() {
            Producto sinStock = new Producto("P002", "Sin stock", 5.0, 0);
            inventario.agregarProducto(sinStock);
            assertTrue(inventario.getMovimientos().isEmpty());
        }

        @Test
        @DisplayName("Agregar producto duplicado lanza excepción (incluso con espacios y minúsculas)")
        void agregarProducto_Duplicado_LanzaExcepcion() {
            inventario.agregarProducto(producto); // Agregamos "P001"
            
            // Comprobar que la validación de duplicados sanitiza correctamente la entrada
            Producto duplicadoSucio = new Producto(" p001  ", "Otro Nombre", 10.0, 5);
            
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> inventario.agregarProducto(duplicadoSucio));
            assertEquals("Ya existe un producto con ID: P001", ex.getMessage());
        }

        // Prueba específica para asegurar que los métodos de Inventario ignoran los espacios y el case
        @Test
        @DisplayName("Operaciones de Inventario sanitizan el ID proporcionado")
        void operaciones_Inventario_SanitizanId() {
            inventario.agregarProducto(producto);
            
            assertAll("Verificación de sanitización en métodos de Inventario",
                () -> assertNotNull(inventario.obtenerProducto(" p001 "), "Debe encontrarlo ignorando espacios y minúsculas"),
                () -> assertTrue(inventario.verificarStock("  P001", 1), "Debe verificar stock ignorando espacios"),
                () -> assertEquals(5, inventario.consultarStock("p001"), "Debe consultar stock ignorando minúsculas")
            );
        }

        @Test
        @DisplayName("Entrada de stock actualiza cantidad y registra movimiento")
        void entradaStock_ActualizaYRegistra() {
            inventario.agregarProducto(producto);
            inventario.entradaStock("P001", 2, "Compra");
            
            assertAll("Actualización por entrada de stock",
                () -> assertEquals(7, inventario.consultarStock("P001"), "Stock incorrecto"),
                () -> assertEquals(2, inventario.getMovimientos().size(), "Debe haber 2 movimientos")
            );
        }

        @Test
        @DisplayName("Salida de stock actualiza cantidad y registra movimiento")
        void salidaStock_ActualizaYRegistra() {
            inventario.agregarProducto(producto);
            inventario.salidaStock("P001", 1, "Venta");
            
            assertAll("Actualización por salida de stock",
                () -> assertEquals(4, inventario.consultarStock("P001"), "Stock incorrecto"),
                () -> assertEquals(2, inventario.getMovimientos().size(), "Debe haber 2 movimientos")
            );
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
            assertAll("Verificación de disponibilidad de stock",
                () -> assertTrue(inventario.verificarStock("P001", 3), "Debería haber stock suficiente"),
                () -> assertFalse(inventario.verificarStock("P001", 10), "Debería faltar stock")
            );
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
        @DisplayName("buscarProductoPorNombre limpia espacios y es insensible a mayúsculas/minúsculas")
        void buscarPorNombre_NormalizaCaracteresYEspacios() {
            inventario.agregarProducto(producto);
            List<Producto> resultado = inventario.buscarProductoPorNombre("  pRoDuCtO  ");
            
            assertAll("Búsqueda con normalización",
                () -> assertEquals(1, resultado.size(), "Debe encontrar 1 producto"),
                () -> assertEquals("P001", resultado.get(0).getId(), "El ID debe coincidir")
            );
        }

        @Test
        @DisplayName("getTotalEntradas ignora los movimientos de salida")
        void getTotalEntradas_IgnoraSalidas() {
            inventario.agregarProducto(producto); 
            inventario.salidaStock("P001", 2, "Venta"); 
            assertEquals(5, inventario.getTotalEntradas());
        }

        @Test
        @DisplayName("getTotalSalidas ignora los movimientos de entrada")
        void getTotalSalidas_IgnoraEntradas() {
            inventario.agregarProducto(producto);
            inventario.salidaStock("P001", 2, "Venta"); 
            inventario.entradaStock("P001", 4, "Compra"); 
            assertEquals(2, inventario.getTotalSalidas());
        }

        @Test
        @DisplayName("eliminarProducto remueve exitosamente un producto existente")
        void eliminarProducto_Existente_EliminaCorrectamente() {
            inventario.agregarProducto(producto);
            inventario.eliminarProducto("P001");
            
            // Agrupar la validación del estado post-eliminación
            assertAll("Verificación de inventario tras eliminación",
                () -> assertThrows(IllegalArgumentException.class, () -> inventario.obtenerProducto("P001"), "No debería encontrar el producto"),
                () -> assertTrue(inventario.listarProductos().isEmpty(), "La lista de productos debe estar vacía")
            );
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" ", "   "})
        @DisplayName("eliminarProducto con ID vacío o nulo lanza excepción")
        void eliminarProducto_IdInvalido_LanzaExcepcion(String idInvalido) {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> inventario.eliminarProducto(idInvalido));
            assertEquals("ID de producto no puede estar vacío o ser nulo", ex.getMessage());
        }

        @Test
        @DisplayName("eliminarProducto con ID inexistente lanza excepción")
        void eliminarProducto_Inexistente_LanzaExcepcion() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> inventario.eliminarProducto("ID_FALSO"));
            assertEquals("Producto no encontrado: ID_FALSO", ex.getMessage());
        }
    }

    @Nested
    @DisplayName("Validaciones de Movimiento")
    class MovimientoTests {

        @Test
        @DisplayName("Movimiento válido asigna fecha y datos")
        void movimientoValido_AsignaDatos() {
            Movimiento movimiento = new Movimiento(Movimiento.Tipo.ENTRADA, 2, "P001", "Compra");
            
            // Uso de assertAll
            assertAll("Asignación de datos en Movimiento",
                () -> assertNotNull(movimiento.getFecha(), "La fecha no debe ser nula"),
                () -> assertEquals(Movimiento.Tipo.ENTRADA, movimiento.getTipo(), "Tipo incorrecto"),
                () -> assertEquals(2, movimiento.getCantidad(), "Cantidad incorrecta"),
                () -> assertEquals("P001", movimiento.getProductoId(), "ID de producto incorrecto"),
                () -> assertEquals("Compra", movimiento.getMotivo(), "Motivo incorrecto")
            );
        }

        @Test
        @DisplayName("Movimiento con tipo nulo lanza excepción")
        void movimientoTipoNulo_LanzaExcepcion() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> new Movimiento(null, 1, "P001", "Test"));
            assertEquals("Tipo de movimiento no puede ser nulo", ex.getMessage());
        }

        @Test
        @DisplayName("Movimiento con productoId vacío lanza excepción")
        void movimientoProductoIdVacio_LanzaExcepcion() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> new Movimiento(Movimiento.Tipo.SALIDA, 1, " ", "Test"));
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
            
            assertAll("Verificación de toString en Movimiento",
                () -> assertTrue(movimiento.toString().contains("SALIDA"), "Debe contener el tipo"),
                () -> assertTrue(movimiento.toString().contains("P001"), "Debe contener el ID de producto")
            );
        }

        @Test
        @DisplayName("Movimiento con motivo null es aceptado y toString muestra null")
        void movimientoMotivoNull_Aceptado() {
            Movimiento movimiento = new Movimiento(Movimiento.Tipo.ENTRADA, 2, "P010", null);
            
            // Uso de assertAll
            assertAll("Creación de movimiento con motivo null",
                () -> assertTrue(movimiento.toString().contains("null"), "toString debe manejar null"),
                () -> assertEquals(null, movimiento.getMotivo(), "El motivo debe ser null"),
                () -> assertEquals(Movimiento.Tipo.ENTRADA, movimiento.getTipo(), "El tipo debe coincidir"),
                () -> assertEquals(2, movimiento.getCantidad(), "La cantidad debe coincidir"),
                () -> assertEquals("P010", movimiento.getProductoId(), "El ID debe coincidir"),
                () -> assertNotNull(movimiento.getFecha(), "La fecha debe estar asignada")
            );
        }

        @Test
        @DisplayName("Movimiento almacena y devuelve el motivo correctamente")
        void movimientoGetMotivo_RetornaValor() {
            Movimiento movimiento = new Movimiento(Movimiento.Tipo.ENTRADA, 3, "P011", "Ajuste");
            assertEquals("Ajuste", movimiento.getMotivo());
        }
    }
}