package br.com.zup.edu.rodrigo.pix.registra

import br.com.zup.edu.rodrigo.PixKeyManagerRegistraGrpcServiceGrpc
import br.com.zup.edu.rodrigo.RegistraChavePixRequest
import br.com.zup.edu.rodrigo.RegistraChavePixResponse
import br.com.zup.edu.rodrigo.shared.grpc.ErrorHandler
import io.grpc.stub.StreamObserver
import javax.inject.Inject
import javax.inject.Singleton

@ErrorHandler
@Singleton
class RegistraChavePixEndpoint(
    @Inject val registraChave: RegistraChave
) : PixKeyManagerRegistraGrpcServiceGrpc.PixKeyManagerRegistraGrpcServiceImplBase() {

    override fun registra(
        request: RegistraChavePixRequest,
        responseObserver: StreamObserver<RegistraChavePixResponse>
    ) {
        val novaChavePix = request.paraNovaChavePix()
        val chaveCriada = registraChave.registra(novaChavePix)
        responseObserver.onNext(
            RegistraChavePixResponse.newBuilder()
                .setPixId(chaveCriada.id.toString())
                .build()
        )
        responseObserver.onCompleted()
    }
}