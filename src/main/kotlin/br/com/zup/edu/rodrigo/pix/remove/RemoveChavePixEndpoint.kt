package br.com.zup.edu.rodrigo.pix.remove

import br.com.zup.edu.rodrigo.PixKeyManagerRemoveGrpcServiceGrpc
import br.com.zup.edu.rodrigo.RemoveChavePixRequest
import br.com.zup.edu.rodrigo.RemoveChavePixResponse
import br.com.zup.edu.rodrigo.shared.grpc.ErrorHandler
import io.grpc.stub.StreamObserver
import javax.inject.Inject
import javax.inject.Singleton

@ErrorHandler
@Singleton
class RemoveChavePixEndpoint(
    @Inject val removeChavePix: RemoveChavePix) : PixKeyManagerRemoveGrpcServiceGrpc.PixKeyManagerRemoveGrpcServiceImplBase() {

    override fun remove(request: RemoveChavePixRequest, responseObserver: StreamObserver<RemoveChavePixResponse>) {
        val dto = request.paraRemoveChavePixDTO()
        val chaveRemovida = removeChavePix.remove(dto)
        responseObserver.onNext(
            RemoveChavePixResponse.newBuilder()
                .setPixId(chaveRemovida.id.toString())
                .build()
        )
        responseObserver.onCompleted()
    }
}