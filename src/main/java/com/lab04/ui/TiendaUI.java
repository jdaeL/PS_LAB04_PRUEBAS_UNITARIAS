package com.lab04.ui;

import com.lab04.propuestos.ej1_inventario.Inventario;
import com.lab04.propuestos.ej1_inventario.Movimiento;
import com.lab04.propuestos.ej1_inventario.Producto;
import com.lab04.propuestos.ej2_compras.CarritoCompra;
import com.lab04.propuestos.ej2_compras.ItemCarrito;
import com.lab04.propuestos.ej2_compras.ServicioPrecio;
import com.lab04.propuestos.ej2_compras.ServicioPrecioReal;
import javafx.application.Application;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Pos;
import javafx.stage.Stage;
import javafx.beans.binding.Bindings;

import java.util.Optional;

/**
 * Interfaz gráfica profesional que integra:
 * - Gestión de inventario (Ejercicio 1): ver productos, agregar stock, retirar
 * stock.
 * - Carrito de compras (Ejercicio 2): agregar productos al carrito, actualizar
 * cantidades,
 * calcular total con descuentos/impuestos, finalizar compra (descuenta stock
 * real).
 */
public class TiendaUI extends Application {

    private Inventario inventario;
    private CarritoCompra carrito;
    private ServicioPrecio servicioPrecio;

    // Modelos para las tablas
    private ObservableList<Producto> productosInventario;
    private FilteredList<Producto> productosFiltrados;
    private ObservableList<ItemCarritoView> itemsCarritoView;

    // Componentes de la UI
    private TableView<Producto> tablaInventario;
    private TableView<ItemCarritoView> tablaCarrito;
    private TextField filtroInventario;
    private TextField filtroCarrito;
    private FilteredList<ItemCarritoView> itemsCarritoFiltrados;
    private ListView<String> listaHistorial;
    private Button btnCalcularTotal;
    private Button btnFinalizarCompra;
    private Button btnAgregarAlCarrito;
    private Button btnRetirarStock;
    private Button btnActualizarCantidad;
    private Button btnRemover;
    private Button btnVaciar;
    private Label labelSubtotal;
    private Label labelDescuento;
    private Label labelImpuesto;
    private Label labelTotal;
    private Label labelTituloCarrito;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        // Inicializar servicios y datos de ejemplo
        servicioPrecio = new ServicioPrecioReal();
        inventario = new Inventario();
        cargarDatosEjemplo();

        carrito = new CarritoCompra(servicioPrecio, inventario);

        // Crear paneles
        VBox panelInventario = crearPanelInventario();
        VBox panelCarrito = crearPanelCarrito();
        VBox panelAcciones = crearPanelAcciones();

        btnAgregarAlCarrito.disableProperty().bind(
            Bindings.createBooleanBinding(() -> {
                Producto productoSeleccionado = tablaInventario.getSelectionModel().getSelectedItem();
                return productoSeleccionado == null || productoSeleccionado.getStock() <= 0;
            }, tablaInventario.getSelectionModel().selectedItemProperty())
        );

        btnRetirarStock.disableProperty().bind(
            Bindings.createBooleanBinding(() -> {
                Producto productoSeleccionado = tablaInventario.getSelectionModel().getSelectedItem();
                return productoSeleccionado == null || productoSeleccionado.getStock() <= 0;
            }, tablaInventario.getSelectionModel().selectedItemProperty())
        );
        // Layout principal
        HBox mainLayout = new HBox(20, panelInventario, panelCarrito, panelAcciones);
        mainLayout.setPadding(new Insets(15));
        mainLayout.setStyle("-fx-background-color: #f4f4f4;");

        Scene scene = new Scene(mainLayout, 1300, 700);
        primaryStage.setTitle("Sistema Integral de Inventario y Carrito de Compras");
        primaryStage.setScene(scene);
        primaryStage.show();

