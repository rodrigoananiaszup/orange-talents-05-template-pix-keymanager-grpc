package br.com.zup.edu.rodrigo.integration.bcb

import br.com.zup.edu.rodrigo.pix.registra.ChavePix
import br.com.zup.edu.rodrigo.pix.registra.ContaAssociada
import br.com.zup.edu.rodrigo.pix.registra.TipoChave
import br.com.zup.edu.rodrigo.pix.registra.TipoConta
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Delete
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.Post
import io.micronaut.http.client.annotation.Client
import java.time.LocalDateTime

@Client("\${bcb.pix.url}")
interface BancoCentralClient {

    @Post(
        "/api/v1/pix/keys",
        produces = [MediaType.APPLICATION_XML],
        consumes = [MediaType.APPLICATION_XML],
    )
    fun create(@Body request: CreatePixRequest): HttpResponse<CreatePixResponse>

    @Delete(
        "/api/v1/pix/keys/{key}",
        produces = [MediaType.APPLICATION_XML],
        consumes = [MediaType.APPLICATION_XML],
    )
    fun delete(@PathVariable key: String, @Body request: DeletePixRequest)
            : HttpResponse<DeletePixResponse>
}

data class DeletePixRequest(
    val key: String,
    val participant: String = ContaAssociada.ITAU_UNIBANCO_ISBP
)

data class DeletePixResponse(
    val key: String,
    val participant: String,
    val deletedAt: LocalDateTime
)

data class CreatePixRequest(
    val keyType: PixKeyType,
    val key: String,
    val bankAccount: BankAccount,
    val owner: Owner
) {
    companion object {
        fun of(chavePix: ChavePix): CreatePixRequest {
            return CreatePixRequest(
                keyType = PixKeyType.by(chavePix.tipoChave!!),
                key = chavePix.chave,
                bankAccount = BankAccount(
                    participant = ContaAssociada.ITAU_UNIBANCO_ISBP,
                    branch = chavePix.conta.agencia,
                    accountNumber = chavePix.conta.numero,
                    accountType = BankAccount.AccountType.by(chavePix.tipoConta!!),
                ),
                owner = Owner(
                    type = Owner.OwnerType.NATURAL_PERSON,
                    name = chavePix.conta.titular.nomeTitular,
                    taxIdNumber = chavePix.conta.titular.cpf
                )

            )
        }
    }
}

data class CreatePixResponse(
    val keyType: PixKeyType,
    val key: String,
    val bankAccount: BankAccount,
    val owner: Owner,
    val createdAt: LocalDateTime
)

enum class PixKeyType {
    CPF,
    CNPJ,
    PHONE,
    EMAIL,
    RANDOM;

    companion object {
        fun by(tipoChave: TipoChave): PixKeyType {
            return when (tipoChave) {
                TipoChave.CPF -> CPF
                TipoChave.CELULAR -> PHONE
                TipoChave.EMAIL -> EMAIL
                TipoChave.ALEATORIA -> RANDOM
            }
        }
    }
}

data class BankAccount(
    val participant: String,
    val branch: String,
    val accountNumber: String,
    val accountType: AccountType
) {
    enum class AccountType {
        CACC,
        SVGS;

        companion object {
            fun by(tipoConta: TipoConta): AccountType {
                return when (tipoConta) {
                    TipoConta.CONTA_POUPANCA -> SVGS
                    TipoConta.CONTA_CORRENTE -> CACC
                }
            }
        }
    }
}

data class Owner(
    val type: OwnerType,
    val name: String,
    val taxIdNumber: String
) {
    enum class OwnerType {
        NATURAL_PERSON,
        LEGAL_PERSON;
    }
}



