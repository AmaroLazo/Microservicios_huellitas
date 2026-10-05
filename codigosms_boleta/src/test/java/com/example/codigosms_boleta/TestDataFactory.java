package com.example.codigosms_boleta;

import com.example.codigosms_boleta.dto.BoletaCompraRequest;
import com.example.codigosms_boleta.dto.CarritoDTO;
import com.example.codigosms_boleta.dto.ItemCarritoDTO;
import com.example.codigosms_boleta.model.BoletaCompra;
import com.example.codigosms_boleta.model.EstadoCompra;
import com.example.codigosms_boleta.model.MetodoPago;
import net.datafaker.Faker;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
public class TestDataFactory {
private static final Faker faker = new Faker();

    public static ItemCarritoDTO unItemCarritoDTO(Long libroId, double precio, int cantidad) {
        return ItemCarritoDTO.builder()
                .id(faker.number().randomNumber())
                .libroId(libroId)
                .tituloLibro(faker.book().title())
                .precioUnitario(BigDecimal.valueOf(precio))
                .cantidad(cantidad)
                .build();
    }

    public static CarritoDTO unCarritoDTO(Long id, String usuario, List<ItemCarritoDTO> items) {
        BigDecimal montoTotal = items.stream()
                .map(item -> item.getPrecioUnitario().multiply(BigDecimal.valueOf(item.getCantidad())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return CarritoDTO.builder()
                .id(id)
                .usuario(usuario)
                .fechaCreacion(LocalDateTime.now())
                .items(items)
                .montoTotal(montoTotal)
                .build();
    }

    public static BoletaCompraRequest unaBoletaCompraRequest(Long carritoId, String usuario) {
        return BoletaCompraRequest.builder()
                .carritoId(carritoId)
                .usuario(usuario)
                .metodoPago(MetodoPago.TRANSFERENCIA)
                .build();
    }

    public static BoletaCompra unaBoletaCompra(Long carritoId, String usuario, BigDecimal montoTotal) {
        return BoletaCompra.builder()
                .id(faker.number().randomNumber())
                .carritoId(carritoId)
                .usuario(usuario)
                .montoTotal(montoTotal)
                .metodoPago(MetodoPago.TRANSFERENCIA)
                .estado(EstadoCompra.COMPLETADA)
                .fechaCompra(LocalDateTime.now())
                .build();
    }


}
