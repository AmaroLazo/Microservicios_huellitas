package com.servicio.product.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.servicio.product.model.StockMovement;

public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {
}
