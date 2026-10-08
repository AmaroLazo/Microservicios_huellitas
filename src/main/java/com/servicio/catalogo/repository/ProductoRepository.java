package com.servicio.catalogo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.servicio.catalogo.model.Producto;

public interface ProductoRepository extends JpaRepository<Producto, Long> {
    boolean existsByCodigoIgnoreCase(String codigo);
    List<Producto> findByActivoTrue();
    List<Producto> findByCategoriaIdAndActivoTrue(Long categoriaId);
    List<Producto> findByCategoria_NombreIgnoreCaseAndActivoTrue(String categoriaNombre);
    List<Producto> findByNombreContainingIgnoreCaseAndActivoTrue(String nombre);
}