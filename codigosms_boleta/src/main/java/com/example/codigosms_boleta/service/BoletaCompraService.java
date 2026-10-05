package com.example.codigosms_boleta.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.codigosms_boleta.client.CarritoClient;
import com.example.codigosms_boleta.dto.BoletaCompraRequest;
import com.example.codigosms_boleta.dto.BoletaCompraResponse;
import com.example.codigosms_boleta.dto.CarritoDTO;
import com.example.codigosms_boleta.model.BoletaCompra;
import com.example.codigosms_boleta.model.EstadoCompra;
import com.example.codigosms_boleta.repository.BoletaCompraRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor

public class BoletaCompraService {
    private final BoletaCompraRepository compraRepository;
    private final CarritoClient carritoClient;



    
    @Transactional
    public BoletaCompraResponse crearBoleta(BoletaCompraRequest request) {
        log.info("Registrando compra...");
        CarritoDTO carrito = carritoClient.obtenerCarritoPorId(request.getCarritoId());

         BigDecimal totalCalculado = carrito.getItems().stream()
            .map(item -> item.getPrecioUnitario().multiply(new BigDecimal(item.getCantidad())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);


    BoletaCompra nuevaCompra = new BoletaCompra();
    nuevaCompra.setUsuario(request.getUsuario());
    nuevaCompra.setCarritoId(carrito.getId());
    nuevaCompra.setMontoTotal(totalCalculado);
    nuevaCompra.setMetodoPago(request.getMetodoPago());
    nuevaCompra.setEstado(EstadoCompra.COMPLETADA);
    nuevaCompra.setFechaCompra(LocalDateTime.now());

    return mapADTO(compraRepository.save(nuevaCompra));
        
    }

    @Transactional
    public Page<BoletaCompraResponse> listarBoletas(Pageable pageable){
        Page<BoletaCompra> paginaBoleta = compraRepository.findAll(pageable);
        return paginaBoleta.map(this::mapADTO);
    }

    @Transactional
    public BoletaCompraResponse obtenerBoletaPorId(Long id) {
        return compraRepository.findById(id)
                .map(this::mapADTO)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException(
                    "Anuncio con ID " + id + " no fue encontrado"
                ));
    }


    public void eliminarUsuario(Long id) {
        compraRepository.deleteById(id);
       
    }

    
private BoletaCompraResponse mapADTO(BoletaCompra compra){
 BoletaCompraResponse response = new BoletaCompraResponse();
    response.setCompraId(compra.getId());
    response.setUsuario(compra.getUsuario());
    response.setMontoTotal(compra.getMontoTotal());
    response.setEstado(compra.getEstado().name());
    response.setFechaCompra(compra.getFechaCompra());
    
    response.setCarritoId(compra.getCarritoId()); 
    
    return response;






}
}

