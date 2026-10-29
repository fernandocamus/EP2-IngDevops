package cl.deyfer.tcg.service;

import cl.deyfer.tcg.dto.ProductoTcgRequest;
import cl.deyfer.tcg.dto.ProductoTcgResponse;
import cl.deyfer.tcg.model.ProductoTcg;
import cl.deyfer.tcg.repository.ProductoTcgRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductoTcgService {

    private final ProductoTcgRepository productoRepository;

    public List<ProductoTcgResponse> findAll() {
        return productoRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public ProductoTcgResponse findById(Long id) {
        return toResponse(buscarOFallar(id));
    }

    public ProductoTcgResponse create(ProductoTcgRequest request) {
        if (productoRepository.existsBySku(request.getSku())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Ya existe un producto con el SKU " + request.getSku());
        }

        ProductoTcg producto = ProductoTcg.builder()
                .sku(request.getSku())
                .nombre(request.getNombre())
                .setColeccion(request.getSetColeccion())
                .categoria(request.getCategoria())
                .idioma(request.getIdioma())
                .precio(request.getPrecio())
                .stock(request.getStock())
                .disponible(request.getDisponible())
                .build();

        return toResponse(productoRepository.save(producto));
    }

    public ProductoTcgResponse update(Long id, ProductoTcgRequest request) {
        ProductoTcg producto = buscarOFallar(id);

        productoRepository.findBySku(request.getSku())
                .filter(otro -> !otro.getId().equals(id))
                .ifPresent(otro -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT,
                            "Ya existe un producto con el SKU " + request.getSku());
                });

        producto.setSku(request.getSku());
        producto.setNombre(request.getNombre());
        producto.setSetColeccion(request.getSetColeccion());
        producto.setCategoria(request.getCategoria());
        producto.setIdioma(request.getIdioma());
        producto.setPrecio(request.getPrecio());
        producto.setStock(request.getStock());
        producto.setDisponible(request.getDisponible());

        return toResponse(productoRepository.save(producto));
    }

    public void delete(Long id) {
        ProductoTcg producto = buscarOFallar(id);
        productoRepository.delete(producto);
    }

    private ProductoTcg buscarOFallar(Long id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No se encontró el producto con id " + id));
    }

    private ProductoTcgResponse toResponse(ProductoTcg producto) {
        return ProductoTcgResponse.builder()
                .id(producto.getId())
                .sku(producto.getSku())
                .nombre(producto.getNombre())
                .setColeccion(producto.getSetColeccion())
                .categoria(producto.getCategoria())
                .idioma(producto.getIdioma())
                .precio(producto.getPrecio())
                .stock(producto.getStock())
                .disponible(producto.getDisponible())
                .build();
    }
}