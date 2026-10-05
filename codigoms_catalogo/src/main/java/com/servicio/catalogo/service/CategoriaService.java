package com.servicio.catalogo.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.servicio.catalogo.model.Categoria;
import com.servicio.catalogo.repository.CategoriaRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    public List<Categoria> obtenerTodas() { 
        return categoriaRepository.findAll(); 
    }

    public Optional<Categoria> obtenerPorId(Long id) { 
        return categoriaRepository.findById(id); 
    }

    public Categoria guardar(Categoria c) { 
        return categoriaRepository.save(c); 
    }
    public void eliminar(Long id) { 
        categoriaRepository.deleteById(id); 
    }
}
