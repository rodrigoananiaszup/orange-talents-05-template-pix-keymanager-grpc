package br.com.zup.edu.rodrigo.pix.remove

import br.com.zup.edu.rodrigo.PixKeyManagerRemoveGrpcServiceGrpc
import br.com.zup.edu.rodrigo.RemoveChavePixRequest
import br.com.zup.edu.rodrigo.RemoveChavePixResponse
import br.com.zup.edu.rodrigo.shared.grpc.ErrorHandler
import io.grpc.stub.StreamObserver
import javax.inject.Inject
import javax.inject.Singleton

@ErrorHandler // 1
@Singleton
class RemoveChaveEndpoint(@Inject private val service: RemoveChavePix,) // 1
    : PixKeyManagerRemoveGrpcServiceGrpc.PixKeyManagerRemoveGrpcServiceImplBase() { // 1

    // 5
    override fun remove(
        request: RemoveChavePixRequest, // 1
        responseObserver: StreamObserver<RemoveChavePixResponse>, // 1
    ) {

        service.remove(clienteId = request.clienteId, pixId = request.pixId)

        responseObserver.onNext(RemoveChavePixResponse.newBuilder() // 1
            .setClienteId(request.clienteId)
            .setPixId(request.pixId)
            .build())
        responseObserver.onCompleted()
    }

}