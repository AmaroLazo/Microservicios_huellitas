package com.servicio.catalogo;
import net.datafaker.Faker;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.servicio.catalogo.dto.LibroRequestDTO;
import com.servicio.catalogo.dto.LibroResponseDTO;
import com.servicio.catalogo.model.Libro;
import com.servicio.catalogo.repository.LibroRepository;
import com.servicio.catalogo.service.LibroService;

@ExtendWith(MockitoExtension.class)
class LibroServiceTest {

    @Mock
    private LibroRepository libroRepository;

    @InjectMocks
    private LibroService libroService;

    private final Faker faker = new Faker();
    private Libro libroMock;

    @BeforeEach
    void setUp() {
        libroMock = new Libro();
        libroMock.setId(1L);
        libroMock.setTitulo(faker.book().title());
        libroMock.setAutor(faker.book().author());
        libroMock.setIsbn("978-" + faker.number().digits(10));
        libroMock.setPrecio(BigDecimal.valueOf(faker.number().randomDouble(2, 5, 50)));
        libroMock.setCategoriaNombre(faker.book().genre());
        libroMock.setDisponible(true);
    }

    @Test
    void obtenerTodos_retornaListaCompleta() {
        when(libroRepository.findAll()).thenReturn(List.of(libroMock));

        List<LibroResponseDTO> result = libroService.obtenerTodos();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitulo()).isEqualTo(libroMock.getTitulo());
        verify(libroRepository).findAll();
    }

    @Test
    void obtenerPorId_libroExiste_retornaDTO() {
        when(libroRepository.findById(1L)).thenReturn(Optional.of(libroMock));

        LibroResponseDTO result = libroService.obtenerPorId(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitulo()).isEqualTo(libroMock.getTitulo());
        assertThat(result.getPrecio()).isEqualTo(libroMock.getPrecio());
    }

    @Test
    void obtenerPorId_libroNoExiste_lanzaExcepcion() {
        when(libroRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> libroService.obtenerPorId(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("99");
    }

    @Test
    void obtenerDisponibles_retornarSoloDisponibles() {
        when(libroRepository.findByDisponibleTrue()).thenReturn(List.of(libroMock));

        List<LibroResponseDTO> result = libroService.obtenerDisponibles();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).isDisponible()).isTrue();
    }

    @Test
    void crear_datosValidos_retornaLibroCreado() {
        LibroRequestDTO request = new LibroRequestDTO();
        request.setTitulo(libroMock.getTitulo());
        request.setAutor(libroMock.getAutor());
        request.setIsbn(libroMock.getIsbn());
        request.setPrecio(libroMock.getPrecio());
        request.setCategoriaNombre(libroMock.getCategoriaNombre());

        when(libroRepository.save(any(Libro.class))).thenReturn(libroMock);

        LibroResponseDTO result = libroService.crear(request);

        assertThat(result.getTitulo()).isEqualTo(request.getTitulo());
        assertThat(result.isDisponible()).isTrue();
        verify(libroRepository).save(any(Libro.class));
    }

    @Test
    void eliminar_libroExiste_eliminaCorrectamente() {
        when(libroRepository.existsById(1L)).thenReturn(true);

        libroService.eliminar(1L);

        verify(libroRepository).deleteById(1L);
    }

    @Test
    void eliminar_libroNoExiste_lanzaExcepcion() {
        when(libroRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> libroService.eliminar(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("99");

        verify(libroRepository, never()).deleteById(any());
    }
}
