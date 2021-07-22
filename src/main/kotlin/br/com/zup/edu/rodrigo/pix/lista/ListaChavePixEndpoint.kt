package br.com.zup.edu.rodrigo.pix.lista

import br.com.zup.edu.rodrigo.*
import br.com.zup.edu.rodrigo.pix.ChavePixRepository
import br.com.zup.edu.rodrigo.shared.grpc.ErrorHandler
import com.google.protobuf.Timestamp
import io.grpc.stub.StreamObserver
import java.time.ZoneId
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@ErrorHandler
@Singleton
class ListaChavesPixEndpoint(@Inject private val repository: ChavePixRepository)
    : PixKeyManagerListaGrpcServiceGrpc.PixKeyManagerListaGrpcServiceImplBase() {


    override fun lista(
        request: ListaChavePixRequest,
        responseObserver: StreamObserver<ListaChavePixResponse>,
    ) {

        if (request.clienteId.isNullOrBlank())
            throw IllegalArgumentException("Cliente ID não pode ser nulo ou vazio")

        val clienteId = UUID.fromString(request.clienteId)
        val chaves = repository.findAllByClienteId(clienteId).map { it ->
            ListaChavePixResponse.ChavePix.newBuilder()
                .setPixId(it.id.toString())
                .setTipo(TipoChave.valueOf(it.tipoDeChave.name))
                .setChave(it.chave)
                .setTipoDeConta(TipoConta.valueOf(it.tipoDeConta.name))
                .setCriadaEm(it.criadaEm.let {
                    val createdAt = it.atZone(ZoneId.of("UTC")).toInstant()
                    Timestamp.newBuilder()
                        .setSeconds(createdAt.epochSecond)
                        .setNanos(createdAt.nano)
                        .build()
                })
                .build()
        }

        responseObserver.onNext(
            ListaChavePixResponse.newBuilder()
                .setClienteId(clienteId.toString())
                .addAllChaves(chaves)
                .build()
        )
        responseObserver.onCompleted()
    }
}