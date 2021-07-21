package br.com.zup.edu.rodrigo.pix.consulta

import br.com.zup.edu.rodrigo.pix.ChavePix
import br.com.zup.edu.rodrigo.pix.ContaAssociada
import br.com.zup.edu.rodrigo.pix.TipoChave
import br.com.zup.edu.rodrigo.pix.TipoConta
import java.time.LocalDateTime
import java.util.*

data class ChavePixInfo(
    val pixId: UUID? = null,
    val clienteId: UUID? = null,
    val tipoDeChave: TipoChave,
    val chave: String,
    val tipoDeConta: TipoConta,
    val conta: ContaAssociada,
    val registradaEm: LocalDateTime = LocalDateTime.now()
) {

    companion object {
        fun of(chave: ChavePix): ChavePixInfo {
            return ChavePixInfo(
                pixId = chave.id,
                clienteId = chave.clienteId,
                tipoDeChave = chave.tipoDeChave,
                chave = chave.chave,
                tipoDeConta = chave.tipoDeConta,
                conta = chave.conta,
                registradaEm = chave.criadaEm
            )
        }
    }
}