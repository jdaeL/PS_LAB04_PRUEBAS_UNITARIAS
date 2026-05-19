package pe.com.lab04;

public class Producto {
    private final String id;
    private final String nombre;
    private final double precio;
    private boolean disponible;

    public Producto(String id, String nombre, double precio, boolean disponible) {
        if (id == null || id.isBlank())
            throw new IllegalArgumentException("ID no puede estar vacío");
        if (nombre == null || nombre.isBlank())
            throw new IllegalArgumentException("Nombre no puede estar vacío");
        if (precio <= 0)
            throw new IllegalArgumentException("El precio debe ser positivo");
        this.id = id;
        this.nombre = nombre;
        this.precio = precio;
        this.disponible = disponible;
    }

    public String getId() { return id; }
    public String getNombre() { return nombre; }
    public double getPrecio() { return precio; }
    public boolean isDisponible() { return disponible; }
    public void setDisponible(boolean disponible) { this.disponible = disponible; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Producto)) return false;
        Producto producto = (Producto) o;
        return id.equals(producto.id);
    }

    @Override
    public int hashCode() { return id.hashCode(); }
}
