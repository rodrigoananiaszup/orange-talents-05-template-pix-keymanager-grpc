package br.com.zup.edu.rodrigo.pix.remove

import br.com.zup.edu.rodrigo.integration.bcb.BancoCentralClient
import br.com.zup.edu.rodrigo.integration.bcb.DeletePixKeyRequest
import br.com.zup.edu.rodrigo.pix.registra.ChavePixRepository
import br.com.zup.edu.rodrigo.shared.grpc.validacao.ValidUUID
import io.micronaut.http.HttpStatus
import io.micronaut.validation.Validated
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import javax.transaction.Transactional
import javax.validation.constraints.NotBlank


@Singleton
@Validated
class RemoveChavePix(
    @Inject val chavePixRepository: ChavePixRepository,
    @Inject val bcbClient: BancoCentralClient
) {


    @Transactional
    fun remove(
        @NotBlank @ValidUUID clienteId: String?,
        @NotBlank @ValidUUID pixId: String?,
    ) {

        val uuidPixId = UUID.fromString(pixId)
        val uuidClienteId = UUID.fromString(clienteId)

        val chave = chavePixRepository.findByIdAndClienteId(uuidPixId, uuidClienteId)
            .orElseThrow { ChavePixNaoEncontradaException("Chave Pix não encontrada ou não pertence ao cliente") }

        chavePixRepository.delete(chave)

        val request = DeletePixKeyRequest(chave.chave)

        val bcbResponse = bcbClient.delete(key = chave.chave, request = request)
        if (bcbResponse.status != HttpStatus.OK) {
            throw IllegalStateException("Erro ao remover chave Pix no Banco Central do Brasil (BCB)")
        }
    }
}