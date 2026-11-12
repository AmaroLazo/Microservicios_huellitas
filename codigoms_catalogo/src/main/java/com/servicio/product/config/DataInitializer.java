package com.servicio.product.config;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.servicio.product.model.Category;
import com.servicio.product.model.Product;
import com.servicio.product.repository.CategoryRepository;
import com.servicio.product.repository.ProductRepository;

/**
 * Agrega categorías y productos iniciales sin duplicarlos al reiniciar.
 * Los registros existentes NO se modifican (precio/stock/descripción).
 */
@Component
public class DataInitializer implements ApplicationRunner {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    public DataInitializer(CategoryRepository categoryRepository, ProductRepository productRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
    }

    private record ProductSeed(String name, String description, long price,
                               int discountPercent, int stockQuantity, int minStock) {}

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        Map<String, Category> categories = new HashMap<>();
        for (Category category : categoryRepository.findAll()) {
            categories.put(normalize(category.getName()), category);
        }

        // Se usa la combinación categoría + nombre para identificar productos existentes.
        // Así, un producto con el mismo nombre en otra categoría no se confunde.
        Map<String, Set<String>> existingNamesByCategory = new HashMap<>();
        for (Product product : productRepository.findAll()) {
            String categoryKey = normalize(product.getCategory().getName());
            existingNamesByCategory.computeIfAbsent(categoryKey, key -> new HashSet<>())
                    .add(normalize(product.getName()));
        }

        int createdCategories = 0;
        int createdProducts = 0;

        Category category1 = categories.get(normalize("Alimento para Perros"));
        if (category1 == null) {
            category1 = new Category();
            category1.setName("Alimento para Perros");
            category1.setDescription("Alimentos secos, húmedos y premios para perros.");
            category1.setActive(true);
            category1 = categoryRepository.save(category1);
            categories.put(normalize("Alimento para Perros"), category1);
            createdCategories++;
        }
        createdProducts += seedProducts(category1,
                existingNamesByCategory, List.of(
                    new ProductSeed("Alimento seco para perro adulto 3 kg", "Croquetas balanceadas para perros adultos de todas las razas.", 17990, 0, 35, 5),
                    new ProductSeed("Alimento seco para cachorro 3 kg", "Fórmula con proteínas y nutrientes para el crecimiento.", 19990, 5, 30, 5),
                    new ProductSeed("Alimento para perro senior 3 kg", "Nutrición formulada para perros de edad avanzada.", 18990, 0, 24, 4),
                    new ProductSeed("Alimento húmedo pollo para perro 400 g", "Paté de pollo para complementar la alimentación diaria.", 2490, 0, 48, 8),
                    new ProductSeed("Alimento húmedo vacuno para perro 400 g", "Alimento húmedo de vacuno en lata.", 2590, 0, 45, 8),
                    new ProductSeed("Alimento hipoalergénico para perro 2 kg", "Fórmula de ingredientes seleccionados para perros sensibles.", 22990, 10, 22, 4),
                    new ProductSeed("Snack dental para perro 7 unidades", "Premios masticables para complementar la higiene bucal.", 4990, 0, 50, 10),
                    new ProductSeed("Galletas para perro sabor pollo 500 g", "Galletas crujientes para premios y entrenamiento.", 3990, 0, 43, 7),
                    new ProductSeed("Alimento de salmón para perro 3 kg", "Croquetas con salmón para perros adultos.", 20990, 0, 26, 5),
                    new ProductSeed("Snack blando para cachorro 100 g", "Bocaditos suaves para recompensas durante el entrenamiento.", 2990, 0, 55, 10)
                ));

        Category category2 = categories.get(normalize("Alimentos para Gatos"));
        if (category2 == null) {
            category2 = new Category();
            category2.setName("Alimentos para Gatos");
            category2.setDescription("Alimentos secos, húmedos y snacks para gatos.");
            category2.setActive(true);
            category2 = categoryRepository.save(category2);
            categories.put(normalize("Alimentos para Gatos"), category2);
            createdCategories++;
        }
        createdProducts += seedProducts(category2,
                existingNamesByCategory, List.of(
                    new ProductSeed("Alimento seco para gato adulto 2 kg", "Croquetas para gatos adultos de interior o exterior.", 14990, 0, 34, 5),
                    new ProductSeed("Alimento seco para gatito 2 kg", "Nutrición diseñada para gatitos en crecimiento.", 16990, 5, 28, 5),
                    new ProductSeed("Alimento para gato esterilizado 2 kg", "Alimento formulado para gatos esterilizados.", 17990, 0, 32, 5),
                    new ProductSeed("Alimento para gato senior 2 kg", "Croquetas para gatos de edad avanzada.", 16990, 0, 20, 4),
                    new ProductSeed("Alimento húmedo atún para gato 85 g", "Sobre de alimento húmedo con sabor a atún.", 1290, 0, 65, 10),
                    new ProductSeed("Alimento húmedo pollo para gato 85 g", "Sobre de alimento húmedo con sabor a pollo.", 1290, 0, 62, 10),
                    new ProductSeed("Alimento de salmón para gato 2 kg", "Croquetas de salmón para alimentación diaria.", 18990, 10, 27, 5),
                    new ProductSeed("Snack cremoso para gato 4 tubos", "Premio cremoso para ofrecer directamente o con la comida.", 3490, 0, 46, 8),
                    new ProductSeed("Premios crujientes para gato 60 g", "Bocaditos crujientes para recompensa ocasional.", 2490, 0, 58, 10),
                    new ProductSeed("Alimento digestivo para gato 1,5 kg", "Fórmula para gatos que requieren una alimentación delicada.", 19990, 0, 19, 4)
                ));

