package cl.deyfer.tcg.controller;

import cl.deyfer.tcg.dto.ProductoTcgRequest;
import cl.deyfer.tcg.dto.ProductoTcgResponse;
import cl.deyfer.tcg.service.ProductoTcgService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/productos")
@RequiredArgsConstructor
@Tag(name = "Productos TCG", description = "CRUD de productos y cartas Pokémon TCG")
public class ProductoTcgController {

    private final ProductoTcgService productoService;

    @GetMapping
    @Operation(summary = "Listar todos los productos")
    public List<ProductoTcgResponse> findAll() {
        return productoService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar un producto por id")
    public ProductoTcgResponse findById(@PathVariable Long id) {
        return productoService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear un producto nuevo")
    public ProductoTcgResponse create(@Valid @RequestBody ProductoTcgRequest request) {
        return productoService.create(request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar un producto existente")
    public ProductoTcgResponse update(@PathVariable Long id, @Valid @RequestBody ProductoTcgRequest request) {
        return productoService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar un producto")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        productoService.delete(id);
        return ResponseEntity.noContent().build();
    }
}