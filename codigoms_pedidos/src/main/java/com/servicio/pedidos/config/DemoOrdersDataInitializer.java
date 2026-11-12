package com.servicio.pedidos.config;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.servicio.pedidos.model.Order;
import com.servicio.pedidos.model.OrderItem;
import com.servicio.pedidos.model.OrderStatus;
import com.servicio.pedidos.model.ShippingAddress;
import com.servicio.pedidos.repository.OrderRepository;

import lombok.extern.slf4j.Slf4j;

/** Pedidos históricos de demostración; no ajusta el stock ni genera pagos. */
@Slf4j
@Component
@ConditionalOnProperty(name = "app.seed.demo.enabled", havingValue = "true")
public class DemoOrdersDataInitializer implements ApplicationRunner {

    private static final BigDecimal MIN_TOTAL = new BigDecimal("50000");
    private static final BigDecimal MAX_TOTAL = new BigDecimal("250000");
    private static final String EMAIL_DOMAIN = "@huellitas.test";

    private final OrderRepository orderRepository;
    private final RestClient usersClient;
    private final RestClient catalogClient;

    public DemoOrdersDataInitializer(OrderRepository orderRepository,
            @Value("${users.service.url:http://localhost:8090}") String usersUrl,
            @Value("${catalog.service.url:http://localhost:8082}") String catalogUrl) {
        this.orderRepository = orderRepository;
        this.usersClient = RestClient.builder().baseUrl(usersUrl).build();
        this.catalogClient = RestClient.builder().baseUrl(catalogUrl).build();
    }

    private record DemoBuyer(String email, String street, String city) {}

    private static final List<DemoBuyer> BUYERS = List.of(
            new DemoBuyer("brcantero" + EMAIL_DOMAIN, "Los Arrayanes 123", "Coronel"),
            new DemoBuyer("fradrovez" + EMAIL_DOMAIN, "Los Copihues 456", "Concepción"),
            new DemoBuyer("varojas" + EMAIL_DOMAIN, "Las Acacias 789", "San Pedro de la Paz"),
            new DemoBuyer("maherrera" + EMAIL_DOMAIN, "Los Alerces 321", "Talcahuano"),
            new DemoBuyer("cafuentes" + EMAIL_DOMAIN, "Los Robles 654", "Chiguayante")
    );

    private record UserView(Long userId, String email, Boolean active) {}
    private record ProductView(Long id, String name, BigDecimal finalPrice,
                               Integer stockQuantity, Boolean active) {}
    private record ChosenProduct(ProductView product, int quantity) {}

