package cl.deyfer.tcg.config;

import cl.deyfer.tcg.model.ProductoTcg;
import cl.deyfer.tcg.repository.ProductoTcgRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class Data implements CommandLineRunner {

    private final ProductoTcgRepository repository;

    public Data(ProductoTcgRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(String... args) {
        if (repository.count() > 0) {
            return; // Si ya hay datos (por ejemplo, en MySQL), no inserta nada
        }

        List<ProductoTcg> productos = List.of(
            ProductoTcg.builder()
                .sku("PKM-CRZ-ETB")
                .nombre("Elite Trainer Box Crown Zenith")
                .setColeccion("Crown Zenith")
                .categoria("SELLADO")
                .idioma("INGLES")
                .precio(64990.0)
                .stock(12)
                .disponible(true)
                .build(),

            ProductoTcg.builder()
                .sku("PKM-MEW-151-UPC")
                .nombre("Ultra Premium Collection Pokémon 151")
                .setColeccion("Scarlet & Violet 151")
                .categoria("SELLADO")
                .idioma("INGLES")
                .precio(149990.0)
                .stock(4)
                .disponible(true)
                .build(),

            ProductoTcg.builder()
                .sku("PKM-OBF-BB")
                .nombre("Booster Box Obsidian Flames")
                .setColeccion("Obsidian Flames")
                .categoria("SELLADO")
                .idioma("ESPANOL")
                .precio(139990.0)
                .stock(8)
                .disponible(true)
                .build(),

            ProductoTcg.builder()
                .sku("PKM-PAL-BBL")
                .nombre("Blister Pack Paldea Evolved (3 Sobres)")
                .setColeccion("Paldea Evolved")
                .categoria("SELLADO")
                .idioma("INGLES")
                .precio(14990.0)
                .stock(25)
                .disponible(true)
                .build(),

            ProductoTcg.builder()
                .sku("PKM-151-CHZ-SIR")
                .nombre("Charizard ex #199/165 Special Illustration Rare")
                .setColeccion("Scarlet & Violet 151")
                .categoria("SINGLE")
                .idioma("INGLES")
                .precio(125000.0)
                .stock(2)
                .disponible(true)
                .build(),

            ProductoTcg.builder()
                .sku("PKM-PAF-ION-SAR")
                .nombre("Iono #237/091 Special Art Rare")
                .setColeccion("Paldean Fates")
                .categoria("SINGLE")
                .idioma("ESPANOL")
                .precio(42000.0)
                .stock(3)
                .disponible(true)
                .build(),

            ProductoTcg.builder()
                .sku("PKM-151-ETB")
                .nombre("Elite Trainer Box Scarlet & Violet 151")
                .setColeccion("Scarlet & Violet 151")
                .categoria("SELLADO")
                .idioma("INGLES")
                .precio(74990.0)
                .stock(6)
                .disponible(true)
                .build(),

            ProductoTcg.builder()
                .sku("PKM-OBF-BB")
                .nombre("Booster Box Obsidian Flames (36 Sobres)")
                .setColeccion("Obsidian Flames")
                .categoria("SELLADO")
                .idioma("ESPANOL")
                .precio(139990.0)
                .stock(8)
                .disponible(true)
                .build(),

            ProductoTcg.builder()
                .sku("PKM-PAF-ETB")
                .nombre("Elite Trainer Box Paldean Fates")
                .setColeccion("Paldean Fates")
                .categoria("SELLADO")
                .idioma("INGLES")
                .precio(59990.0)
                .stock(15)
                .disponible(true)
                .build(),

            ProductoTcg.builder()
                .sku("PKM-TWM-BB")
                .nombre("Booster Box Twilight Masquerade (36 Sobres)")
                .setColeccion("Twilight Masquerade")
                .categoria("SELLADO")
                .idioma("INGLES")
                .precio(144990.0)
                .stock(10)
                .disponible(true)
                .build(),

            ProductoTcg.builder()
                .sku("PKM-SCR-BB")
                .nombre("Booster Box Stellar Crown (36 Sobres)")
                .setColeccion("Stellar Crown")
                .categoria("SELLADO")
                .idioma("INGLES")
                .precio(144990.0)
                .stock(9)
                .disponible(true)
                .build(),

            ProductoTcg.builder()
                .sku("PKM-SSP-ETB")
                .nombre("Elite Trainer Box Surging Sparks")
                .setColeccion("Surging Sparks")
                .categoria("SELLADO")
                .idioma("INGLES")
                .precio(59990.0)
                .stock(14)
                .disponible(true)
                .build(),

            ProductoTcg.builder()
                .sku("PKM-PAF-TIN-CHA")
                .nombre("Lata Shiny Charizard ex Paldean Fates")
                .setColeccion("Paldean Fates")
                .categoria("SELLADO")
                .idioma("INGLES")
                .precio(24990.0)
                .stock(18)
                .disponible(true)
                .build(),

            ProductoTcg.builder()
                .sku("PKM-PAL-BBL")
                .nombre("Blister Pack Paldea Evolved (3 Sobres + Promo Varoom)")
                .setColeccion("Paldea Evolved")
                .categoria("SELLADO")
                .idioma("ESPANOL")
                .precio(14990.0)
                .stock(20)
                .disponible(true)
                .build(),

            ProductoTcg.builder()
                .sku("PKM-151-CHZ-199")
                .nombre("Charizard ex #199/165 Special Illustration Rare")
                .setColeccion("Scarlet & Violet 151")
                .categoria("SINGLE")
                .idioma("INGLES")
                .precio(125000.0)
                .stock(2)
                .disponible(true)
                .build(),

            ProductoTcg.builder()
                .sku("PKM-PAF-ION-237")
                .nombre("Iono #237/091 Special Illustration Rare")
                .setColeccion("Paldean Fates")
                .categoria("SINGLE")
                .idioma("INGLES")
                .precio(44000.0)
                .stock(3)
                .disponible(true)
                .build(),

            ProductoTcg.builder()
                .sku("PKM-TWM-CAR-218")
                .nombre("Carmine #218/167 Special Illustration Rare")
                .setColeccion("Twilight Masquerade")
                .categoria("SINGLE")
                .idioma("INGLES")
                .precio(89990.0)
                .stock(1)
                .disponible(true)
                .build(),

            ProductoTcg.builder()
                .sku("PKM-SSP-PIK-238")
                .nombre("Pikachu ex #238/191 Special Illustration Rare")
                .setColeccion("Surging Sparks")
                .categoria("SINGLE")
                .idioma("INGLES")
                .precio(180000.0)
                .stock(1)
                .disponible(true)
                .build(),

            ProductoTcg.builder()
                .sku("PKM-151-BLA-202")
                .nombre("Blastoise ex #202/165 Special Illustration Rare")
                .setColeccion("Scarlet & Violet 151")
                .categoria("SINGLE")
                .idioma("ESPANOL")
                .precio(55000.0)
                .stock(0)
                .disponible(false)
                .build(),

            ProductoTcg.builder()
                .sku("PKM-CRZ-GIR-GG69")
                .nombre("Giratina VSTAR #GG69/GG70 Galarian Gallery Gold")
                .setColeccion("Crown Zenith")
                .categoria("SINGLE")
                .idioma("INGLES")
                .precio(110000.0)
                .stock(2)
                .disponible(true)
                .build(),

            ProductoTcg.builder()
                .sku("PKM-LBD-GAR-2024")
                .nombre("League Battle Deck Gardevoir ex")
                .setColeccion("Decks Competitivos")
                .categoria("MAZO")
                .idioma("INGLES")
                .precio(32990.0)
                .stock(7)
                .disponible(true)
                .build()
        );

        repository.saveAll(productos);
    }
}