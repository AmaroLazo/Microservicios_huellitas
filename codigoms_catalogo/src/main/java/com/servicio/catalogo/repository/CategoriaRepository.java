package com.servicio.catalogo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.servicio.catalogo.model.Categoria;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
    boolean existsByNombreIgnoreCase(String nombre);
    List<Categoria> findByActivaTrue();
}