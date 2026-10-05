package test.java.com.carrito;



import java.math.BigDecimal;
import java.math.RoundingMode;

import com.servicio.carrito.dto.ItemCarritoRequestDTO;
import com.servicio.carrito.dto.LibroDTO;
import com.servicio.carrito.model.Carrito;
import com.servicio.carrito.model.ItemCarrito;

import net.datafaker.Faker;

public class TestDataFactory {
  private static final Faker faker = new Faker();

    public static LibroDTO unLibroDTO() {
        LibroDTO dto = new LibroDTO();
        dto.setId(faker.number().numberBetween(1L, 999L));
        dto.setTitulo(faker.book().title());
        dto.setIsbn(faker.code().isbn13());
        dto.setPrecio(precio());
        dto.setCategoriaNombre(faker.book().genre());
        return dto;
    }

    public static Carrito unCarrito() {
        return new Carrito(faker.name().username());
    }

    public static ItemCarrito unItem(LibroDTO libro, Carrito carrito) {
        return new ItemCarrito(
                libro.getId(),
                libro.getTitulo(),
                libro.getPrecio(),
                faker.number().numberBetween(1, 5),
                carrito
        );
    }

    public static ItemCarritoRequestDTO unItemRequest(Long libroId) {
        ItemCarritoRequestDTO dto = new ItemCarritoRequestDTO();
        dto.setLibroId(libroId);
        dto.setCantidad(faker.number().numberBetween(1, 5));
        return dto;
    }

    private static BigDecimal precio() {
        return BigDecimal.valueOf(faker.number().randomDouble(2, 5000, 80000))
                .setScale(2, RoundingMode.HALF_UP);
    }
}
