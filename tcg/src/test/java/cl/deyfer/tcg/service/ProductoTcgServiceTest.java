package cl.deyfer.tcg.service;

import cl.deyfer.tcg.dto.ProductoTcgRequest;
import cl.deyfer.tcg.dto.ProductoTcgResponse;
import cl.deyfer.tcg.model.ProductoTcg;
import cl.deyfer.tcg.repository.ProductoTcgRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductoTcgServiceTest {

    @Mock
    private ProductoTcgRepository productoRepository;

    @InjectMocks
    private ProductoTcgService productoService;

    private ProductoTcg producto;
    private ProductoTcgRequest request;

    @BeforeEach
    void setUp() {
        producto = ProductoTcg.builder()
                .id(1L)
                .sku("PKM-CRZ-ETB")
                .nombre("Elite Trainer Box Crown Zenith")
                .setColeccion("Crown Zenith")
                .categoria("SELLADO")
                .idioma("INGLES")
                .precio(64990.0)
                .stock(12)
                .disponible(true)
                .build();

        request = new ProductoTcgRequest();
        request.setSku("PKM-CRZ-ETB");
        request.setNombre("Elite Trainer Box Crown Zenith");
        request.setSetColeccion("Crown Zenith");
        request.setCategoria("SELLADO");
        request.setIdioma("INGLES");
        request.setPrecio(64990.0);
        request.setStock(12);
        request.setDisponible(true);
    }

    @Test
    @DisplayName("findAll debe retornar la lista mapeada a ProductoTcgResponse")
    void testFindAll() {
        when(productoRepository.findAll()).thenReturn(List.of(producto));

        List<ProductoTcgResponse> resultado = productoService.findAll();

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("PKM-CRZ-ETB", resultado.get(0).getSku());
        assertEquals("Elite Trainer Box Crown Zenith", resultado.get(0).getNombre());
        verify(productoRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("findById debe retornar el producto cuando existe")
    void testFindByIdExistente() {
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));

        ProductoTcgResponse resultado = productoService.findById(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("PKM-CRZ-ETB", resultado.getSku());
        verify(productoRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("findById debe lanzar NOT_FOUND cuando el ID no existe")
    void testFindByIdNoExistente() {
        when(productoRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> productoService.findById(99L));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        assertTrue(ex.getReason().contains("No se encontró el producto con id 99"));
        verify(productoRepository, times(1)).findById(99L);
    }

    @Test
    @DisplayName("create debe guardar y retornar el producto cuando el SKU no existe")
    void testCreateExitoso() {
        when(productoRepository.existsBySku("PKM-CRZ-ETB")).thenReturn(false);
        when(productoRepository.save(any(ProductoTcg.class))).thenReturn(producto);

        ProductoTcgResponse resultado = productoService.create(request);

        assertNotNull(resultado);
        assertEquals("PKM-CRZ-ETB", resultado.getSku());
        verify(productoRepository, times(1)).existsBySku("PKM-CRZ-ETB");
        verify(productoRepository, times(1)).save(any(ProductoTcg.class));
    }

    @Test
    @DisplayName("create debe lanzar CONFLICT cuando el SKU ya existe")
    void testCreateSkuDuplicado() {
        when(productoRepository.existsBySku("PKM-CRZ-ETB")).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> productoService.create(request));

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        assertTrue(ex.getReason().contains("Ya existe un producto con el SKU"));
        verify(productoRepository, times(1)).existsBySku("PKM-CRZ-ETB");
        verify(productoRepository, never()).save(any(ProductoTcg.class));
    }

    @Test
    @DisplayName("update debe modificar y guardar el producto cuando los datos son válidos")
    void testUpdateExitoso() {
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        when(productoRepository.findBySku("PKM-CRZ-ETB")).thenReturn(Optional.of(producto));
        when(productoRepository.save(any(ProductoTcg.class))).thenReturn(producto);

        ProductoTcgResponse resultado = productoService.update(1L, request);

        assertNotNull(resultado);
        assertEquals("PKM-CRZ-ETB", resultado.getSku());
        verify(productoRepository, times(1)).findById(1L);
        verify(productoRepository, times(1)).save(producto);
    }

    @Test
    @DisplayName("update debe lanzar CONFLICT si otro producto ya usa el mismo SKU")
    void testUpdateSkuConflictoConOtroProducto() {
        ProductoTcg otroProducto = ProductoTcg.builder().id(2L).sku("PKM-CRZ-ETB").build();

        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        when(productoRepository.findBySku("PKM-CRZ-ETB")).thenReturn(Optional.of(otroProducto));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> productoService.update(1L, request));

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        assertTrue(ex.getReason().contains("Ya existe un producto con el SKU"));
        verify(productoRepository, never()).save(producto);
    }

    @Test
    @DisplayName("delete debe eliminar el producto cuando existe")
    void testDeleteExitoso() {
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        doNothing().when(productoRepository).delete(producto);

        assertDoesNotThrow(() -> productoService.delete(1L));

        verify(productoRepository, times(1)).findById(1L);
        verify(productoRepository, times(1)).delete(producto);
    }

    @Test
    @DisplayName("delete debe lanzar NOT_FOUND cuando el ID no existe")
    void testDeleteNoExistente() {
        when(productoRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> productoService.delete(99L));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        verify(productoRepository, times(1)).findById(99L);
        verify(productoRepository, never()).delete(any(ProductoTcg.class));
    }
}