package br.com.zup.edu.rodrigo.pix.remove

import br.com.zup.edu.rodrigo.shared.grpc.validacao.ValidUUID
import io.micronaut.core.annotation.Introspected

@Introspected
class RemoveChavePixDto(
    @field:ValidUUID val clienteId: String,
    @field:ValidUUID val pixId: String
) {
}