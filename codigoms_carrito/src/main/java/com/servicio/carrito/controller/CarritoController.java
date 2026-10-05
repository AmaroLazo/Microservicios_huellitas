package com.servicio.carrito.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.servicio.carrito.dto.CarritoResponseDTO;
import com.servicio.carrito.dto.ItemCarritoRequestDTO;
import com.servicio.carrito.service.CarritoService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/carritos")
@RequiredArgsConstructor
public class CarritoController {

    private final CarritoService carritoService;

    @PostMapping
    public ResponseEntity<CarritoResponseDTO> crear(@RequestParam String usuario) {
        return ResponseEntity.status(201).body(carritoService.crear(usuario));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CarritoResponseDTO> obtenerPorId(@PathVariable Long id) {
        return carritoService.obtenerPorId(id).map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<CarritoResponseDTO>> obtenerPorUsuario(@RequestParam String usuario) {
        return ResponseEntity.ok(carritoService.obtenerPorUsuario(usuario));
    }

    @PostMapping("/{id}/items")
    public ResponseEntity<CarritoResponseDTO> agregarItem(@PathVariable Long id,@Valid @RequestBody ItemCarritoRequestDTO dto) {
        return ResponseEntity.ok(carritoService.agregarItem(id, dto));
    }

    @DeleteMapping("/{id}/items/{itemId}")
    public ResponseEntity<CarritoResponseDTO> quitarItem(@PathVariable Long id,@PathVariable Long itemId) {
        return ResponseEntity.ok(carritoService.quitarItem(id, itemId));
    }

    @DeleteMapping("/{id}/vaciar")
    public ResponseEntity<CarritoResponseDTO> vaciar(@PathVariable Long id) {
        return ResponseEntity.ok(carritoService.vaciar(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (carritoService.obtenerPorId(id).isEmpty()) return ResponseEntity.notFound().build();
        carritoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
