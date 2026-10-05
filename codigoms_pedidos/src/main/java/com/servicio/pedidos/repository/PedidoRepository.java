package com.servicio.pedidos.repository;


import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.servicio.pedidos.model.Pedido;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    List<Pedido> findByCliente(String cliente);
}
