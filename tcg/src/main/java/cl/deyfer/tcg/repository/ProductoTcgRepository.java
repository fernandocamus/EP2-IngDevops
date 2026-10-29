package cl.deyfer.tcg.repository;

import cl.deyfer.tcg.model.ProductoTcg;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductoTcgRepository extends JpaRepository<ProductoTcg, Long> {

    boolean existsBySku(String sku);

    Optional<ProductoTcg> findBySku(String sku);
}