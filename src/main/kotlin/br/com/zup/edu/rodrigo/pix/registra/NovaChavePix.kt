package br.com.zup.edu.rodrigo.pix.registra

import br.com.zup.edu.rodrigo.shared.grpc.validacao.ValidPixKey
import br.com.zup.edu.rodrigo.shared.grpc.validacao.ValidUUID
import io.micronaut.core.annotation.Introspected
import java.util.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@ValidPixKey
@Introspected
data class NovaChavePix(
    @field:ValidUUID
    @field:NotBlank
    val clienteId: String?,
    @field:NotNull
    val tipo: TipoChave?,
    @field:Size(max = 77)
    val chave: String?,
    @field:NotNull
    val tipoDeConta: TipoConta?
) {

    fun toModel(conta: ContaAssociada): ChavePix {
        return ChavePix(
            clienteId = UUID.fromString(this.clienteId),
            tipoDeChave = TipoChave.valueOf(this.tipo!!.name),
            chave = if (this.tipo == TipoChave.ALEATORIA) UUID.randomUUID().toString() else this.chave!!,
            tipoDeConta = TipoConta.valueOf(this.tipoDeConta!!.name),
            conta = conta
        )
    }

}
