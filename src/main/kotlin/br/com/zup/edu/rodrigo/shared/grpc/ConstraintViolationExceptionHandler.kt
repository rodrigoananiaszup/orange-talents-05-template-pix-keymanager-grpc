package br.com.zup.edu.rodrigo.shared.grpc

import io.grpc.Status
import javax.inject.Singleton
import javax.validation.ConstraintViolationException

@Singleton
class ConstraintViolationExceptionHandler : ExceptionHandler<ConstraintViolationException> {
    override fun handle(e: ConstraintViolationException): ExceptionHandler.StatusWithDetails {
        return ExceptionHandler.StatusWithDetails(Status.INVALID_ARGUMENT.withDescription(e.message))
    }

    override fun supports(e: Exception): Boolean {
        return e is ConstraintViolationException
    }

}