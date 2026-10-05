package com.example.codigosms_boleta.client;
import com.example.codigosms_boleta.model.BoletaCompra;
import com.example.codigosms_boleta.model.MetodoPago;
import com.example.codigosms_boleta.repository.BoletaCompraRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Slf4j
@Component
@RequiredArgsConstructor
public class BoletaDataInitializer implements CommandLineRunner {

    private final BoletaCompraRepository boletaCompraRepository;


    @Override
    public void run(String... args) {
        if (boletaCompraRepository.count() > 0) {
            log.info(">>> DataInitializer: boletas ya existentes, se omite carga");
            return;
        }

        boletaCompraRepository.save(BoletaCompra.builder()
                .carritoId(1L)
                .usuario("PepitoPro")
                .montoTotal(new BigDecimal("45.99"))
                .metodoPago(MetodoPago.EFECTIVO)
                .build());

        boletaCompraRepository.save(BoletaCompra.builder()
                .carritoId(2L)
                .usuario("Panchosky")
                .montoTotal(new BigDecimal("91.98"))
                .metodoPago(MetodoPago.TRANSFERENCIA)
                .build());

        log.info(">>> DataInitializer: Boletas de prueba añadidas correctamente");
    }





}
