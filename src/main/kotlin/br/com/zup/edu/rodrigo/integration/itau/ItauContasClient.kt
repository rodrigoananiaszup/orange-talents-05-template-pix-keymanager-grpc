package br.com.zup.edu.rodrigo.integration.itau

import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.QueryValue
import io.micronaut.http.client.annotation.Client

@Client("\${itau.contas.url}")
interface ItauContasClient {

    @Get("/api/v1/clientes/{clienteId}/contas{?tipo}")
    fun buscaContaPorTipo(@PathVariable clienteId: String, @QueryValue("tipo") tipo: String)
            : HttpResponse<DadosDaContaResponse>
}


