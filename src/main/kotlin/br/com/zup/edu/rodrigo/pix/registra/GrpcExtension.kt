package br.com.zup.edu.rodrigo.pix.registra


import br.com.zup.edu.rodrigo.RegistraChavePixRequest
import br.com.zup.edu.rodrigo.TipoChave.*
import br.com.zup.edu.rodrigo.TipoConta.*
import br.com.zup.edu.rodrigo.pix.TipoChave
import br.com.zup.edu.rodrigo.pix.TipoConta

fun RegistraChavePixRequest.toModel() : NovaChavePix {
    return NovaChavePix( // 1
        clienteId = clienteId,
        tipo = when (tipoDeChave) {
            UNKNOW_TIPO_CHAVE -> null
            else -> TipoChave.valueOf(tipoDeChave.name) // 1
        },
        chave = chave,
        tipoDeConta = when (tipoDeConta) {
            UNKNOW_TIPO_CONTA -> null
            else -> TipoConta.valueOf(tipoDeConta.name) // 1
        }
    )
}