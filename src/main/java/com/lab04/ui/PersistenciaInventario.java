package com.lab04.ui;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.lab04.propuestos.ej1_inventario.Inventario;
import com.lab04.propuestos.ej1_inventario.Producto;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.*;
import java.util.List;

public class PersistenciaInventario {

    // El archivo se guarda junto al .jar o en la carpeta del proyecto
    private static final String RUTA_ARCHIVO = "productos.json";

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    /** Guarda todos los productos del inventario en el archivo JSON */
    public static void guardar(Inventario inventario) {
        List<Producto> productos = inventario.listarProductos();
        try (Writer writer = new FileWriter(RUTA_ARCHIVO)) {
            gson.toJson(productos, writer);
        } catch (IOException e) {
            System.err.println("Error al guardar productos: " + e.getMessage());
        }
    }

    /** Carga productos desde el archivo JSON al inventario.
     *  Retorna true si cargó desde archivo, false si el archivo no existía. */
    public static boolean cargar(Inventario inventario) {
        Path path = Path.of(RUTA_ARCHIVO);
        if (!Files.exists(path)) {
            return false; // archivo no existe, usar datos de ejemplo
        }
        try (Reader reader = new FileReader(RUTA_ARCHIVO)) {
            Type listaTipo = new TypeToken<List<ProductoDTO>>() {}.getType();
            List<ProductoDTO> dtos = gson.fromJson(reader, listaTipo);
            if (dtos == null || dtos.isEmpty()) return false;
            for (ProductoDTO dto : dtos) {
                inventario.agregarProducto(new Producto(dto.codigo, dto.nombre, dto.precio, dto.stock));
            }
            return true;
        } catch (IOException e) {
            System.err.println("Error al cargar productos: " + e.getMessage());
            return false;
        }
    }

    // DTO simple para serializar/deserializar
    private static class ProductoDTO {
        String codigo;
        String nombre;
        double precio;
        int stock;
    }
}