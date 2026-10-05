package com.servicio.carrito.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.servicio.carrito.model.ItemCarrito;


public interface ItemCarritoRepository extends JpaRepository<ItemCarrito, Long> {
}
