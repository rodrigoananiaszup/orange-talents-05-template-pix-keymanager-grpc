package br.com.zup.edu.rodrigo.pix.consulta

import br.com.zup.edu.rodrigo.ConsultaChavePixResponse
import br.com.zup.edu.rodrigo.TipoChave
import br.com.zup.edu.rodrigo.TipoConta
import com.google.protobuf.Timestamp
import java.time.ZoneId

class CarregaChavePixResponseConverter {

    fun convert(chaveInfo: ChavePixInfo): ConsultaChavePixResponse {
        return ConsultaChavePixResponse.newBuilder()
            .setClienteId(chaveInfo.clienteId?.toString() ?: "")
            .setPixId(chaveInfo.pixId?.toString() ?: "")
            .setChave(ConsultaChavePixResponse.ChavePix
                .newBuilder()
                .setTipo(TipoChave.valueOf(chaveInfo.tipoDeChave.name))
                .setChave(chaveInfo.chave)
                .setConta(ConsultaChavePixResponse.ChavePix.ContaInfo.newBuilder()
                    .setTipo(TipoConta.valueOf(chaveInfo.tipoDeConta.name))
                    .setInstituicao(chaveInfo.conta.instituicao)
                    .setNomeDoTitular(chaveInfo.conta.nomeDoTitular)
                    .setCpfDoTitular(chaveInfo.conta.cpfDoTitular)
                    .setAgencia(chaveInfo.conta.agencia)
                    .setNumeroDaConta(chaveInfo.conta.numeroDaConta)
                    .build()
                )
                .setCriadaEm(chaveInfo.registradaEm.let {
                    val createdAt = it.atZone(ZoneId.of("UTC")).toInstant()
                    Timestamp.newBuilder()
                        .setSeconds(createdAt.epochSecond)
                        .setNanos(createdAt.nano)
                        .build()
                })
            )
            .build()
    }

}