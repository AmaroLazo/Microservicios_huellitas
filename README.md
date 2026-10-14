# Diagramas UML de clases — Pet Shop

Los siguientes diagramas están escritos en Mermaid y pueden visualizarse en GitHub, VS Code (con una extensión compatible) o [Mermaid Live Editor](https://mermaid.live/).

## 1. Product, Category y StockMovement

```mermaid
classDiagram
    class Product {
        -Long id
        -String name
        -String description
        -String imageUrl
        -BigDecimal price
        -BigDecimal discountPercent
        -Integer stockQuantity
        -Integer minStock
        -Boolean active
        +createProduct(name, description, price, stock) Product
        +updateProduct(name, description, imageUrl, price) void
        +setActive(active) void
        +deleteProduct() void
        +updatePrice(newPrice) void
        +getFinalPrice() BigDecimal
        +hasStock(quantity) Boolean
        +isLowStock() Boolean
        +adjustInventory(quantity, int, reason) StockMovement
        +assignCategory(category) void
    }

    class Category {
        -Long id
        -String name
        -String description
        -Boolean active
    }

    class StockMovement {
        -Long id
        -Long productId
        -Integer quantity
        -String reason
        -LocalDateTime createdAt
    }

    Product "*" --> "1" Category : pertenece a
    Product "1" --> "*" StockMovement : registra
```

## 2. Order, OrderItem, ShippingAddress y OrderStatus

```mermaid
classDiagram
    class Order {
        -Long id
        -Long userId
        -OrderStatus status
        -BigDecimal totalAmount
        -LocalDateTime createdAt
        -LocalDateTime updatedAt
        +addItem(productId, productName, quantity, price) void
        +updateItemQuantity(productId, newQty) void
        +removeItem(productId) void
        +clearItems() void
        +getTotalQuantity() Integer
        +calculateTotal() BigDecimal
        +setShippingAddress(address) void
        +confirmOrder() void
        +updateStatus(newStatus) void
        +cancelOrder() Boolean
        +canBeCancelled() Boolean
        +findByUserId(userId) List~Order~
        +findByStatus(status) List~Order~
    }

    class OrderItem {
        -Long id
        -Long productId
        -String productName
        -Integer quantity
        -BigDecimal unitPrice
        +getSubtotal() BigDecimal
    }

    class ShippingAddress {
        -String street
        -String city
    }

    class OrderStatus {
        <<enumeration>>
        PENDIENTE
        CANCELADO
        EN_PREPARACION
        ENVIADO
        ENTREGADO
    }

    Order "1" *-- "1..*" OrderItem : contiene
    Order "1" *-- "1" ShippingAddress : utiliza
    Order --> OrderStatus : tiene estado
```

## 3. Payment y PaymentMethod

```mermaid
classDiagram
    class Payment {
        -Long id
        -Long orderId
        -BigDecimal amount
        -String transactionId
        -LocalDateTime transactionDate
        +processPayment(amount, method) PaymentResponse
        +executeTransaction() Payment
        +getPaymentStatus() PaymentStatus
        +handleGatewayResult(event) void
        +retryPayment() PaymentResponse
        +cancelPayment() Boolean
        +refundPayment(amount) Boolean
        +validateIdempotency(key) Boolean
        +generateReceipt() Receipt
        +findByOrderId(orderId) List~Payment~
        +findByStatus(status) List~Payment~
    }

    class PaymentMethod {
        <<enumeration>>
        CREDITO
        DEBITO
    }

    Payment --> PaymentMethod : utiliza
```

## 4. User, Address y Role

```mermaid
classDiagram
    class User {
        -Long id
        -String name
        -String email
        -String passwordHash
        -Role role
        -Boolean active
        -LocalDateTime createdAt
        +register(name, email, password) User
        +login(email, password) Boolean
        +logout() void
        +resetPassword(email) void
        +getProfile() User
        +updateProfile(name, email) void
        +changePassword(oldPassword, newPassword) void
        +deactivateAccount() void
        +addAddress(address) void
        +removeAddress(addressId) void
        +listUsers() List~User~
        +searchUsers(query) List~User~
        +toggleUserStatus(userId) void
        +updateUserRole(userId, newRole) void
    }

    class Address {
        -Long id
        -String street
        -String city
    }

    class Role {
        <<enumeration>>
        CUSTOMER
        ADMIN
    }

    User "1" *-- "0..*" Address : posee
    User --> Role : tiene rol
```

## Notas

- `*--` representa una relación de composición: un objeto contiene otros objetos que forman parte de él.
- `-->` representa una asociación dirigida entre clases.
- `1` significa exactamente uno.
- `0..*` significa cero o muchos.
- `1..*` significa uno o muchos.
- Las enumeraciones representan un conjunto definido de valores posibles.
- Los tipos y las firmas de los métodos se han transcrito a partir del diagrama de referencia y conviene contrastarlos con el código fuente del proyecto.