        actualizarTablas();
    }

    // ======================== DATOS DE EJEMPLO ========================
    private void cargarDatosEjemplo() {
        inventario.agregarProducto(new Producto("P001", "Laptop Gamer", 1200.0, 5));
        inventario.agregarProducto(new Producto("P002", "Mouse Óptico", 25.0, 10));
        inventario.agregarProducto(new Producto("P003", "Teclado Mecánico", 65.0, 3));
        inventario.agregarProducto(new Producto("P004", "Monitor 24\"", 180.0, 2));
        inventario.agregarProducto(new Producto("P005", "Audífonos Bluetooth", 45.0, 7));
    }

    // ======================== PANEL DE INVENTARIO ========================
    private VBox crearPanelInventario() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(10));
        panel.setStyle("-fx-border-color: #2c3e50; -fx-border-width: 2; -fx-background-color: white;");
        // Dar más espacio al panel de inventario para que las columnas puedan mostrarse
        // correctamente en pantallas habituales.
        panel.setPrefWidth(600);

        Label titulo = new Label("📦 GESTIÓN DE INVENTARIO");
        titulo.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        HBox buscador = new HBox(10);
        Label labelBuscar = new Label("Buscar:");
        filtroInventario = new TextField();
        filtroInventario.setPromptText("ID o nombre...");
        HBox.setHgrow(filtroInventario, Priority.ALWAYS);
        filtroInventario.textProperty().addListener((obs, oldVal, newVal) -> aplicarFiltroInventario(newVal));
        buscador.getChildren().addAll(labelBuscar, filtroInventario);

        // Tabla de inventario
        tablaInventario = new TableView<>();
        configurarTablaInventario();
        // Hacer que la tabla intente usar el ancho del panel (si hay menos espacio, aparecerá scrollbar)
        tablaInventario.prefWidthProperty().bind(panel.widthProperty().subtract(20));

        // Botones de gestión de stock
        Button btnAgregarStock = new Button("➕ Agregar Stock");
        btnRetirarStock = new Button("➖ Retirar Stock");
        Button btnVerMovimientos = new Button("📋 Ver Movimientos");
        Button btnCrearProducto = new Button("🆕 Crear Producto");
        btnAgregarStock.setOnAction(e -> gestionarStock(true));
        btnRetirarStock.setOnAction(e -> gestionarStock(false));
        btnVerMovimientos.setOnAction(e -> mostrarVentanaMovimientos());
        btnCrearProducto.setOnAction(e -> mostrarVentanaCrearProducto());
        HBox botonesStock1 = new HBox(10, btnAgregarStock, btnRetirarStock);
        HBox botonesStock2 = new HBox(10, btnVerMovimientos, btnCrearProducto);
        botonesStock1.setPadding(new Insets(5, 0, 0, 0));
        botonesStock2.setPadding(new Insets(0, 0, 5, 0));

        panel.getChildren().addAll(titulo, buscador, tablaInventario, botonesStock1, botonesStock2);
        return panel;
    }

    private void configurarTablaInventario() {
        TableColumn<Producto, String> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getId()));

        TableColumn<Producto, String> colNombre = new TableColumn<>("Producto");
        colNombre.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getNombre()));

        TableColumn<Producto, Double> colPrecio = new TableColumn<>("Precio (Soles)");
        colPrecio.setCellValueFactory(cell -> new SimpleDoubleProperty(cell.getValue().getPrecio()).asObject());
        colPrecio.setStyle("-fx-alignment: CENTER-RIGHT;");
        colPrecio.setCellFactory(column -> new TableCell<Producto, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f", item));
                }
            }
        });

        TableColumn<Producto, Integer> colStock = new TableColumn<>("Stock");
        colStock.setCellValueFactory(cell -> new SimpleIntegerProperty(cell.getValue().getStock()).asObject());
        colStock.setStyle("-fx-alignment: CENTER;");
        TableColumn<Producto, String> colDisp = new TableColumn<>("Disp.");
        colDisp.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().isDisponible() ? "✅" : "❌"));
        colDisp.setStyle("-fx-alignment: CENTER;");
        colDisp.setPrefWidth(50);

        // Asignar anchos preferidos a las columnas para evitar truncado
        colId.setPrefWidth(60);
        colNombre.setPrefWidth(200);
        colNombre.setMinWidth(150);
        colPrecio.setPrefWidth(110);
        colStock.setPrefWidth(90);
        colDisp.setPrefWidth(60);

        tablaInventario.getColumns().addAll(colId, colNombre, colPrecio, colStock, colDisp);
        productosInventario = FXCollections.observableArrayList();
        productosFiltrados = new FilteredList<>(productosInventario, p -> true);
        SortedList<Producto> productosOrdenados = new SortedList<>(productosFiltrados);
        productosOrdenados.comparatorProperty().bind(tablaInventario.comparatorProperty());
        tablaInventario.setItems(productosOrdenados);
        // No forzar ajuste automático; permitir scroll horizontal si la ventana es más pequeña
        tablaInventario.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

        tablaInventario.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(Producto item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setStyle("");
                } else if (item.getStock() == 0) {
                    setStyle("-fx-background-color: #f5c6cb;"); // rojo claro
                } else if (item.getStock() <= 1) {
                    setStyle("-fx-background-color: #fff3cd;"); // amarillo advertencia
                } else {
                    setStyle("");
                }
            }
        });
    }

    private void gestionarStock(boolean esAgregar) {
        Producto seleccionado = tablaInventario.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarAlerta("Debe seleccionar un producto.", Alert.AlertType.WARNING);
            return;
        }

        TextInputDialog dialog = new TextInputDialog("1");
        dialog.setTitle(esAgregar ? "Agregar Stock" : "Retirar Stock");
        dialog.setHeaderText(esAgregar ? "Ingrese la cantidad a agregar:" : "Ingrese la cantidad a retirar:");
        dialog.setContentText("Cantidad:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(cantStr -> {
            try {
                int cantidad = Integer.parseInt(cantStr);
                if (esAgregar) {
                    inventario.entradaStock(seleccionado.getId(), cantidad, "Ajuste manual UI");
                    mostrarAlerta("Stock agregado correctamente.", Alert.AlertType.INFORMATION);
                } else {
                    inventario.salidaStock(seleccionado.getId(), cantidad, "Retiro manual UI");
                    mostrarAlerta("Stock retirado correctamente.", Alert.AlertType.INFORMATION);
                }
                actualizarTablas();
            } catch (NumberFormatException e) {
                mostrarAlerta("Cantidad inválida.", Alert.AlertType.ERROR);
            } catch (IllegalArgumentException | IllegalStateException e) {
                mostrarAlerta("Error: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        });
    }

    private void mostrarVentanaMovimientos() {
        Stage ventana = new Stage();
        ventana.setTitle("📋 Historial de Movimientos del Inventario");

        TableView<Movimiento> tabla = new TableView<>();

        TableColumn<Movimiento, String> colFecha = new TableColumn<>("Fecha");
        colFecha.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFecha().toString()));
        colFecha.setPrefWidth(180);

        TableColumn<Movimiento, String> colTipo = new TableColumn<>("Tipo");
        colTipo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTipo().name()));
        colTipo.setPrefWidth(80);

        TableColumn<Movimiento, String> colProd = new TableColumn<>("Producto ID");
        colProd.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getProductoId()));
        colProd.setPrefWidth(100);

        TableColumn<Movimiento, Integer> colCant = new TableColumn<>("Cantidad");
        colCant.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getCantidad()).asObject());
        colCant.setPrefWidth(80);

        TableColumn<Movimiento, String> colMotivo = new TableColumn<>("Motivo");
        colMotivo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getMotivo()));

        tabla.getColumns().addAll(colFecha, colTipo, colProd, colCant, colMotivo);
        tabla.setItems(FXCollections.observableArrayList(inventario.getMovimientos()));
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        // Resaltar entradas en verde, salidas en rojo
        tabla.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(Movimiento m, boolean empty) {
                super.updateItem(m, empty);
                if (m == null || empty)
                    setStyle("");
                else if (m.getTipo() == Movimiento.Tipo.ENTRADA)
                    setStyle("-fx-background-color: #d4edda;");
                else
                    setStyle("-fx-background-color: #f8d7da;");
            }
        });

        // Totales de entradas/salidas
        Label resumen = new Label(String.format("Total entradas: %d  |  Total salidas: %d",
                inventario.getTotalEntradas(), inventario.getTotalSalidas()));
        resumen.setStyle("-fx-font-weight: bold; -fx-padding: 8px;");

        VBox layout = new VBox(10, tabla, resumen);
        layout.setPadding(new Insets(10));
        VBox.setVgrow(tabla, Priority.ALWAYS);

        ventana.setScene(new Scene(layout, 700, 450));
        ventana.show();
    }

    private void mostrarVentanaCrearProducto() {
        Stage ventana = new Stage();
        ventana.setTitle("🆕 Crear Nuevo Producto");

        // Formulario
        GridPane formulario = new GridPane();
        formulario.setHgap(12);
        formulario.setVgap(12);
        formulario.setPadding(new Insets(30));
        formulario.setAlignment(Pos.CENTER);

        Label lblTitulo = new Label("📦 NUEVO PRODUCTO");
        lblTitulo.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Label lblId = new Label("Código (ID):");
        lblId.setStyle("-fx-font-weight: bold;");
        TextField txtId = new TextField();
        txtId.setPromptText("Ej: P006");
        txtId.setPrefWidth(250);

        Label lblNombre = new Label("Nombre:");
        lblNombre.setStyle("-fx-font-weight: bold;");
        TextField txtNombre = new TextField();
        txtNombre.setPromptText("Ej: Cámara Web HD");
        txtNombre.setPrefWidth(250);

        Label lblPrecio = new Label("Precio (S/):");
        lblPrecio.setStyle("-fx-font-weight: bold;");
        TextField txtPrecio = new TextField();
        txtPrecio.setPromptText("Ej: 59.99");
        txtPrecio.setPrefWidth(250);

        Label lblStock = new Label("Stock Inicial:");
        lblStock.setStyle("-fx-font-weight: bold;");
        TextField txtStock = new TextField();
        txtStock.setPromptText("Ej: 10");
        txtStock.setPrefWidth(250);

        Label lblMensaje = new Label();
        lblMensaje.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");

        formulario.add(lblTitulo, 0, 0, 2, 1);
        formulario.add(new Separator(), 0, 1, 2, 1);
        formulario.add(lblId, 0, 2);
        formulario.add(txtId, 1, 2);
        formulario.add(lblNombre, 0, 3);
        formulario.add(txtNombre, 1, 3);
        formulario.add(lblPrecio, 0, 4);
        formulario.add(txtPrecio, 1, 4);
        formulario.add(lblStock, 0, 5);
        formulario.add(txtStock, 1, 5);
        formulario.add(new Separator(), 0, 6, 2, 1);
        formulario.add(lblMensaje, 0, 8, 2, 1);

        // Botones
        Button btnCrear = new Button("✅ Crear Producto");
        btnCrear.setStyle(
                "-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20;");
        Button btnLimpiar = new Button("🔄 Limpiar");
        btnLimpiar.setStyle("-fx-padding: 8 20;");
        Button btnCerrar = new Button("❌ Cerrar");
        btnCerrar.setStyle("-fx-padding: 8 20;");

        btnLimpiar.setOnAction(e -> {
            txtId.clear();
            txtNombre.clear();
            txtPrecio.clear();
            txtStock.clear();
            lblMensaje.setText("");
            lblMensaje.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
        });

        btnCerrar.setOnAction(e -> ventana.close());

        btnCrear.setOnAction(e -> {
            String id = txtId.getText().trim();
            String nombre = txtNombre.getText().trim();
            String precioStr = txtPrecio.getText().trim();
            String stockStr = txtStock.getText().trim();

            if (id.isEmpty() || nombre.isEmpty() || precioStr.isEmpty() || stockStr.isEmpty()) {
                lblMensaje.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                lblMensaje.setText("⚠️ Todos los campos son obligatorios.");
                return;
            }

            try {
                double precio;
                try {
                    precio = Double.parseDouble(precioStr);
                } catch (NumberFormatException ex) {
                    lblMensaje.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                    lblMensaje.setText("⚠️ El precio debe ser un número válido (Ej: 59.99).");
                    txtPrecio.requestFocus();
                    return;
                }

                int stock;
                try {
                    stock = Integer.parseInt(stockStr);
                } catch (NumberFormatException ex) {
                    lblMensaje.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                    lblMensaje.setText("⚠️ El stock debe ser un número entero (sin decimales).");
                    txtStock.requestFocus();
                    return;
                }

                // Validar que el precio sea positivo
                if (precio <= 0) {
                    lblMensaje.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                    lblMensaje.setText("⚠️ El precio debe ser positivo.");
                    txtPrecio.requestFocus();
                    return;
                }

                // Validar que el stock inicial sea no-negativo
                if (stock < 0) {
                    lblMensaje.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                    lblMensaje.setText("⚠️ El stock no puede ser negativo.");
                    txtStock.requestFocus();
                    return;
                }

                // Diálogo de confirmación
                Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
                confirmacion.setTitle("Confirmar creación");
                confirmacion.setHeaderText("¿Desea crear el siguiente producto?");
                confirmacion.setContentText(String.format(
                        "Código: %s\nNombre: %s\nPrecio: S/ %.2f\nStock Inicial: %d",
                        id, nombre, precio, stock));

                Optional<ButtonType> resultado = confirmacion.showAndWait();
                if (resultado.isEmpty() || resultado.get() != ButtonType.OK) {
                    return;
                }

                Producto nuevoProducto = new Producto(id, nombre, precio, stock);
                inventario.agregarProducto(nuevoProducto);
                actualizarTablas();

                lblMensaje.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                lblMensaje.setText("✅ Producto '" + nombre + "' creado exitosamente.");

                // Limpiar campos para siguiente producto
                txtId.clear();
                txtNombre.clear();
                txtPrecio.clear();
                txtStock.clear();
                txtId.requestFocus();
            } catch (IllegalArgumentException ex) {
                lblMensaje.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                lblMensaje.setText("⚠️ " + ex.getMessage());
            }
        });

        HBox botones = new HBox(10, btnCrear, btnLimpiar, btnCerrar);
        formulario.add(botones, 0, 7, 2, 1);

        // Resumen de productos existentes
        Label resumen = new Label(String.format("Productos registrados: %d", inventario.listarProductos().size()));
        resumen.setStyle("-fx-font-weight: bold; -fx-padding: 8px;");

        VBox layout = new VBox(10, formulario, resumen);
        layout.setPadding(new Insets(10));
        layout.setAlignment(Pos.CENTER);
        VBox.setVgrow(formulario, Priority.ALWAYS);

        ventana.setScene(new Scene(layout, 700, 450));
        ventana.show();
    }

    // ======================== PANEL DEL CARRITO ========================
    private VBox crearPanelCarrito() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(10));
        panel.setStyle("-fx-border-color: #16a085; -fx-border-width: 2; -fx-background-color: white;");
        panel.setPrefWidth(500);

        labelTituloCarrito = new Label("🛒 CARRITO DE COMPRAS (0 unidades)");
        labelTituloCarrito.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #16a085;");

        HBox buscadorCarrito = new HBox(8);
        Label lbl = new Label("Buscar:");
        filtroCarrito = new TextField();
        filtroCarrito.setPromptText("Nombre del producto...");
        HBox.setHgrow(filtroCarrito, Priority.ALWAYS);
        filtroCarrito.textProperty().addListener((obs, o, n) -> aplicarFiltroCarrito(n));
        buscadorCarrito.getChildren().addAll(lbl, filtroCarrito);

        // Tabla del carrito
        tablaCarrito = new TableView<>();
        configurarTablaCarrito();

        // Botones de acciones del carrito
        btnAgregarAlCarrito = new Button("➕ Agregar al carrito");
        btnActualizarCantidad = new Button("✏️ Actualizar cantidad");
        btnRemover = new Button("❌ Remover producto");
        btnVaciar = new Button("🗑️ Vaciar carrito");

        btnAgregarAlCarrito.setOnAction(e -> agregarAlCarrito());
        btnActualizarCantidad.setOnAction(e -> actualizarCantidadCarrito());
        btnRemover.setOnAction(e -> removerDelCarrito());
        btnVaciar.setOnAction(e -> vaciarCarritoConConfirmacion());

        HBox botonesCarrito = new HBox(10, btnAgregarAlCarrito, btnActualizarCantidad, btnRemover, btnVaciar);
        botonesCarrito.setPadding(new Insets(5, 0, 5, 0));
        panel.getChildren().addAll(labelTituloCarrito, buscadorCarrito, tablaCarrito, botonesCarrito);
        return panel;
    }

    private void configurarTablaCarrito() {
        TableColumn<ItemCarritoView, String> colProd = new TableColumn<>("Producto");
        colProd.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getProducto().getNombre()));

        TableColumn<ItemCarritoView, Integer> colCant = new TableColumn<>("Cantidad");
        colCant.setCellValueFactory(cell -> new SimpleIntegerProperty(cell.getValue().getCantidad()).asObject());

        TableColumn<ItemCarritoView, Double> colSub = new TableColumn<>("Subtotal");
        colSub.setCellValueFactory(cell -> new SimpleDoubleProperty(cell.getValue().getSubtotal()).asObject());
        colSub.setStyle("-fx-alignment: CENTER-RIGHT;");

        tablaCarrito.getColumns().addAll(colProd, colCant, colSub);
        itemsCarritoView = FXCollections.observableArrayList();
        itemsCarritoFiltrados =
                new FilteredList<>(itemsCarritoView, p -> true);

        SortedList<ItemCarritoView> itemsOrdenados =
                new SortedList<>(itemsCarritoFiltrados);

        itemsOrdenados.comparatorProperty()
                .bind(tablaCarrito.comparatorProperty());

        tablaCarrito.setItems(itemsOrdenados);
        tablaCarrito.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tablaCarrito.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            actualizarEstadoAcciones();
        });
    }

    private void agregarAlCarrito() {
        Producto seleccionado = tablaInventario.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarAlerta("Seleccione un producto del inventario.", Alert.AlertType.WARNING);
            return;
        }
        if (seleccionado.getStock() <= 0) {
            mostrarAlerta("El producto no tiene stock disponible.", Alert.AlertType.WARNING);
            return;
        }

        TextInputDialog dialog = new TextInputDialog("1");
        dialog.setTitle("Agregar al carrito");
        dialog.setHeaderText("Cantidad de " + seleccionado.getNombre() + " (máx. " + seleccionado.getStock() + ")");
        dialog.setContentText("Cantidad:");

        dialog.showAndWait().ifPresent(cantStr -> {
            try {
                int cantidad = Integer.parseInt(cantStr);
                com.lab04.propuestos.ej2_compras.Producto productoCarrito = crearProductoCarrito(seleccionado);
                carrito.agregarProducto(productoCarrito, cantidad);
                actualizarTablas();
                mostrarAlerta("Producto agregado al carrito.", Alert.AlertType.INFORMATION);
            } catch (NumberFormatException e) {
                mostrarAlerta("Cantidad inválida.", Alert.AlertType.ERROR);
            } catch (IllegalArgumentException | IllegalStateException e) {
                mostrarAlerta("Error: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        });
    }

    private void actualizarCantidadCarrito() {
        ItemCarritoView seleccionado = tablaCarrito.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarAlerta("Seleccione un producto del carrito.", Alert.AlertType.WARNING);
            return;
        }
        com.lab04.propuestos.ej2_compras.Producto producto = seleccionado.getProducto();
        int stockDisponible = inventario.consultarStock(producto.getId());

        TextInputDialog dialog = new TextInputDialog(String.valueOf(seleccionado.getCantidad()));
        dialog.setTitle("Actualizar cantidad");
        dialog.setHeaderText("Nueva cantidad para " + producto.getNombre() + " (máx. " + stockDisponible + ")");
        dialog.setContentText("Cantidad:");

        dialog.showAndWait().ifPresent(cantStr -> {
            try {
                int nueva = Integer.parseInt(cantStr);
                carrito.actualizarCantidad(producto, nueva);
                actualizarTablas();
            } catch (Exception e) {
                mostrarAlerta("Error: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        });
    }

    private void removerDelCarrito() {
        ItemCarritoView seleccionado = tablaCarrito.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarAlerta("Seleccione un producto del carrito.", Alert.AlertType.WARNING);
            return;
        }
        carrito.removerProducto(seleccionado.getProducto());
        actualizarTablas();
    }
    private void vaciarCarritoConConfirmacion() {
        if (carrito.estaVacio()) {
            mostrarAlerta("El carrito ya está vacío.", Alert.AlertType.INFORMATION);
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Vaciar carrito");
        confirm.setHeaderText("¿Está seguro que desea vaciar el carrito?");
        confirm.setContentText("Se eliminarán todos los productos (" + carrito.contarItems() + " unidades).");
        confirm.showAndWait().ifPresent(resp -> {
            if (resp == ButtonType.OK) {
                carrito.vaciarCarrito();
                actualizarTablas();
            }
        });
    }
    private void vaciarCarrito() {
        carrito.vaciarCarrito();
        actualizarTablas();
    }

    // ======================== PANEL DE TOTALES Y FINALIZAR
    // ========================
    private VBox crearPanelAcciones() {
        VBox panel = new VBox(15);
        panel.setPadding(new Insets(10));
        panel.setStyle("-fx-border-color: #e67e22; -fx-border-width: 2; -fx-background-color: white;");
        panel.setPrefWidth(250);

        Label titulo = new Label("💰 TOTAL Y PAGO");
        titulo.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #e67e22;");

        labelSubtotal = new Label("S/ 0.00");
        labelDescuento = new Label("S/ 0.00");
        labelImpuesto = new Label("S/ 0.00");
        labelTotal = new Label("S/ 0.00");
        labelTotal.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #27ae60;");

        GridPane resumen = new GridPane();
        resumen.setHgap(10);
        resumen.setVgap(8);
        resumen.addRow(0, new Label("Subtotal:"), labelSubtotal);
        resumen.addRow(1, new Label("Descuento:"), labelDescuento);
        resumen.addRow(2, new Label("Impuesto:"), labelImpuesto);
        resumen.addRow(3, new Label("Total:"), labelTotal);

        btnCalcularTotal = new Button("🧮 Calcular total");
        btnFinalizarCompra = new Button("✅ Finalizar compra");

        btnCalcularTotal.setOnAction(e -> actualizarTotal());
        btnFinalizarCompra.setOnAction(e -> finalizarCompra());

        Label tituloHistorial = new Label("📋 Historial del carrito");
        tituloHistorial.setStyle("-fx-font-weight: bold;");
        listaHistorial = new ListView<>();
        listaHistorial.setPrefHeight(220);
        listaHistorial.setPlaceholder(new Label("Sin operaciones"));
        VBox.setVgrow(listaHistorial, Priority.ALWAYS);

        panel.getChildren().addAll(titulo, resumen, btnCalcularTotal, btnFinalizarCompra, new Separator(),
                tituloHistorial, listaHistorial);
        return panel;
    }

    private void actualizarTotal() {
        try {
            carrito.calcularTotal();
            actualizarTotalesUI();
        } catch (Exception e) {
            mostrarAlerta("Error al calcular total: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void finalizarCompra() {
        try {
            carrito.finalizarCompra();
            mostrarAlerta("Compra finalizada con éxito. El stock ha sido actualizado.", Alert.AlertType.INFORMATION);
            actualizarTablas(); // refresca inventario y carrito
        } catch (IllegalStateException e) {
            mostrarAlerta("No se pudo finalizar: " + e.getMessage(), Alert.AlertType.ERROR);
        } catch (Exception e) {
            mostrarAlerta("Error inesperado: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    // ======================== MÉTODOS AUXILIARES ========================
    private void actualizarTablas() {
        // Actualizar inventario
        productosInventario.setAll(inventario.listarProductos());

        // Actualizar vista del carrito
        itemsCarritoView.setAll(carrito.getItems().stream()
                .map(item -> new ItemCarritoView(item.getProducto(), item.getCantidad(), item.getSubtotal()))
                .toList());

        actualizarHistorial();
        actualizarTotalesUI();
        actualizarEstadoAcciones();
        actualizarTituloCarrito();
    }

    private void actualizarTituloCarrito() {
        int total = carrito.contarItems();
        labelTituloCarrito.setText("🛒 CARRITO DE COMPRAS (" + total + " unidad" + (total == 1 ? "" : "es") + ")");
    }
    private void mostrarAlerta(String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(tipo == Alert.AlertType.ERROR ? "Error" : "Información");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void aplicarFiltroCarrito(String filtro) {
        String texto = filtro == null ? "" : filtro.trim().toLowerCase();
        itemsCarritoFiltrados.setPredicate(item ->
                texto.isEmpty() || item.getProducto().getNombre().toLowerCase().contains(texto));
    }

    private com.lab04.propuestos.ej2_compras.Producto crearProductoCarrito(Producto inventarioProducto) {
        return new com.lab04.propuestos.ej2_compras.Producto(
                inventarioProducto.getId(),
                inventarioProducto.getNombre(),
                inventarioProducto.getPrecio(),
                inventarioProducto.isDisponible());
    }

    private void aplicarFiltroInventario(String filtro) {
        String texto = filtro == null ? "" : filtro.trim().toLowerCase();
        productosFiltrados.setPredicate(producto -> {
            if (texto.isEmpty())
                return true;
            return producto.getId().toLowerCase().contains(texto)
                    || producto.getNombre().toLowerCase().contains(texto);
        });
    }

    private void actualizarHistorial() {
        listaHistorial.getItems().setAll(carrito.getHistorialOperaciones());
        if (!listaHistorial.getItems().isEmpty()) {
            listaHistorial.scrollTo(listaHistorial.getItems().size() - 1);
        }
    }

    private void actualizarTotalesUI() {
        Totales totales = calcularTotales();
        labelSubtotal.setText(String.format("S/ %.2f", totales.subtotal));
        labelDescuento.setText(String.format("S/ %.2f", totales.descuento));
        labelImpuesto.setText(String.format("S/ %.2f", totales.impuesto));
        labelTotal.setText(String.format("S/ %.2f", totales.total));
    }

    private Totales calcularTotales() {
        double subtotal = carrito.getItems().stream().mapToDouble(ItemCarrito::getSubtotal).sum();
        double descuento = servicioPrecio.calcularDescuento(subtotal);
        double conDescuento = subtotal - descuento;
        double impuesto = servicioPrecio.calcularImpuesto(conDescuento);
        double total = conDescuento + impuesto;
        return new Totales(subtotal, descuento, impuesto, total);
    }

    private void actualizarEstadoAcciones() {
        boolean carritoVacio = carrito.getItems().isEmpty();
        btnCalcularTotal.setDisable(carritoVacio);
        btnFinalizarCompra.setDisable(carritoVacio);
        btnVaciar.setDisable(carritoVacio);
        if (carritoVacio) {
            btnActualizarCantidad.setDisable(true);
            btnRemover.setDisable(true);
        } else {
            ItemCarritoView seleccionado = tablaCarrito.getSelectionModel().getSelectedItem();
            btnActualizarCantidad.setDisable(seleccionado == null);
            btnRemover.setDisable(seleccionado == null);
        }
    }

    // Clase auxiliar para mostrar datos del carrito en la tabla
    public static class ItemCarritoView {
        private final com.lab04.propuestos.ej2_compras.Producto producto;
        private final int cantidad;
        private final double subtotal;

        public ItemCarritoView(com.lab04.propuestos.ej2_compras.Producto producto, int cantidad, double subtotal) {
            this.producto = producto;
            this.cantidad = cantidad;
            this.subtotal = subtotal;
        }

        public com.lab04.propuestos.ej2_compras.Producto getProducto() {
            return producto;
        }

        public int getCantidad() {
            return cantidad;
        }

        public double getSubtotal() {
            return subtotal;
        }
    }

    private static class Totales {
        private final double subtotal;
        private final double descuento;
        private final double impuesto;
        private final double total;

        private Totales(double subtotal, double descuento, double impuesto, double total) {
            this.subtotal = subtotal;
            this.descuento = descuento;
            this.impuesto = impuesto;
            this.total = total;
        }
    }
}
