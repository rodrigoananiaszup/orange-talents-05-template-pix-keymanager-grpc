package br.com.zup.edu.rodrigo.pix.registra


import br.com.zup.edu.rodrigo.RegistraChavePixRequest
import br.com.zup.edu.rodrigo.TipoChave.*
import br.com.zup.edu.rodrigo.TipoConta.*

fun RegistraChavePixRequest.paraNovaChavePix(): NovaChavePix {
    return NovaChavePix(
        clienteId = this.clienteId,
        tipoChave = when (this.tipoDeChave) {
            UNKNOW_TIPO_CHAVE -> null
            else -> TipoChave.valueOf(this.tipoDeChave.name)
        },
        chave = this.chave,
        tipoConta = when (this.tipoDeConta) {
            UNKNOW_TIPO_CONTA -> null
            else -> TipoConta.valueOf(this.tipoDeConta.name)
        }
    )
}