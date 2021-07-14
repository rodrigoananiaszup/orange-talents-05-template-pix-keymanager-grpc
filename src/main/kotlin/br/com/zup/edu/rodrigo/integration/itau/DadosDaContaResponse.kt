package br.com.zup.edu.rodrigo.integration.itau

import br.com.zup.edu.rodrigo.pix.registra.ContaAssociada
import io.micronaut.core.annotation.Introspected
import java.util.*

@Introspected
data class DadosDaContaResponse(
    val tipo: String,
    val instituicao: InstituicaoResponse,
    val agencia: String,
    val numero: String,
    val titular: TitularResponse
) {

    data class InstituicaoResponse(
        val nome: String,
        val ispb: String
    ) {
        fun toModel(): ContaAssociada.Instituicao {
            return ContaAssociada.Instituicao(
                nomeInstituicao = this.nome,
                ispb = this.ispb
            )
        }
    }

    data class TitularResponse(
        val id: String,
        val nome: String,
        val cpf: String
    ) {
        fun toModel(): ContaAssociada.Titular {
            return ContaAssociada.Titular(
                titularId = UUID.fromString(this.id),
                nomeTitular = this.nome,
                cpf = this.cpf
            )
        }
    }

    fun toModel(): ContaAssociada {
        return ContaAssociada(
            tipo = this.tipo,
            instituicao = this.instituicao.toModel(),
            agencia = this.agencia,
            numero = this.numero,
            titular = this.titular.toModel()
        )
    }

}


