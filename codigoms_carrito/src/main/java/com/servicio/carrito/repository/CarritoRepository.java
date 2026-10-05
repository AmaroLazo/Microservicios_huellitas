package com.servicio.carrito.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.servicio.carrito.model.Carrito;


public interface CarritoRepository extends JpaRepository<Carrito, Long> {
    List<Carrito> findByUsuario(String usuario);
}
