package com.example.codigosms_boleta;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.example.codigosms_boleta.client.CarritoClient;
import com.example.codigosms_boleta.dto.BoletaCompraRequest;
import com.example.codigosms_boleta.dto.BoletaCompraResponse;
import com.example.codigosms_boleta.dto.CarritoDTO;
import com.example.codigosms_boleta.dto.ItemCarritoDTO;
import com.example.codigosms_boleta.model.BoletaCompra;
import com.example.codigosms_boleta.repository.BoletaCompraRepository;
import com.example.codigosms_boleta.service.BoletaCompraService;

import feign.FeignException;
import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
@DisplayName("BoletaCompraService - Pruebas Unitarias")
class CodigosmsBoletaApplicationTests {
	@Mock
    private BoletaCompraRepository compraRepository;

    @Mock
    private CarritoClient carritoClient;

    @InjectMocks
    private BoletaCompraService boletaCompraService;
	@Test
    @DisplayName("crearBoleta: calcula el total y guarda la boleta correctamente cuando el carrito existe")
    void crearBoleta_carritoValido_guardaBoletaYCalculaTotal() {
        // Arrange
        String nombreUsuario = "AmaroUsuario";
        Long carritoId = 15L;

        ItemCarritoDTO item1 = TestDataFactory.unItemCarritoDTO(101L, 15000.0, 2); // 30000
        ItemCarritoDTO item2 = TestDataFactory.unItemCarritoDTO(102L, 5000.0, 1);  // 5000
        List<ItemCarritoDTO> items = List.of(item1, item2);
        BigDecimal totalEsperado = new BigDecimal("35000.0");

        CarritoDTO carritoMock = TestDataFactory.unCarritoDTO(carritoId, nombreUsuario, items);
        BoletaCompraRequest request = TestDataFactory.unaBoletaCompraRequest(carritoId, nombreUsuario);
        BoletaCompra boletaGuardada = TestDataFactory.unaBoletaCompra(carritoId, nombreUsuario, totalEsperado);

        when(carritoClient.obtenerCarritoPorId(carritoId)).thenReturn(carritoMock);
        when(compraRepository.save(any(BoletaCompra.class))).thenReturn(boletaGuardada);

        // Act
        BoletaCompraResponse resultado = boletaCompraService.crearBoleta(request);

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado.getMontoTotal()).isEqualByComparingTo(totalEsperado);
        assertThat(resultado.getUsuario()).isEqualTo(nombreUsuario);
        assertThat(resultado.getCarritoId()).isEqualTo(carritoId);
        
        verify(carritoClient).obtenerCarritoPorId(carritoId);
        verify(compraRepository).save(any(BoletaCompra.class));
    }

    @Test
    @DisplayName("crearBoleta: lanza excepción cuando el cliente Feign no encuentra el carrito")
    void crearBoleta_carritoNoExiste_lanzaFeignException() {
    
        Long carritoId = 999L;
        BoletaCompraRequest request = TestDataFactory.unaBoletaCompraRequest(carritoId, "usuarioTest");

     
        FeignException.NotFound feignNotFound = mock(FeignException.NotFound.class);
        when(carritoClient.obtenerCarritoPorId(carritoId)).thenThrow(feignNotFound);

        assertThatThrownBy(() -> boletaCompraService.crearBoleta(request))
                .isInstanceOf(FeignException.NotFound.class);

        verify(compraRepository, never()).save(any(BoletaCompra.class));
    }

    @Test
    @DisplayName("listarBoletas: retorna una página vacía o con datos mapeados a DTO")
    void listarBoletas_retornaPaginaDeBoletasMapeadas() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        BoletaCompra b1 = TestDataFactory.unaBoletaCompra(1L, "user1", new BigDecimal("100.0"));
        BoletaCompra b2 = TestDataFactory.unaBoletaCompra(2L, "user2", new BigDecimal("200.0"));
        Page<BoletaCompra> paginaMock = new PageImpl<>(List.of(b1, b2), pageable, 2);

        when(compraRepository.findAll(pageable)).thenReturn(paginaMock);

     
        Page<BoletaCompraResponse> resultado = boletaCompraService.listarBoletas(pageable);


        assertThat(resultado).hasSize(2);
        assertThat(resultado.getContent()).extracting(BoletaCompraResponse::getUsuario)
                .containsExactly("user1", "user2");
        verify(compraRepository).findAll(pageable);
    }

    @Test
    @DisplayName("obtenerBoletaPorId: retorna el DTO de la boleta cuando el ID existe")
    void obtenerBoletaPorId_idExistente_retornaBoletaResponse() {
        // Arrange
        Long idBoleta = 1L;
        BoletaCompra boleta = TestDataFactory.unaBoletaCompra(10L, "testUser", new BigDecimal("5000"));
        boleta.setId(idBoleta);

        when(compraRepository.findById(idBoleta)).thenReturn(Optional.of(boleta));


        BoletaCompraResponse resultado = boletaCompraService.obtenerBoletaPorId(idBoleta);


        assertThat(resultado).isNotNull();
        assertThat(resultado.getCompraId()).isEqualTo(idBoleta);
        assertThat(resultado.getUsuario()).isEqualTo("testUser");
        verify(compraRepository).findById(idBoleta);
    }

    @Test
    @DisplayName("obtenerBoletaPorId: lanza EntityNotFoundException si el ID no existe")
    void obtenerBoletaPorId_idInexistente_lanzaEntityNotFoundException() {

        Long idInexistente = 404L;
        when(compraRepository.findById(idInexistente)).thenReturn(Optional.empty());


        assertThatThrownBy(() -> boletaCompraService.obtenerBoletaPorId(idInexistente))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("no fue encontrado");
    }

    @Test
    @DisplayName("eliminarUsuario: ejecuta la eliminación física de la boleta por ID")
    void eliminarUsuario_idValido_llamaAlRepository() {

        Long idEliminar = 5L;
        doNothing().when(compraRepository).deleteById(idEliminar);


        boletaCompraService.eliminarUsuario(idEliminar);


        verify(compraRepository).deleteById(idEliminar);
    }

}
