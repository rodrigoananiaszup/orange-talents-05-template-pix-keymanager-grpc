package br.com.zup.edu.rodrigo.pix.remove

import br.com.zup.edu.rodrigo.RemoveChavePixRequest

fun RemoveChavePixRequest.paraRemoveChavePixDTO(): RemoveChavePixDto {
    return RemoveChavePixDto(
        clienteId = this.clienteId,
        pixId = this.pixId
    )
}