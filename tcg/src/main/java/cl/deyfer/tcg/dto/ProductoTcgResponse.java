package cl.deyfer.tcg.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductoTcgResponse {

    private Long id;
    private String sku;
    private String nombre;
    private String setColeccion;
    private String categoria;
    private String idioma;
    private Double precio;
    private Integer stock;
    private Boolean disponible;
}