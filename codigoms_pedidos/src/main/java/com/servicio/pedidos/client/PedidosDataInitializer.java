package com.servicio.pedidos.client;

import com.servicio.pedidos.model.Pedido;
import com.servicio.pedidos.repository.PedidoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PedidosDataInitializer implements CommandLineRunner {

    private final PedidoRepository pedidoRepository;

    @Override
    public void run(String... args) {
        if (pedidoRepository.count() > 0) {
            log.info(">>> DataInitializer: pedidos ya existentes, se omite carga");
            return;
        }

        pedidoRepository.save(Pedido.builder()
                .libroId(1L)
                .cliente("Panchosky")
                .cantidad(10)
                .build());

        pedidoRepository.save(Pedido.builder()
                .libroId(2L)
                .cliente("PepitoPro")
                .cantidad(100)
                .build());

        log.info(">>> DataInitializer: Pedidos de prueba añadidos correctamente");
    }





}
