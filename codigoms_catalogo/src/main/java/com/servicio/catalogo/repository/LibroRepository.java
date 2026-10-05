package com.servicio.catalogo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.servicio.catalogo.model.Libro;

public interface LibroRepository extends JpaRepository<Libro, Long> {
    List<Libro> findByDisponibleTrue();
    List<Libro> findByCategoriaNombreIgnoreCase(String categoria);
}
