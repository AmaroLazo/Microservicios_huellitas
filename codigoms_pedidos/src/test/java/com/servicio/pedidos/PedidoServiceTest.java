package com.servicio.pedidos;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.servicio.pedidos.client.CatalogoClient;
import com.servicio.pedidos.client.UsuariosClient;
import com.servicio.pedidos.dto.LibroDTO;
import com.servicio.pedidos.dto.PedidoRequestDTO;
import com.servicio.pedidos.dto.PedidoResponseDTO;
import com.servicio.pedidos.model.Pedido;
import com.servicio.pedidos.repository.PedidoRepository;
import com.servicio.pedidos.service.PedidoService;

import feign.FeignException;


@ExtendWith(MockitoExtension.class)
@DisplayName("PedidoService - Pruebas Unitarias")
class PedidoServiceTest {

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private CatalogoClient catalogoClient;

    @Mock
    private UsuariosClient usuariosClient;

    @InjectMocks
    private PedidoService pedidoService;

    @Test
    @DisplayName("crear: retorna pedido con total calculado cuando libro y cliente son validos")
    void crear_libroYClienteValidos_retornaPedidoConTotal() {
        LibroDTO libro = TestDataFactory.unLibroDTO();
        PedidoRequestDTO request = TestDataFactory.unPedidoRequest(libro.getId());
        Pedido pedidoGuardado = TestDataFactory.unPedido(libro, request.getCliente(), request.getCantidad());

      
        when(usuariosClient.buscarPorNombreUsuario(anyString())).thenReturn(null);
        when(catalogoClient.obtenerLibro(libro.getId())).thenReturn(libro);
        when(pedidoRepository.save(any(Pedido.class))).thenReturn(pedidoGuardado);

        PedidoResponseDTO resultado = pedidoService.crear(request);

        assertThat(resultado.getTituloLibro()).isEqualTo(libro.getTitulo());
        assertThat(resultado.getCliente()).isEqualTo(request.getCliente());
        assertThat(resultado.getTotal()).isPositive();

        assertThat(resultado.getTotal())
                .isEqualByComparingTo(libro.getPrecio().multiply(
                        java.math.BigDecimal.valueOf(request.getCantidad())));
        verify(pedidoRepository).save(any(Pedido.class));
    }

    @Test
    @DisplayName("crear: lanza excepcion cuando el libro no existe en catalogo (FeignException.NotFound)")
    void crear_libroNoExiste_lanzaExcepcion() {
        PedidoRequestDTO request = TestDataFactory.unPedidoRequest(99L);

        when(usuariosClient.buscarPorNombreUsuario(anyString())).thenReturn(null);
        
        when(catalogoClient.obtenerLibro(anyLong()))
                .thenThrow(mock(FeignException.NotFound.class));

        assertThatThrownBy(() -> pedidoService.crear(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("no existe en el catalogo");

        verify(pedidoRepository, never()).save(any());
    }

    @Test
    @DisplayName("crear: lanza excepcion cuando codigoms_catalogo no esta disponible")
    void crear_catalogoNoDisponible_lanzaExcepcion() {
        PedidoRequestDTO request = TestDataFactory.unPedidoRequest(10L);

        when(usuariosClient.buscarPorNombreUsuario(anyString())).thenReturn(null);
      
        when(catalogoClient.obtenerLibro(anyLong()))
                .thenThrow(mock(FeignException.class));

        assertThatThrownBy(() -> pedidoService.crear(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Verifique que codigoms_catalogo");

        verify(pedidoRepository, never()).save(any());
    }

    @Test
    @DisplayName("crear: lanza excepcion cuando el cliente no existe en usuarios (FeignException.NotFound)")
    void crear_clienteNoExiste_lanzaExcepcion() {
      
        PedidoRequestDTO request = TestDataFactory.unPedidoRequest(10L);
        when(usuariosClient.buscarPorNombreUsuario(anyString()))
                .thenThrow(mock(FeignException.NotFound.class));

        assertThatThrownBy(() -> pedidoService.crear(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("no existe en el sistema");


        verify(catalogoClient, never()).obtenerLibro(anyLong());
        verify(pedidoRepository, never()).save(any());
    }

    @Test
    @DisplayName("obtenerTodos: retorna todos los pedidos como DTO")
    void obtenerTodos_retornaListaCompleta() {
        LibroDTO libro = TestDataFactory.unLibroDTO();
        Pedido p1 = TestDataFactory.unPedido(libro, "juan", 1);
        Pedido p2 = TestDataFactory.unPedido(libro, "maria", 2);
        when(pedidoRepository.findAll()).thenReturn(List.of(p1, p2));

        List<PedidoResponseDTO> resultado = pedidoService.obtenerTodos();

        assertThat(resultado).hasSize(2);
        assertThat(resultado).allMatch(p -> p.getTotal().compareTo(java.math.BigDecimal.ZERO) > 0);
        verify(pedidoRepository).findAll();
    }

    @Test
    @DisplayName("obtenerPorId: retorna el pedido cuando el id existe")
    void obtenerPorId_existente_retornaPedido() {
        LibroDTO libro = TestDataFactory.unLibroDTO();
        Pedido pedido = TestDataFactory.unPedido(libro, "juan", 2);
        when(pedidoRepository.findById(pedido.getId())).thenReturn(Optional.of(pedido));

        Optional<PedidoResponseDTO> resultado = pedidoService.obtenerPorId(pedido.getId());

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getTituloLibro()).isEqualTo(libro.getTitulo());
        assertThat(resultado.get().getCliente()).isEqualTo("juan");
    }

    @Test
    @DisplayName("obtenerPorCliente: retorna solo los pedidos del cliente indicado")
    void obtenerPorCliente_retornaSoloPedidosDelCliente() {
        LibroDTO libro = TestDataFactory.unLibroDTO();
        Pedido p1 = TestDataFactory.unPedido(libro, "juan", 1);
        Pedido p2 = TestDataFactory.unPedido(libro, "juan", 3);
        when(pedidoRepository.findByCliente("juan")).thenReturn(List.of(p1, p2));

        List<PedidoResponseDTO> resultado = pedidoService.obtenerPorCliente("juan");

        assertThat(resultado).hasSize(2);
        assertThat(resultado).extracting(PedidoResponseDTO::getCliente)
                .allMatch(c -> c.equals("juan"));
        verify(pedidoRepository).findByCliente("juan");
    }
}