package br.com.fiap.ms_pagamento.http;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

// Definindo que é um FeignClient
@FeignClient("ms-pedidos")  // passando o nome do MS como parâmetro
public interface PedidoClient {

    // Passando o tipo da requisição, para qual endpoint e o que precisa ser feito
    @RequestMapping(method = RequestMethod.PUT, value = "/pedidos/{id}/pago")
    void atualizarPagamentoDoPedido(@PathVariable Long id);

}
