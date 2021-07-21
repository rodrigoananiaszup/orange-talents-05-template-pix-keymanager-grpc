package br.com.zup.edu.rodrigo.pix.consulta

import br.com.zup.edu.rodrigo.ConsultaChavePixRequest
import br.com.zup.edu.rodrigo.ConsultaChavePixResponse
import br.com.zup.edu.rodrigo.PixKeyManagerConsultaGrpcServiceGrpc
import br.com.zup.edu.rodrigo.integration.bcb.BancoCentralClient
import br.com.zup.edu.rodrigo.pix.ChavePixRepository
import br.com.zup.edu.rodrigo.shared.grpc.ErrorHandler
import io.grpc.stub.StreamObserver
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.Validator

@ErrorHandler
@Singleton
class ConsultaChavePixEndpoint(
    @Inject private val repository: ChavePixRepository,
    @Inject private val bcbClient: BancoCentralClient,
    @Inject private val validator: Validator,
) : PixKeyManagerConsultaGrpcServiceGrpc.PixKeyManagerConsultaGrpcServiceImplBase() {


    override fun consulta(
        request: ConsultaChavePixRequest,
        responseObserver: StreamObserver<ConsultaChavePixResponse>,
    ) {

        val filtro = request.toModel(validator)
        val chaveInfo = filtro.filtra(repository = repository, bcbClient = bcbClient)

        responseObserver.onNext(CarregaChavePixResponseConverter().convert(chaveInfo))
        responseObserver.onCompleted()
    }
}