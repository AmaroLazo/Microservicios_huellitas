package com.servicio.carrito.service;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.servicio.carrito.client.CatalogClient;
import com.servicio.carrito.client.OrderClient;
import com.servicio.carrito.client.UserClient;
import com.servicio.carrito.dto.CartItemDTO;
import com.servicio.carrito.dto.CartValidation;
import com.servicio.carrito.dto.CreateOrderDTO;
import com.servicio.carrito.dto.OrderItemDTO;
import com.servicio.carrito.dto.OrderResponse;
import com.servicio.carrito.dto.ShippingAddressDTO;
import com.servicio.carrito.dto.UserDTO;
import com.servicio.carrito.model.Cart;
import com.servicio.carrito.model.CartItem;
import com.servicio.carrito.model.CartStatus;
import com.servicio.carrito.repository.CartRepository;

import feign.FeignException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CatalogClient catalogClient;
    private final OrderClient orderClient;
    private final UserClient userClient;

    @Transactional
    public Cart getCart(Long userId) {
        return getOrCreateActiveCart(userId);
    }

    @Transactional
    public Cart addItem(Long userId, CartItemDTO dto) {
        Cart cart = getOrCreateActiveCart(userId);
        CartItem existing = findItem(cart, dto.getProductId());
        int newQuantity = dto.getQuantity() + (existing != null ? existing.getQuantity() : 0);

        checkStockOrFail(dto.getProductId(), newQuantity);

        if (existing != null) {
            existing.setQuantity(newQuantity);
        } else {
            cart.addItem(new CartItem(dto.getProductId(), dto.getProductName(),
                    catalogClient.getCurrentPrice(dto.getProductId()), dto.getQuantity()));
        }
        return cartRepository.save(cart);
    }

    @Transactional
    public Cart updateQuantity(Long userId, Long itemId, int quantity) {
        Cart cart = getActiveCartOrFail(userId);
        if (quantity > 0) {
            CartItem item = cart.getItems().stream()
                    .filter(i -> itemId.equals(i.getId()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Item no encontrado: " + itemId));
            checkStockOrFail(item.getProductId(), quantity);
        }
        cart.updateQuantity(itemId, quantity);
        return cartRepository.save(cart);
    }

    @Transactional
    public Cart removeItem(Long userId, Long itemId) {
        Cart cart = getActiveCartOrFail(userId);
        cart.removeItem(itemId);
        return cartRepository.save(cart);
    }

    @Transactional
    public void clearCart(Long userId) {
        Cart cart = getActiveCartOrFail(userId);
        cart.clear();
        cartRepository.save(cart);
    }

    @Transactional
    public Cart mergeCarts(String sessionId, Long userId) {
        Cart userCart = getOrCreateActiveCart(userId);

        cartRepository.findActiveBySessionId(sessionId).ifPresent(guestCart -> {
            for (CartItem g : guestCart.getItems()) {
                CartItem existing = findItem(userCart, g.getProductId());
                if (existing != null) {
                    existing.setQuantity(existing.getQuantity() + g.getQuantity());
                } else {
                    userCart.addItem(new CartItem(g.getProductId(), g.getProductName(),
                            g.getUnitPrice(), g.getQuantity()));
                }
            }
            guestCart.setStatus(CartStatus.ABANDONED);
            cartRepository.save(guestCart);
        });

        return cartRepository.save(userCart);
    }

    public CartValidation validateBeforeCheckout(Cart cart) {
        CartValidation validation = new CartValidation();

        if (cart.getItems().isEmpty()) {
            validation.getErrors().add("El carrito esta vacio");
        }
        for (CartItem item : cart.getItems()) {
            if (!Boolean.TRUE.equals(catalogClient.checkStock(item.getProductId(), item.getQuantity()))) {
                validation.getErrors().add("Stock insuficiente o producto no disponible: " + item.getProductName());
            } else {
                BigDecimal currentPrice = catalogClient.getCurrentPrice(item.getProductId());
                if (currentPrice.compareTo(item.getUnitPrice()) != 0) {
                    validation.getErrors().add("El precio de " + item.getProductName() + " cambio");
                }
            }
        }
        validation.setValid(validation.getErrors().isEmpty());
        return validation;
    }

    @Transactional
    public OrderResponse checkout(Long userId, ShippingAddressDTO address) {
        UserDTO user = findUser(userId);
        if (!Boolean.TRUE.equals(user.getActivo())) {
            throw new RuntimeException("El usuario '" + user.getNombre() + "' no esta activo");
        }

        Cart cart = getActiveCartOrFail(userId);
        CartValidation validation = validateBeforeCheckout(cart);
        if (!validation.isValid()) {
            throw new RuntimeException("Carrito invalido: " + validation.getErrors());
        }

        CreateOrderDTO orderDto = new CreateOrderDTO();
        orderDto.setUserId(userId);
        orderDto.setShippingAddress(address);
        orderDto.setItems(cart.getItems().stream().map(i -> {
            OrderItemDTO d = new OrderItemDTO();
            d.setProductId(i.getProductId());
            d.setProductName(i.getProductName());
            d.setQuantity(i.getQuantity());
            d.setUnitPrice(i.getUnitPrice());
            return d;
        }).toList());

        OrderResponse order = orderClient.createOrder(orderDto);

        cart.setStatus(CartStatus.CHECKED_OUT);
        cartRepository.save(cart);
        return order;
    }

    // ---------- helpers ----------

    private Cart getOrCreateActiveCart(Long userId) {
        return cartRepository.findActiveByUserId(userId)
                .orElseGet(() -> cartRepository.save(new Cart(userId)));
    }

    private Cart getActiveCartOrFail(Long userId) {
        return cartRepository.findActiveByUserId(userId)
                .orElseThrow(() -> new RuntimeException("No hay carrito activo para el usuario " + userId));
    }

    private CartItem findItem(Cart cart, Long productId) {
        return cart.getItems().stream()
                .filter(i -> i.getProductId().equals(productId))
                .findFirst()
                .orElse(null);
    }

    private void checkStockOrFail(Long productId, int quantity) {
        if (!Boolean.TRUE.equals(catalogClient.checkStock(productId, quantity))) {
            throw new RuntimeException("Stock insuficiente o producto no disponible: " + productId);
        }
    }

    private UserDTO findUser(Long userId) {
        try {
            return userClient.getUserById(userId);
        } catch (FeignException.NotFound e) {
            throw new RuntimeException("El usuario " + userId + " no existe en el sistema");
        } catch (FeignException e) {
            throw new RuntimeException("No se pudo verificar el usuario. Verifique que codigoms_usuarios este corriendo.");
        }
    }
}
