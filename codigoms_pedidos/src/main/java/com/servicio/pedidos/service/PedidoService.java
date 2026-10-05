package com.servicio.pedidos.service;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.servicio.pedidos.client.CatalogoClient;
import com.servicio.pedidos.client.UsuariosClient;
import com.servicio.pedidos.dto.LibroDTO;
import com.servicio.pedidos.dto.PedidoRequestDTO;
import com.servicio.pedidos.dto.PedidoResponseDTO;
import com.servicio.pedidos.model.Pedido;
import com.servicio.pedidos.repository.PedidoRepository;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final CatalogoClient catalogoClient;
    private final UsuariosClient usuariosClient;

    @Transactional
    public PedidoResponseDTO crear(PedidoRequestDTO dto) {
        validarCliente(dto.getCliente());
        LibroDTO libro = consultarLibro(dto.getLibroId());

        Pedido pedido = new Pedido();
        pedido.setLibroId(libro.getId());
        pedido.setTituloLibro(libro.getTitulo());
        pedido.setPrecioUnitario(libro.getPrecio());
        pedido.setCliente(dto.getCliente());
        pedido.setCantidad(dto.getCantidad());
        pedido.setFechaPedido(LocalDateTime.now());

        Pedido guardado = pedidoRepository.save(pedido);
        log.info("Pedido creado id={} para libro '{}'", guardado.getId(), libro.getTitulo());
        return mapToDTO(guardado);
    }

    public List<PedidoResponseDTO> obtenerTodos() {
        return pedidoRepository.findAll().stream().map(this::mapToDTO).toList();
    }

    public Optional<PedidoResponseDTO> obtenerPorId(Long id) {
        return pedidoRepository.findById(id).map(this::mapToDTO);
    }

    public List<PedidoResponseDTO> obtenerPorCliente(String cliente) {
        return pedidoRepository.findByCliente(cliente).stream().map(this::mapToDTO).toList();
    }

    @Transactional
    public void eliminar(Long id) {
        pedidoRepository.deleteById(id);
    }

    private void validarCliente(String cliente) {
        try {
            usuariosClient.buscarPorNombreUsuario(cliente);
        } catch (FeignException.NotFound e) {
            throw new RuntimeException("El cliente '" + cliente + "' no existe en el sistema");
        } catch (FeignException e) {
            log.error("Error al contactar codigoms_usuarios: {}", e.getMessage());
            throw new RuntimeException("No se pudo verificar el cliente. Verifique que codigoms_usuarios este corriendo.");
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

    private PedidoResponseDTO mapToDTO(Pedido p) {
        PedidoResponseDTO dto = new PedidoResponseDTO();
        dto.setId(p.getId());
        dto.setLibroId(p.getLibroId());
        dto.setTituloLibro(p.getTituloLibro());
        dto.setPrecioUnitario(p.getPrecioUnitario());
        dto.setCliente(p.getCliente());
        dto.setCantidad(p.getCantidad());
        dto.setFechaPedido(p.getFechaPedido());
        dto.setTotal(p.getPrecioUnitario().multiply(BigDecimal.valueOf(p.getCantidad())));
        return dto;
    }
}
