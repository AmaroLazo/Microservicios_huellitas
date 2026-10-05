package com.servicio.catalogo.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;


import com.servicio.catalogo.dto.LibroRequestDTO;
import com.servicio.catalogo.dto.LibroResponseDTO;
import com.servicio.catalogo.service.LibroService;



@RestController
@RequestMapping("/api/libros")
@RequiredArgsConstructor
public class LibroController {

    private final LibroService libroService;

    @GetMapping
    public List<LibroResponseDTO> obtenerTodos() {
        return libroService.obtenerTodos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<LibroResponseDTO> obtenerPorId(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(libroService.obtenerPorId(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/disponibles")
    public List<LibroResponseDTO> obtenerDisponibles() {
        return libroService.obtenerDisponibles();
    }

    @GetMapping("/categoria/{categoria}")
    public List<LibroResponseDTO> obtenerPorCategoria(@PathVariable String categoria) {
        return libroService.obtenerPorCategoria(categoria);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LibroResponseDTO crear(@Valid @RequestBody LibroRequestDTO request) {
        return libroService.crear(request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        libroService.eliminar(id);
    }
}
