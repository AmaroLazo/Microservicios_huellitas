package com.servicio.catalogo.service;

import java.util.List;


import org.springframework.stereotype.Service;

import com.servicio.catalogo.dto.LibroRequestDTO;
import com.servicio.catalogo.dto.LibroResponseDTO;

import com.servicio.catalogo.model.Libro;

import com.servicio.catalogo.repository.LibroRepository;

import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class LibroService {

    private final LibroRepository libroRepository;
    

    public List<LibroResponseDTO> obtenerTodos() {
        return libroRepository.findAll().stream()
                .map(this::toDTO)
                .toList();
    }

    public LibroResponseDTO obtenerPorId(Long id) {
        Libro libro = libroRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Libro no encontrado con id: " + id));
        return toDTO(libro);
    }

    public List<LibroResponseDTO> obtenerDisponibles() {
        return libroRepository.findByDisponibleTrue().stream()
                .map(this::toDTO)
                .toList();
    }

    public List<LibroResponseDTO> obtenerPorCategoria(String categoria) {
        return libroRepository.findByCategoriaNombreIgnoreCase(categoria).stream()
                .map(this::toDTO)
                .toList();
    }

    public LibroResponseDTO crear(LibroRequestDTO request) {
        Libro libro = new Libro();
        libro.setTitulo(request.getTitulo());
        libro.setAutor(request.getAutor());
        libro.setIsbn(request.getIsbn());
        libro.setPrecio(request.getPrecio());
        libro.setCategoriaNombre(request.getCategoriaNombre());
        libro.setDisponible(true);
        return toDTO(libroRepository.save(libro));
    }

    public void eliminar(Long id) {
        if (!libroRepository.existsById(id)) {
            throw new RuntimeException("Libro no encontrado con id: " + id);
        }
        libroRepository.deleteById(id);
    }

    private LibroResponseDTO toDTO(Libro libro) {
        LibroResponseDTO dto = new LibroResponseDTO();
        dto.setId(libro.getId());
        dto.setTitulo(libro.getTitulo());
        dto.setAutor(libro.getAutor());
        dto.setIsbn(libro.getIsbn());
        dto.setPrecio(libro.getPrecio());
        dto.setCategoriaNombre(libro.getCategoriaNombre());
        dto.setDisponible(libro.isDisponible());
        return dto;
    }
}
