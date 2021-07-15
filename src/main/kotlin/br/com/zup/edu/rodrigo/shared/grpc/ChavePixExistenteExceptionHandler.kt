package br.com.zup.edu.rodrigo.shared.grpc

import br.com.zup.edu.rodrigo.pix.registra.ChavePixExistenteException
import io.grpc.Status
import javax.inject.Singleton

@Singleton
class ChavePixExistenteExceptionHandler : ExceptionHandler<ChavePixExistenteException> {

    override fun handle(e: ChavePixExistenteException): ExceptionHandler.StatusWithDetails {
        return ExceptionHandler.StatusWithDetails(Status.ALREADY_EXISTS.withDescription(e.message).withCause(e))
    }

    override fun supports(e: Exception): Boolean {
        return e is ChavePixExistenteException
    }


}