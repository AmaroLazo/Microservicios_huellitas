package test.java.com.carrito;

import java.math.BigDecimal;
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

import com.servicio.carrito.client.CatalogoClient;
import com.servicio.carrito.client.UsuariosClient;
import com.servicio.carrito.dto.CarritoResponseDTO;
import com.servicio.carrito.dto.ItemCarritoRequestDTO;
import com.servicio.carrito.dto.LibroDTO;
import com.servicio.carrito.model.Carrito;
import com.servicio.carrito.model.ItemCarrito;
import com.servicio.carrito.repository.CarritoRepository;
import com.servicio.carrito.repository.ItemCarritoRepository;
import com.servicio.carrito.service.CarritoService;

import feign.FeignException;

@ExtendWith(MockitoExtension.class)
@DisplayName("CarritoService - Pruebas Unitarias")
public class CarritoServiceTest {

    
    @Mock
    private CarritoRepository carritoRepository;

    @Mock
    private ItemCarritoRepository itemCarritoRepository;

    @Mock
    private CatalogoClient catalogoClient;

    @Mock
    private UsuariosClient usuariosClient;

    @InjectMocks
    private CarritoService carritoService;

    @Test
    @DisplayName("crear: guarda el carrito cuando el usuario existe")
    void crear_usuarioValido_guardaCarrito() {
        Carrito carritoGuardado = test.java.com.carrito.TestDataFactory.unCarrito();
        when(carritoRepository.save(any(Carrito.class))).thenReturn(carritoGuardado);

        CarritoResponseDTO resultado = carritoService.crear(carritoGuardado.getUsuario());

        assertThat(resultado).isNotNull();
        assertThat(resultado.getUsuario()).isEqualTo(carritoGuardado.getUsuario());
        verify(carritoRepository).save(any(Carrito.class));
    }

    @Test
    @DisplayName("crear: lanza excepcion cuando el usuario no existe en el sistema")
    void crear_usuarioNoExiste_lanzaExcepcion() {

        FeignException.NotFound feignException = mock(FeignException.NotFound.class);
        when(usuariosClient.buscarPorNombreUsuario(anyString())).thenThrow(feignException);

        assertThatThrownBy(() -> carritoService.crear("usuarioInexistente"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("no existe en el sistema");

        verify(carritoRepository, never()).save(any());
    }

    @Test
    @DisplayName("agregarItem: agrega un item nuevo cuando el libro no esta en el carrito")
    void agregarItem_libroNuevo_agregaItem() {

        Carrito carrito = new Carrito("juan");
        LibroDTO libro = TestDataFactory.unLibroDTO();
        ItemCarritoRequestDTO request = new ItemCarritoRequestDTO();
        request.setLibroId(libro.getId());
        request.setCantidad(2);

        when(carritoRepository.findById(1L)).thenReturn(Optional.of(carrito));
        when(catalogoClient.obtenerLibro(libro.getId())).thenReturn(libro);
        when(carritoRepository.save(any(Carrito.class))).thenReturn(carrito);

        carritoService.agregarItem(1L, request);

        assertThat(carrito.getItems()).hasSize(1);
        assertThat(carrito.getItems().get(0).getLibroId()).isEqualTo(libro.getId());
        assertThat(carrito.getItems().get(0).getCantidad()).isEqualTo(2);
        verify(carritoRepository).save(carrito);
    }

    @Test
    @DisplayName("agregarItem: incrementa la cantidad cuando el libro ya esta en el carrito")
    void agregarItem_libroYaExiste_incrementaCantidad() {

        Carrito carrito = new Carrito("juan");
        LibroDTO libro = TestDataFactory.unLibroDTO();
        ItemCarrito itemExistente = new ItemCarrito(
                libro.getId(), libro.getTitulo(), libro.getPrecio(), 2, carrito);
        carrito.getItems().add(itemExistente);

    
        ItemCarritoRequestDTO request = new ItemCarritoRequestDTO();
        request.setLibroId(libro.getId());
        request.setCantidad(3);

        when(carritoRepository.findById(1L)).thenReturn(Optional.of(carrito));
        when(catalogoClient.obtenerLibro(libro.getId())).thenReturn(libro);
        when(carritoRepository.save(any(Carrito.class))).thenReturn(carrito);

        carritoService.agregarItem(1L, request);

    
        assertThat(carrito.getItems()).hasSize(1);
    
        assertThat(itemExistente.getCantidad()).isEqualTo(5);
        verify(carritoRepository).save(carrito);
    }

    @Test
    @DisplayName("agregarItem: lanza excepcion cuando el libro no existe en catalogo")
    void agregarItem_libroNoExiste_lanzaExcepcion() {
        Carrito carrito = new Carrito("juan");
        ItemCarritoRequestDTO request = new ItemCarritoRequestDTO();
        request.setLibroId(99L);
        request.setCantidad(1);

        when(carritoRepository.findById(1L)).thenReturn(Optional.of(carrito));
        when(catalogoClient.obtenerLibro(anyLong()))
                .thenThrow(mock(FeignException.NotFound.class));

        assertThatThrownBy(() -> carritoService.agregarItem(1L, request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("no existe en el catalogo");

        verify(carritoRepository, never()).save(any());
    }

    @Test
    @DisplayName("obtenerPorId: retorna el carrito cuando el id existe")
    void obtenerPorId_existente_retornaCarrito() {
        Carrito carrito = new Carrito("pedro");
        when(carritoRepository.findById(1L)).thenReturn(Optional.of(carrito));

        var resultado = carritoService.obtenerPorId(1L);

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getUsuario()).isEqualTo("pedro");
    }

    @Test
    @DisplayName("quitarItem: elimina el item cuando este pertenece al carrito")
    void quitarItem_itemPertenece_eliminaItem() {
        Carrito carrito = new Carrito("juan");
    
        carrito.setId(1L);

        LibroDTO libro = TestDataFactory.unLibroDTO();
        ItemCarrito item = new ItemCarrito(libro.getId(), libro.getTitulo(),
                libro.getPrecio(), 2, carrito);
        item.setId(5L);
        carrito.getItems().add(item);

        when(carritoRepository.findById(1L)).thenReturn(Optional.of(carrito));
        when(itemCarritoRepository.findById(5L)).thenReturn(Optional.of(item));
        when(carritoRepository.save(any(Carrito.class))).thenReturn(carrito);

        carritoService.quitarItem(1L, 5L);


        assertThat(carrito.getItems()).isEmpty();
        verify(carritoRepository).save(carrito);
    }

    @Test
    @DisplayName("vaciar: elimina todos los items del carrito")
    void vaciar_eliminaTodosLosItems() {
     
        Carrito carrito = new Carrito("ana");
        LibroDTO libro = TestDataFactory.unLibroDTO();
        carrito.getItems().add(new ItemCarrito(libro.getId(), libro.getTitulo(),
                libro.getPrecio(), 1, carrito));
        carrito.getItems().add(new ItemCarrito(libro.getId() + 1, "Otro libro",
                new BigDecimal("9900.00"), 2, carrito));

        when(carritoRepository.findById(1L)).thenReturn(Optional.of(carrito));
        when(carritoRepository.save(any(Carrito.class))).thenReturn(carrito);

        carritoService.vaciar(1L);

        assertThat(carrito.getItems()).isEmpty();
        verify(carritoRepository).save(carrito);
    }










}
