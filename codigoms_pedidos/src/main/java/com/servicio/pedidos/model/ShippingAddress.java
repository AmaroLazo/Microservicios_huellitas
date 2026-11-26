package com.servicio.pedidos.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class ShippingAddress {
    private String street;
    private String city;

    @Column(length = 20)
    private String houseNumber;

    @Column(length = 100)
    private String region;

    @Column(length = 7)
    private String postalCode;

    public ShippingAddress(String street, String city) {
        this(street, city, null, null, null);
    }
}