    @Override
    public void run(ApplicationArguments args) {
        // El microservicio de pedidos NO accede directamente a usuarios_db ni catalogo_db.
        UserView[] users;
        ProductView[] products;
        try {
            users = usersClient.get().uri("/api/users").retrieve().body(UserView[].class);
            products = catalogClient.get().uri("/api/v1/products/available")
                    .retrieve().body(ProductView[].class);
        } catch (RuntimeException e) {
            log.warn("No se insertan pedidos de demo: inicia primero usuarios (8090) y catálogo (8082). {}",
                    e.getMessage());
            return; // Se puede reintentar al reiniciar el servicio de pedidos.
        }

        if (users == null || products == null) {
            log.warn("Sin usuarios o productos disponibles: pedidos demo omitidos.");
            return;
        }
        Map<String, UserView> usersByEmail = java.util.Arrays.stream(users)
                .filter(u -> u.email() != null)
                .collect(Collectors.toMap(u -> u.email().toLowerCase(java.util.Locale.ROOT),
                        Function.identity(), (a, b) -> a));
        List<ProductView> catalog = java.util.Arrays.stream(products)
                .filter(p -> p.id() != null && p.name() != null && p.finalPrice() != null
                        && p.finalPrice().signum() > 0 && p.stockQuantity() != null
                        && p.stockQuantity() >= 1 && Boolean.TRUE.equals(p.active()))
                .sorted(Comparator.comparing(ProductView::id))
                .toList();
        if (catalog.isEmpty()) {
            log.warn("No hay productos activos con stock y precio. Inicializa catálogo primero.");
            return;
        }

        int inserted = 0;
        for (int buyerIndex = 0; buyerIndex < BUYERS.size(); buyerIndex++) {
            DemoBuyer buyer = BUYERS.get(buyerIndex);
            UserView user = usersByEmail.get(buyer.email());
            if (user == null || user.userId() == null || !Boolean.TRUE.equals(user.active())) {
                log.warn("Usuario de prueba {} no existe o está inactivo, se omiten sus pedidos", buyer.email());
                continue;
            }
            for (int n = 1; n <= 3; n++) {
                String seedKey = "HUELLITAS-DEMO-2026-" + buyer.email().split("@")[0] + "-" + n;
                // Sin modificar Order ni OrderRepository: reconocemos pedidos demo por una marca
                // en la calle de envío. Solo se usa en datos ficticios.
                String marca = " [" + seedKey + "]";
                boolean yaExiste = orderRepository.findByUserId(user.userId()).stream()
                        .anyMatch(p -> p.getShippingAddress() != null
                                && p.getShippingAddress().getStreet() != null
                                && p.getShippingAddress().getStreet().endsWith(marca));
                if (yaExiste) continue;

                BigDecimal target = BigDecimal.valueOf(65000L + buyerIndex * 17000L + n * 12000L);
                List<ChosenProduct> chosen = selectProducts(catalog, buyerIndex * 7 + n * 5, target);
                BigDecimal total = chosen.stream()
                        .map(c -> c.product().finalPrice().multiply(BigDecimal.valueOf(c.quantity())))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                if (total.compareTo(MIN_TOTAL) < 0 || total.compareTo(MAX_TOTAL) > 0) {
                    log.warn("No se pudo armar el pedido {} dentro del rango $50.000-$250.000: {}",
                            seedKey, total);
                    continue;
                }
                saveDemoOrder(seedKey, user.userId(), buyer, chosen, n);
                inserted++;
            }
        }
        log.info("Pedidos de demostración nuevos: {} (máximo 15). Comprueba pedidos_db.orders y order_items.", inserted);
    }

    private List<ChosenProduct> selectProducts(List<ProductView> products, int offset, BigDecimal target) {
        List<ChosenProduct> selected = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        // Selección determinista, sin asumir IDs concretos ni precios fijos del catálogo.
        for (int i = 0; i < products.size() && selected.size() < 7; i++) {
            ProductView p = products.get((offset + i) % products.size());
            if (total.compareTo(target) >= 0) break;
            BigDecimal remainingBudget = MAX_TOTAL.subtract(total);
            int maxQuantityByBudget = remainingBudget.divide(p.finalPrice(), 0, RoundingMode.DOWN).intValue();
            int quantity = Math.min(3, Math.min(p.stockQuantity(), maxQuantityByBudget));
            if (quantity < 1) continue;
            // En pedidos de demo se combinan productos, no una sola línea gigante.
            if (total.compareTo(MIN_TOTAL) >= 0) quantity = 1;
            selected.add(new ChosenProduct(p, quantity));
            total = total.add(p.finalPrice().multiply(BigDecimal.valueOf(quantity)));
        }
        return selected;
    }

    private void saveDemoOrder(String seedKey, Long userId, DemoBuyer buyer,
                                 List<ChosenProduct> chosen, int orderNumber) {
        // Idempotencia mediante una marca en la dirección ficticia, sin cambiar entidades ni repositorios.
        Order order = new Order();
        
        order.setUserId(userId);
        order.setShippingAddress(new ShippingAddress(buyer.street() + " [" + seedKey + "]", buyer.city()));
        order.setStatus(switch (orderNumber) {
            case 1 -> OrderStatus.ENTREGADO;
            case 2 -> OrderStatus.EN_PREPARACION;
            default -> OrderStatus.PENDIENTE;
        });
        BigDecimal total = BigDecimal.ZERO;
        for (ChosenProduct selection : chosen) {
            ProductView product = selection.product();
            OrderItem item = new OrderItem(product.id(), product.name(),
                    selection.quantity(), product.finalPrice());
            item.setOrder(order);
            order.getItems().add(item);
            total = total.add(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        }
        order.setTotalAmount(total);
        orderRepository.save(order);
        log.info("Pedido demo {} creado para userId={}, total={}", seedKey, userId, total);
    }
}
