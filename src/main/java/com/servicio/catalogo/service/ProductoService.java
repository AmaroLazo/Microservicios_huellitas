package com.servicio.catalogo.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.springframework.stereotype.Service;

import com.servicio.catalogo.dto.ProductoRequestDTO;
import com.servicio.catalogo.dto.ProductoResponseDTO;
import com.servicio.catalogo.model.Categoria;
import com.servicio.catalogo.model.Producto;
import com.servicio.catalogo.repository.CategoriaRepository;
import com.servicio.catalogo.repository.ProductoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;

    public List<ProductoResponseDTO> obtenerTodos() {
        return productoRepository.findAll().stream().map(this::toDTO).toList();
    }

    public ProductoResponseDTO obtenerPorId(Long id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con id: " + id));
        return toDTO(producto);
    }

    public List<ProductoResponseDTO> obtenerDisponibles() {
        return productoRepository.findByActivoTrue().stream().map(this::toDTO).toList();
    }

    public List<ProductoResponseDTO> obtenerPorCategoria(String categoriaNombre) {
        return productoRepository.findByCategoria_NombreIgnoreCaseAndActivoTrue(categoriaNombre)
                .stream().map(this::toDTO).toList();
    }

    public List<ProductoResponseDTO> buscarPorNombre(String nombre) {
        return productoRepository.findByNombreContainingIgnoreCaseAndActivoTrue(nombre)
                .stream().map(this::toDTO).toList();
    }

    public ProductoResponseDTO crear(ProductoRequestDTO request) {
        if (productoRepository.existsByCodigoIgnoreCase(request.getCodigo())) {
            throw new RuntimeException("Ya existe un producto con el código: " + request.getCodigo());
        }
        Categoria categoria = categoriaRepository.findById(request.getCategoriaId())
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada con id: " + request.getCategoriaId()));
        if (!categoria.isActiva()) {
            throw new RuntimeException("No se puede asociar a una categoría inactiva: " + categoria.getNombre());
        }

        Producto producto = new Producto();
        producto.setCodigo(request.getCodigo());
        producto.setNombre(request.getNombre());
        producto.setDescripcion(request.getDescripcion());
        producto.setPrecio(request.getPrecio());
        producto.setPorcentajeDescuento(
                request.getPorcentajeDescuento() != null ? request.getPorcentajeDescuento() : BigDecimal.ZERO
        );
        producto.setCategoria(categoria);
        producto.setTipoMascota(request.getTipoMascota());
        producto.setStock(request.getStock());
        producto.setImagenUrl(request.getImagenUrl());
        producto.setActivo(true);

        return toDTO(productoRepository.save(producto));
    }

    // PENDIENTE (Fase 1): hoy borra físico sin restricciones. Según la
    // sección 5 del prompt, debe migrar a: desactivar por defecto, y
    // eliminar solo si no tiene movimientos de stock ni referencias.
    public void eliminar(Long id) {
        if (!productoRepository.existsById(id)) {
            throw new RuntimeException("Producto no encontrado con id: " + id);
        }
        productoRepository.deleteById(id);
    }

    private ProductoResponseDTO toDTO(Producto p) {
        ProductoResponseDTO dto = new ProductoResponseDTO();
        dto.setId(p.getId());
        dto.setCodigo(p.getCodigo());
        dto.setNombre(p.getNombre());
        dto.setDescripcion(p.getDescripcion());
        dto.setPrecio(p.getPrecio());

        BigDecimal descuento = p.getPorcentajeDescuento() != null ? p.getPorcentajeDescuento() : BigDecimal.ZERO;
        dto.setPorcentajeDescuento(descuento);
        dto.setEnOferta(descuento.compareTo(BigDecimal.ZERO) > 0);

        // precioFinal = precio - (precio * descuento / 100), redondeado a 2 decimales
        BigDecimal factor = BigDecimal.ONE.subtract(descuento.divide(BigDecimal.valueOf(100)));
        BigDecimal precioFinal = p.getPrecio().multiply(factor).setScale(2, RoundingMode.HALF_UP);
        dto.setPrecioFinal(precioFinal);

        dto.setCategoriaId(p.getCategoria().getId());
        dto.setCategoriaNombre(p.getCategoria().getNombre());
        dto.setTipoMascota(p.getTipoMascota());
        dto.setStock(p.getStock());
        dto.setActivo(p.isActivo());
        dto.setDisponible(p.isActivo() && p.getStock() != null && p.getStock() > 0);
        dto.setImagenUrl(p.getImagenUrl() != null ? p.getImagenUrl() : "/images/producto-default.png");
        return dto;
    }
}