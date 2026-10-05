package com.example.codigosms_usuario;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.example.codigosms_usuario.dto.UsuarioRequest;
import com.example.codigosms_usuario.dto.UsuarioResponse;
import com.example.codigosms_usuario.model.Usuario;
import com.example.codigosms_usuario.repository.UsuarioRepository;
import com.example.codigosms_usuario.service.UsuarioService;

import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
@DisplayName("UsuarioService - Pruebas Unitarias")
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private UsuarioService usuarioService;

    @Test
    @DisplayName("crearUsuario: guarda el usuario correctamente y retorna su DTO")
    void crearUsuario_datosValidos_guardaYRetornaUsuario() {

        UsuarioRequest request = TestDataFactory.unUsuarioRequest("PepitoPro", "Pedro Pérez", "12345678-9");
        Usuario usuarioGuardado = TestDataFactory.unUsuario("PepitoPro", "Pedro Pérez", "12345678-9");

        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioGuardado);

 
        UsuarioResponse resultado = usuarioService.crearUsuario(request);


        assertThat(resultado).isNotNull();
        assertThat(resultado.getUsuarioId()).isEqualTo(usuarioGuardado.getId());
        assertThat(resultado.getNombreUsuario()).isEqualTo("PepitoPro");
        assertThat(resultado.getNombreReal()).isEqualTo("Pedro Pérez");
        assertThat(resultado.getRut()).isEqualTo("12345678-9");
        assertThat(resultado.getFechaCreacion()).isNotNull();

        verify(usuarioRepository).save(any(Usuario.class));
    }

    @Test
    @DisplayName("listarUsuarios: retorna una página con los usuarios existentes mapeados")
    void listarUsuarios_retornaPaginaDeUsuariosMapeados() {

        Pageable pageable = PageRequest.of(0, 10);
        Usuario u1 = TestDataFactory.unUsuario("PepitoPro", "Pedro Pérez", "12345678-9");
        Usuario u2 = TestDataFactory.unUsuario("Panchosky", "Francisco Adrovez", "21469836-6");
        Page<Usuario> paginaMock = new PageImpl<>(List.of(u1, u2), pageable, 2);

        when(usuarioRepository.findAll(pageable)).thenReturn(paginaMock);


        Page<UsuarioResponse> resultado = usuarioService.listarUsuarios(pageable);

  
        assertThat(resultado).hasSize(2);
        assertThat(resultado.getContent()).extracting(UsuarioResponse::getNombreUsuario)
                .containsExactly("PepitoPro", "Panchosky");
        verify(usuarioRepository).findAll(pageable);
    }

    @Test
    @DisplayName("obtenerUsuarioPorId: retorna el DTO del usuario cuando el ID existe")
    void obtenerUsuarioPorId_idExistente_retornaUsuarioResponse() {

        Long idUsuario = 1L;
        Usuario usuario = TestDataFactory.unUsuario("Panchosky", "Francisco Adrovez", "21469836-6");
        usuario.setId(idUsuario);

        when(usuarioRepository.findById(idUsuario)).thenReturn(Optional.of(usuario));


        UsuarioResponse resultado = usuarioService.obtenerUsuarioPorId(idUsuario);

 
        assertThat(resultado).isNotNull();
        assertThat(resultado.getUsuarioId()).isEqualTo(idUsuario);
        assertThat(resultado.getNombreUsuario()).isEqualTo("Panchosky");
        verify(usuarioRepository).findById(idUsuario);
    }

    @Test
    @DisplayName("obtenerUsuarioPorId: lanza EntityNotFoundException si el ID no existe")
    void obtenerUsuarioPorId_idInexistente_lanzaEntityNotFoundException() {

        Long idInexistente = 99L;
        when(usuarioRepository.findById(idInexistente)).thenReturn(Optional.empty());


        assertThatThrownBy(() -> usuarioService.obtenerUsuarioPorId(idInexistente))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Anuncio con ID " + idInexistente + " no fue encontrado");
    }

    @Test
    @DisplayName("eliminarUsuario: elimina físicamente el registro cuando el ID existe")
    void eliminarUsuario_idExistente_eliminaCorrectamente() {
  
        Long idEliminar = 1L;
        when(usuarioRepository.existsById(idEliminar)).thenReturn(true);
        doNothing().when(usuarioRepository).deleteById(idEliminar);


        usuarioService.eliminarUsuario(idEliminar);

  
        verify(usuarioRepository).existsById(idEliminar);
        verify(usuarioRepository).deleteById(idEliminar);
    }

    @Test
    @DisplayName("eliminarUsuario: lanza EntityNotFoundException si el usuario a eliminar no existe")
    void eliminarUsuario_idInexistente_lanzaEntityNotFoundException() {
  
        Long idInexistente = 404L;
        when(usuarioRepository.existsById(idInexistente)).thenReturn(false);

      
        assertThatThrownBy(() -> usuarioService.eliminarUsuario(idInexistente))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("No se puede eliminar: El anuncio con ID " + idInexistente + " no existe");

        verify(usuarioRepository, never()).deleteById(anyLong());
    }
}