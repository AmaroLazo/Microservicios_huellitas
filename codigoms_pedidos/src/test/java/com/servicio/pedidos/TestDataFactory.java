package com.servicio.pedidos;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

import com.servicio.pedidos.dto.LibroDTO;
import com.servicio.pedidos.dto.PedidoRequestDTO;
import com.servicio.pedidos.model.Pedido;

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

    public static PedidoRequestDTO unPedidoRequest(Long libroId) {
        PedidoRequestDTO dto = new PedidoRequestDTO();
        dto.setLibroId(libroId);
        dto.setCliente(faker.name().username());
        dto.setCantidad(faker.number().numberBetween(1, 5));
        return dto;
    }

    public static Pedido unPedido(LibroDTO libro, String cliente, Integer cantidad) {
        Pedido p = new Pedido();
        p.setId(faker.number().numberBetween(1L, 999L));
        p.setLibroId(libro.getId());
        p.setTituloLibro(libro.getTitulo());
        p.setPrecioUnitario(libro.getPrecio());
        p.setCliente(cliente);
        p.setCantidad(cantidad);
        p.setFechaPedido(LocalDateTime.now());
        return p;
    }

    private static BigDecimal precio() {
        return BigDecimal.valueOf(faker.number().randomDouble(2, 5000, 80000))
                .setScale(2, RoundingMode.HALF_UP);
    }


}
