package com.example.codigosms_boleta.model;


import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
@Entity
@Table(name = "compras") 
@Data 
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoletaCompra {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;


    @Column(name = "carrito_id", nullable = false)
    private Long carritoId;
    
    @Column(nullable = false, length = 100)
    private String usuario;

    @Column(name = "fecha_compra", nullable = false)
    private LocalDateTime fechaCompra;

    @Column(nullable = false, columnDefinition = "DECIMAL(10,2)")
    private BigDecimal montoTotal;
    
    
    @Column(length = 20)
    private EstadoCompra estado;

    
    @Column(name = "metodo_pago", length = 30, nullable = false)
    private MetodoPago metodoPago;

    @PrePersist
    protected void onCreate() {
        this.fechaCompra = LocalDateTime.now();
        if (this.estado == null) this.estado = EstadoCompra.PENDIENTE;
    }
}