        Category category3 = categories.get(normalize("Accesorios para Perros"));
        if (category3 == null) {
            category3 = new Category();
            category3.setName("Accesorios para Perros");
            category3.setDescription("Accesorios de paseo, juego e higiene para perros.");
            category3.setActive(true);
            category3 = categoryRepository.save(category3);
            categories.put(normalize("Accesorios para Perros"), category3);
            createdCategories++;
        }
        createdProducts += seedProducts(category3,
                existingNamesByCategory, List.of(
                    new ProductSeed("Correa ajustable para perro 1,5 m", "Correa resistente con mosquetón para paseos diarios.", 6990, 0, 25, 4),
                    new ProductSeed("Arnés acolchado para perro talla M", "Arnés regulable para paseos cómodos.", 12990, 10, 22, 4),
                    new ProductSeed("Collar reflectante para perro talla M", "Collar regulable con detalles reflectantes.", 5990, 0, 35, 5),
                    new ProductSeed("Plato de acero inoxidable para perro 1 L", "Recipiente fácil de limpiar para agua o alimento.", 7990, 0, 28, 5),
                    new ProductSeed("Juguete pelota de goma para perro", "Pelota de goma resistente para juegos de búsqueda.", 4990, 0, 44, 8),
                    new ProductSeed("Cuerda mordedora para perro", "Juguete de cuerda para tirar y morder.", 4490, 0, 41, 7),
                    new ProductSeed("Cama acolchada para perro talla M", "Cama suave con borde elevado y funda lavable.", 24990, 5, 16, 3),
                    new ProductSeed("Bolsa dispensadora para paseo", "Dispensador portátil con rollo de bolsas de higiene.", 2990, 0, 52, 10),
                    new ProductSeed("Cepillo de aseo para perro", "Cepillo de cerdas suaves para el cuidado del pelaje.", 6490, 0, 27, 5),
                    new ProductSeed("Botella portátil de agua para perro", "Botella de paseo con bebedero integrado.", 9990, 0, 24, 4)
                ));

        Category category4 = categories.get(normalize("Accesorios para gatos"));
        if (category4 == null) {
            category4 = new Category();
            category4.setName("Accesorios para gatos");
            category4.setDescription("Accesorios de juego, descanso e higiene para gatos.");
            category4.setActive(true);
            category4 = categoryRepository.save(category4);
            categories.put(normalize("Accesorios para gatos"), category4);
            createdCategories++;
        }
        createdProducts += seedProducts(category4,
                existingNamesByCategory, List.of(
                    new ProductSeed("Rascador de cartón para gato", "Superficie de cartón para rascar y jugar.", 8990, 0, 31, 5),
                    new ProductSeed("Rascador vertical con cuerda para gato", "Poste rascador compacto para interiores.", 18990, 10, 20, 4),
                    new ProductSeed("Varita con plumas para gato", "Juguete interactivo para estimular el movimiento.", 3990, 0, 55, 10),
                    new ProductSeed("Pelota con cascabel para gato", "Pelota liviana para juegos individuales.", 2490, 0, 61, 10),
                    new ProductSeed("Ratón de juguete para gato", "Juguete pequeño de tela con forma de ratón.", 2990, 0, 48, 8),
                    new ProductSeed("Arenero plástico para gato", "Bandeja sanitaria de plástico fácil de lavar.", 13990, 0, 23, 4),
                    new ProductSeed("Pala para arena de gato", "Pala ranurada para retirar residuos del arenero.", 2990, 0, 42, 7),
                    new ProductSeed("Cama tipo cueva para gato", "Refugio acolchado para descanso en interior.", 22990, 5, 17, 3),
                    new ProductSeed("Transportadora para gato talla S", "Transportadora rígida con ventilación y asa superior.", 29990, 0, 14, 3),
                    new ProductSeed("Fuente de agua para gato 2 L", "Bebedero de circulación continua para mascotas.", 26990, 0, 19, 3)
                ));

        System.out.printf("[Catalogo DataInitializer] Categorías nuevas: %d; productos nuevos: %d%n",
                createdCategories, createdProducts);
    }

    private int seedProducts(Category category, Map<String, Set<String>> namesByCategory,
                             List<ProductSeed> seeds) {
        String categoryKey = normalize(category.getName());
        Set<String> existingNames = namesByCategory.computeIfAbsent(categoryKey, key -> new HashSet<>());
        int created = 0;

        for (ProductSeed seed : seeds) {
            if (!existingNames.add(normalize(seed.name()))) {
                continue; // Ya existe en esta categoría: mantenerlo sin cambios.
            }
            Product product = new Product();
            product.setName(seed.name());
            product.setDescription(seed.description());
            product.setImageUrl(null); // No se inventan URL de imágenes.
            product.setPrice(BigDecimal.valueOf(seed.price()));
            product.setDiscountPercent(BigDecimal.valueOf(seed.discountPercent()));
            product.setStockQuantity(seed.stockQuantity());
            product.setMinStock(seed.minStock());
            product.setActive(true);
            product.setCategory(category);
            productRepository.save(product);
            created++;
        }
        return created;
    }

    private String normalize(String value) {
        return value == null ? "" : value.strip().toLowerCase(Locale.ROOT);
    }
}
