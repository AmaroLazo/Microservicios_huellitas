package com.servicio.carrito.client;

import com.servicio.carrito.model.Carrito;
import com.servicio.carrito.model.ItemCarrito;
import com.servicio.carrito.repository.CarritoRepository;
import com.servicio.carrito.repository.ItemCarritoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Slf4j
@Component
@RequiredArgsConstructor
public class CarritoDataInitializer implements CommandLineRunner {

    private final CarritoRepository carritoRepository;
    private final ItemCarritoRepository itemCarritoRepository;

    @Override
    public void run(String... args) {
        if (carritoRepository.count() > 0) {
            log.info(">>> DataInitializer: carritos ya existentes, se omite carga");
            return;
        }

        Carrito carritoPepito = new Carrito("PepitoPro");
        carritoPepito = carritoRepository.save(carritoPepito);

        ItemCarrito itemPepito = new ItemCarrito(
                1L,
                "Clean Code",
                new BigDecimal("45.99"),
                1,
                carritoPepito
        );
        itemCarritoRepository.save(itemPepito);

        Carrito carritoPanchosky = new Carrito("Panchosky");
        carritoPanchosky = carritoRepository.save(carritoPanchosky);

        ItemCarrito itemPanchosky = new ItemCarrito(
                1L,
                "Clean Code",
                new BigDecimal("45.99"),
                2,
                carritoPanchosky
        );
        itemCarritoRepository.save(itemPanchosky);

        log.info(">>> DataInitializer: Carritos e Items de prueba añadidos correctamente");
    }



}
