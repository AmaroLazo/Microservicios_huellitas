package com.servicio.codigoms_pagos.client;

import com.servicio.codigoms_pagos.dto.GatewayResponse;
import com.servicio.codigoms_pagos.dto.PaymentRequest;

public interface PaymentGatewayClient {

    GatewayResponse charge(PaymentRequest request);

    Boolean refund(String transactionId);
}
