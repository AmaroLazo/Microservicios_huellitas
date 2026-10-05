package com.example.codigosms_boleta.controller;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.codigosms_boleta.dto.BoletaCompraRequest;
import com.example.codigosms_boleta.dto.BoletaCompraResponse;
import com.example.codigosms_boleta.service.BoletaCompraService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/boletas")
@RequiredArgsConstructor
public class BoletaCompraController {
private final BoletaCompraService boletaCompraService;

    @PostMapping
    public ResponseEntity<BoletaCompraResponse> crearCompra(@Valid @RequestBody BoletaCompraRequest request) {
        BoletaCompraResponse nuevaBoleta = boletaCompraService.crearBoleta(request);
        return new ResponseEntity<>(nuevaBoleta, HttpStatus.CREATED);
    }    
    
    @GetMapping("/{id}")
    public ResponseEntity<BoletaCompraResponse> obtenerPorId(@PathVariable Long id){
        BoletaCompraResponse response = boletaCompraService.obtenerBoletaPorId(id);
        return ResponseEntity.ok(response);
}

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        boletaCompraService.eliminarUsuario(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/obtener")
    public ResponseEntity<Page<BoletaCompraResponse>> obtenerTodo(Pageable pageable){
        Page<BoletaCompraResponse> v = boletaCompraService.listarBoletas(pageable);
        return ResponseEntity.ok(v);
    
    }



    















}




