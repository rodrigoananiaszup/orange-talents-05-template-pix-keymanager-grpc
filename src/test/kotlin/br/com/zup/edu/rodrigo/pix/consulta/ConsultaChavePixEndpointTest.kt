package br.com.zup.edu.rodrigo.pix.consulta

import br.com.zup.edu.rodrigo.ConsultaChavePixRequest
import br.com.zup.edu.rodrigo.PixKeyManagerConsultaGrpcServiceGrpc
import br.com.zup.edu.rodrigo.integration.bcb.*
import br.com.zup.edu.rodrigo.pix.ChavePix
import br.com.zup.edu.rodrigo.pix.ChavePixRepository
import br.com.zup.edu.rodrigo.pix.ContaAssociada
import br.com.zup.edu.rodrigo.pix.TipoChave
import br.com.zup.edu.rodrigo.pix.TipoConta.CONTA_CORRENTE
import br.com.zup.edu.rodrigo.pix.registra.RegistraChaveEndpointTest.Companion.CLIENTE_ID
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.http.HttpResponse
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject

@MicronautTest(transactional = false)
internal class ConsultaChavePixEndpointTest(
    val chavePixRepository: ChavePixRepository,
    val clientGrpc: PixKeyManagerConsultaGrpcServiceGrpc
    .PixKeyManagerConsultaGrpcServiceBlockingStub
) {
    @Inject
    lateinit var bancoCentralClient: BancoCentralClient

    companion object {
        val CLIENT_ID: UUID = UUID.randomUUID()
    }

    @BeforeEach
    internal fun setUp() {
        chavePixRepository.save(
            chave(
                tipo = TipoChave.EMAIL,
                chave = "rodrigo.ananias@zup.com.br",
                clienteId = CLIENTE_ID
            )
        )
        chavePixRepository.save(chave(tipo = TipoChave.CPF, chave = "42353525806", clienteId = UUID.randomUUID()))
        chavePixRepository.save(chave(tipo = TipoChave.ALEATORIA, chave = "randomkey-3", clienteId = CLIENTE_ID))
        chavePixRepository.save(chave(tipo = TipoChave.CELULAR, chave = "+5517981664479", clienteId = CLIENTE_ID))
    }

    @AfterEach
    fun cleanUp() {
        chavePixRepository.deleteAll()
    }

    @Test
    fun `deve consultar chave por pixId e clienteId`() {
        // cenário
        val chaveExistente = chavePixRepository.findByChave("+5517981664479").get()

        // ação
        val response = clientGrpc.consulta(
            ConsultaChavePixRequest.newBuilder()
                .setPixId(
                    ConsultaChavePixRequest.FiltroPorPixId.newBuilder()
                        .setPixId(chaveExistente.id.toString())
                        .setClienteId(chaveExistente.clienteId.toString())
                        .build()
                ).build()
        )

        // validação
        with(response) {
            assertEquals(chaveExistente.id.toString(), this.pixId)
            assertEquals(chaveExistente.clienteId.toString(), this.clienteId)
            assertEquals(chaveExistente.tipoDeChave.name, this.chave.tipo.name)
            assertEquals(chaveExistente.chave, this.chave.chave)
        }
    }

    @Test
    fun `nao deve carregar chave por pixId e clienteId quando filtro invalido`() {
        // ação
        val thrown = assertThrows<StatusRuntimeException> {
            clientGrpc.consulta(ConsultaChavePixRequest.newBuilder()
                .setPixId(
                    ConsultaChavePixRequest.FiltroPorPixId.newBuilder()
                    .setPixId("")
                    .setClienteId("")
                    .build()
                ).build())
        }

        // validação
        with(thrown) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("pixId: não deve estar em branco, clienteId: não deve estar em branco", status.description)
        }
    }

    @Test
    fun `nao deve carregar chave por pixId e clienteId quando registro nao existir`() {
        // ação
        val pixIdNaoExistente = UUID.randomUUID().toString()
        val clienteIdNaoExistente = UUID.randomUUID().toString()
        val thrown = assertThrows<StatusRuntimeException> {
            clientGrpc.consulta(ConsultaChavePixRequest.newBuilder()
                .setPixId(
                    ConsultaChavePixRequest.FiltroPorPixId.newBuilder()
                    .setPixId(pixIdNaoExistente)
                    .setClienteId(clienteIdNaoExistente)
                    .build()
                ).build())
        }

        // validação
        with(thrown) {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("Chave Pix não encontrada", status.description)
        }
    }

    @Test
    fun `deve carregar chave por valor da chave quando registro existir localmente`() {
        // cenário
        val chaveExistente = chavePixRepository.findByChave("rodrigo.ananias@zup.com.br").get()

        // ação
        val response = clientGrpc.consulta(ConsultaChavePixRequest.newBuilder()
            .setChave("rodrigo.ananias@zup.com.br")
            .build())

        // validação
        with(response) {
            assertEquals(chaveExistente.id.toString(), this.pixId)
            assertEquals(chaveExistente.clienteId.toString(), this.clienteId)
            assertEquals(chaveExistente.tipoDeChave.name, this.chave.tipo.name)
            assertEquals(chaveExistente.chave, this.chave.chave)
        }
    }


    @Test
    fun `nao deve carregar chave por valor da chave quando filtro invalido`() {
        // ação
        val thrown = assertThrows<StatusRuntimeException> {
            clientGrpc.consulta(ConsultaChavePixRequest.newBuilder().setChave("").build())
        }

        // validação
        with(thrown) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("chave: não deve estar em branco", status.description)
        }
    }

    @Test
    fun `nao deve carregar chave quando filtro invalido`() {

        // ação
        val thrown = assertThrows<StatusRuntimeException> {
            clientGrpc.consulta(ConsultaChavePixRequest.newBuilder().build())
        }

        // validação
        with(thrown) {
            Assertions.assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            Assertions.assertEquals("Chave Pix inválida ou não informada", status.description)
        }
    }


}


