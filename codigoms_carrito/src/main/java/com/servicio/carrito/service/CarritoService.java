package com.servicio.carrito.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.servicio.carrito.client.CatalogoClient;
import com.servicio.carrito.client.UsuariosClient;
import com.servicio.carrito.dto.CarritoResponseDTO;
import com.servicio.carrito.dto.ItemCarritoRequestDTO;
import com.servicio.carrito.dto.ItemCarritoResponseDTO;
import com.servicio.carrito.dto.LibroDTO;
import com.servicio.carrito.model.Carrito;
import com.servicio.carrito.model.ItemCarrito;
import com.servicio.carrito.repository.CarritoRepository;
import com.servicio.carrito.repository.ItemCarritoRepository;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CarritoService {

    private final CarritoRepository carritoRepository;
    private final ItemCarritoRepository itemCarritoRepository;
    private final CatalogoClient catalogoClient;
    private final UsuariosClient usuariosClient;

    @Transactional
    public CarritoResponseDTO crear(String usuario) {
        validarUsuario(usuario);
        return mapToDTO(carritoRepository.save(new Carrito(usuario)));
    }

    public Optional<CarritoResponseDTO> obtenerPorId(Long id) {
        return carritoRepository.findById(id).map(this::mapToDTO);
    }

    public List<CarritoResponseDTO> obtenerPorUsuario(String usuario) {
        return carritoRepository.findByUsuario(usuario).stream().map(this::mapToDTO).toList();
    }

    @Transactional
    public CarritoResponseDTO agregarItem(Long carritoId, ItemCarritoRequestDTO dto) {
        Carrito carrito = carritoRepository.findById(carritoId)
                .orElseThrow(() -> new RuntimeException("Carrito no encontrado: " + carritoId));

        LibroDTO libro = consultarLibro(dto.getLibroId());

        Optional<ItemCarrito> existente = carrito.getItems().stream()
                .filter(i -> i.getLibroId().equals(dto.getLibroId()))
                .findFirst();

        if (existente.isPresent()) {
            existente.get().setCantidad(existente.get().getCantidad() + dto.getCantidad());
        } else {
            carrito.getItems().add(new ItemCarrito(
                    libro.getId(), libro.getTitulo(), libro.getPrecio(),
                    dto.getCantidad(), carrito));
        }

        return mapToDTO(carritoRepository.save(carrito));
    }

    @Transactional
    public CarritoResponseDTO quitarItem(Long carritoId, Long itemId) {
        Carrito carrito = carritoRepository.findById(carritoId)
                .orElseThrow(() -> new RuntimeException("Carrito no encontrado: " + carritoId));
        ItemCarrito item = itemCarritoRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item no encontrado: " + itemId));
        if (!item.getCarrito().getId().equals(carritoId))
            throw new RuntimeException("El item " + itemId + " no pertenece al carrito " + carritoId);
        carrito.getItems().remove(item);
        return mapToDTO(carritoRepository.save(carrito));
    }

    @Transactional
    public CarritoResponseDTO vaciar(Long carritoId) {
        Carrito carrito = carritoRepository.findById(carritoId)
                .orElseThrow(() -> new RuntimeException("Carrito no encontrado: " + carritoId));
        carrito.getItems().clear();
        return mapToDTO(carritoRepository.save(carrito));
    }

    @Transactional
    public void eliminar(Long carritoId) {
        carritoRepository.deleteById(carritoId);
    }

    private void validarUsuario(String usuario) {
        try {
            usuariosClient.buscarPorNombreUsuario(usuario);
        } catch (FeignException.NotFound e) {
            throw new RuntimeException("El usuario '" + usuario + "' no existe en el sistema");
        } catch (FeignException e) {
            log.error("Error al contactar codigoms_usuarios: {}", e.getMessage());
            throw new RuntimeException("No se pudo verificar el usuario. Verifique que codigoms_usuarios este corriendo.");
        }
    }

    private LibroDTO consultarLibro(Long libroId) {
        try {
            return catalogoClient.obtenerLibro(libroId);
        } catch (FeignException.NotFound e) {
            throw new RuntimeException("El libro con id " + libroId + " no existe en el catalogo");
        } catch (FeignException e) {
            log.error("Error al contactar codigoms_catalogo: {}", e.getMessage());
            throw new RuntimeException("No se pudo verificar el libro. Verifique que codigoms_catalogo este corriendo.");
        }
    }

    private CarritoResponseDTO mapToDTO(Carrito c) {
        CarritoResponseDTO dto = new CarritoResponseDTO();
        dto.setId(c.getId());
        dto.setUsuario(c.getUsuario());
        dto.setFechaCreacion(c.getFechaCreacion());

        List<ItemCarritoResponseDTO> items = c.getItems().stream().map(i -> {
            ItemCarritoResponseDTO iDto = new ItemCarritoResponseDTO();
            iDto.setId(i.getId());
            iDto.setLibroId(i.getLibroId());
            iDto.setTituloLibro(i.getTituloLibro());
            iDto.setCantidad(i.getCantidad());
            iDto.setPrecioUnitario(i.getPrecioUnitario());
            iDto.setSubtotal(i.getPrecioUnitario().multiply(BigDecimal.valueOf(i.getCantidad())));
            return iDto;
        }).toList();

        dto.setItems(items);
        dto.setTotal(items.stream()
                .map(ItemCarritoResponseDTO::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        return dto;
    }
}
