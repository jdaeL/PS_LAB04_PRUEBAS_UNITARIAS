package pe.com.lab04;

public class Main {
    public static void main(String[] args) {
        // Implementación real del servicio (sin mock)
        ServicioPrecio servicioReal = new ServicioPrecio() {
            @Override
            public double calcularDescuento(double total) {
                if (total > 500) return total * 0.10; // 10% descuento
                return 0;
            }

            @Override
            public double calcularImpuesto(double total) {
                return total * 0.19; // 19% IVA
            }
        };

        CarritoCompra carrito = new CarritoCompra(servicioReal);

        Producto laptop = new Producto("P001", "Laptop", 1000.0, true);
        Producto mouse = new Producto("P002", "Mouse", 25.0, true);
        Producto teclado = new Producto("P003", "Teclado", 50.0, false); // no disponible

        carrito.agregarProducto(laptop, 1);
        carrito.agregarProducto(mouse, 2);
        try {
            carrito.agregarProducto(teclado, 1);
        } catch (IllegalStateException e) {
            System.out.println("Error: " + e.getMessage());
        }

        System.out.println(carrito.obtenerResumenCompra());
        System.out.println("Historial:");
        carrito.getHistorialOperaciones().forEach(System.out::println);
    }
}