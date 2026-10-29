package cl.deyfer.tcg.controller;

import cl.deyfer.tcg.dto.ProductoTcgRequest;
import cl.deyfer.tcg.dto.ProductoTcgResponse;
import cl.deyfer.tcg.service.ProductoTcgService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductoTcgControllerTest {

    @Mock
    private ProductoTcgService productoService;

    @InjectMocks
    private ProductoTcgController productoController;

    private ProductoTcgResponse response;
    private ProductoTcgRequest request;

    @BeforeEach
    void setUp() {
        response = ProductoTcgResponse.builder()
                .id(1L)
                .sku("PKM-MEW-151-UPC")
                .nombre("Ultra Premium Collection Pokémon 151")
                .setColeccion("Scarlet & Violet 151")
                .categoria("SELLADO")
                .idioma("INGLES")
                .precio(129990.0)
                .stock(6)
                .disponible(true)
                .build();

        request = new ProductoTcgRequest();
        request.setSku("PKM-MEW-151-UPC");
        request.setNombre("Ultra Premium Collection Pokémon 151");
        request.setSetColeccion("Scarlet & Violet 151");
        request.setCategoria("SELLADO");
        request.setIdioma("INGLES");
        request.setPrecio(129990.0);
        request.setStock(6);
        request.setDisponible(true);
    }

    @Test
    @DisplayName("GET /api/v1/productos debe invocar el servicio y retornar lista")
    void testFindAll() {
        when(productoService.findAll()).thenReturn(List.of(response));

        List<ProductoTcgResponse> resultado = productoController.findAll();

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("PKM-MEW-151-UPC", resultado.get(0).getSku());
        verify(productoService, times(1)).findAll();
    }

    @Test
    @DisplayName("GET /api/v1/productos/{id} debe retornar el producto buscado")
    void testFindById() {
        when(productoService.findById(1L)).thenReturn(response);

        ProductoTcgResponse resultado = productoController.findById(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("PKM-MEW-151-UPC", resultado.getSku());
        verify(productoService, times(1)).findById(1L);
    }

    @Test
    @DisplayName("POST /api/v1/productos debe delegar al servicio y crear el producto")
    void testCreate() {
        when(productoService.create(request)).thenReturn(response);

        ProductoTcgResponse resultado = productoController.create(request);

        assertNotNull(resultado);
        assertEquals("PKM-MEW-151-UPC", resultado.getSku());
        verify(productoService, times(1)).create(request);
    }

    @Test
    @DisplayName("PUT /api/v1/productos/{id} debe delegar al servicio y actualizar")
    void testUpdate() {
        when(productoService.update(1L, request)).thenReturn(response);

        ProductoTcgResponse resultado = productoController.update(1L, request);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        verify(productoService, times(1)).update(1L, request);
    }

    @Test
    @DisplayName("DELETE /api/v1/productos/{id} debe invocar delete y retornar 204 No Content")
    void testDelete() {
        doNothing().when(productoService).delete(1L);

        ResponseEntity<Void> res = productoController.delete(1L);

        assertEquals(HttpStatus.NO_CONTENT, res.getStatusCode());
        assertNull(res.getBody());
        verify(productoService, times(1)).delete(1L);
    }
}