@MockBean(BancoCentralClient::class)
fun bancoCentralClient(): BancoCentralClient? {
    return mock(BancoCentralClient::class.java)
}

@Factory
class Clients {
    @Bean
    fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel):
            PixKeyManagerConsultaGrpcServiceGrpc.PixKeyManagerConsultaGrpcServiceBlockingStub {
        return PixKeyManagerConsultaGrpcServiceGrpc.newBlockingStub(channel)
    }
}

private fun chave(
    tipo: TipoChave,
    chave: String = UUID.randomUUID().toString(),
    clienteId: UUID = UUID.randomUUID(),
): ChavePix {
    return ChavePix(
        clienteId = clienteId,
        tipoDeChave = tipo,
        chave = chave,
        tipoDeConta = CONTA_CORRENTE,
        conta = ContaAssociada(
            instituicao = "UNIBANCO ITAU",
            nomeDoTitular = "Rafael Ponte",
            cpfDoTitular = "12345678900",
            agencia = "1218",
            numeroDaConta = "123456"
        )
    )
}

private fun pixKeyDetailsResponse(): PixKeyDetailsResponse {
    return PixKeyDetailsResponse(
        keyType = PixKeyType.EMAIL,
        key = "user.from.another.bank@santander.com.br",
        bankAccount = bankAccount(),
        owner = owner(),
        createdAt = LocalDateTime.now()
    )
}

private fun bankAccount(): BankAccount {
    return BankAccount(
        participant = "90400888",
        branch = "9871",
        accountNumber = "987654",
        accountType = BankAccount.AccountType.SVGS
    )
}

private fun owner(): Owner {
    return Owner(
        type = Owner.OwnerType.NATURAL_PERSON,
        name = "Another User",
        taxIdNumber = "12345678901"
    )
}
