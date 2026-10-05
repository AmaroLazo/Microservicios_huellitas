package com.servicio.catalogo.repository;


import org.springframework.data.jpa.repository.JpaRepository;

import com.servicio.catalogo.model.Categoria;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
}
