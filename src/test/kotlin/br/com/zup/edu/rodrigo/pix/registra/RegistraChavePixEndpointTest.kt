package br.com.zup.edu.rodrigo.pix.registra

import br.com.zup.edu.rodrigo.PixKeyManagerRegistraGrpcServiceGrpc
import br.com.zup.edu.rodrigo.RegistraChavePixRequest
import br.com.zup.edu.rodrigo.TipoChave.*
import br.com.zup.edu.rodrigo.TipoConta.CONTA_CORRENTE
import br.com.zup.edu.rodrigo.TipoConta.CONTA_POUPANCA
import br.com.zup.edu.rodrigo.integration.bcb.*
import br.com.zup.edu.rodrigo.integration.itau.DadosDaContaResponse
import br.com.zup.edu.rodrigo.integration.itau.InstituicaoResponse
import br.com.zup.edu.rodrigo.integration.itau.ItauContasClient
import br.com.zup.edu.rodrigo.integration.itau.TitularResponse
import io.grpc.Channel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.http.HttpResponse
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@MicronautTest(transactional = false)
internal class RegistraChaveEndpointTest(
    private val chavePixRepository: ChavePixRepository,
    private val clientGrpc: PixKeyManagerRegistraGrpcServiceGrpc
    .PixKeyManagerRegistraGrpcServiceBlockingStub
) {
    @field:Inject
    lateinit var itauContasClient: ItauContasClient

    @field:Inject
    lateinit var bancoCentralClient: BancoCentralClient

    companion object {
        val CLIENTE_ID: UUID = UUID.randomUUID()
    }

    @MockBean(BancoCentralClient::class)
    fun bancoCentralClientMock(): BancoCentralClient {
        return mock(BancoCentralClient::class.java)
    }

    @MockBean(ItauContasClient::class)
    fun itauContasClientMock(): ItauContasClient {
        return mock(ItauContasClient::class.java)
    }


    //cenario

    @BeforeEach
    fun setup() {
        chavePixRepository.deleteAll()
    }

    /**
     * Happy-Path
     */
    @Test
    fun `deve registrar nova chave pix`() {
        // cenário
        `when`(itauContasClient.buscaContaPorTipo(clienteId = CLIENTE_ID.toString(), tipo = "CONTA_CORRENTE"))
            .thenReturn(HttpResponse.ok(itauResponse()))

        `when`(bancoCentralClient.create(bancoCentralRequest()))
            .thenReturn(HttpResponse.created(bancoCentralResponse()))

        // ação
        val response = clientGrpc.registra(
            RegistraChavePixRequest.newBuilder()
                .setClienteId(CLIENTE_ID.toString())
                .setTipoDeChave(EMAIL)
                .setChave("rponte@gmail.com")
                .setTipoDeConta(CONTA_CORRENTE)
                .build()
        )

        // validação
        with(response) {
            assertEquals(CLIENTE_ID.toString(), clienteId)
            assertNotNull(pixId)
        }
    }


    @Test
    fun `nao deve registrar uma nova chave pix invalida`() {

        val request = RegistraChavePixRequest.newBuilder()
            .setClienteId("2ac09233-21b2-4276-84fb-d83dbd9f8bab")
            .setTipoDeChave(EMAIL)
            .setChave("rodrigo.ananias.zup.com.br")
            .setTipoDeConta(CONTA_POUPANCA)
            .build()

        val error = assertThrows<StatusRuntimeException> {
            clientGrpc.registra(request)
        }

        with(error) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("registra.novaChave.chave: chave Pix inválida (EMAIL)", status.description)
        }
    }

    @Test
    fun `nao deve adicionar uma chave pix existente`() {

        // cenário
        chavePixRepository.save(chave(
            tipo = TipoChave.CPF,
            chave = "42353525806",
            clienteId = CLIENTE_ID
        ))

        // ação
        val thrown = assertThrows<StatusRuntimeException> {
            clientGrpc.registra(RegistraChavePixRequest.newBuilder()
                .setClienteId(CLIENTE_ID.toString())
                .setTipoDeChave(CPF)
                .setChave("42353525806")
                .setTipoDeConta(CONTA_CORRENTE)
                .build())
        }

        // validação
        with(thrown) {
            assertEquals(Status.ALREADY_EXISTS.code, status.code)
        }
    }

    @Test
    fun `nao deve adicionar uma nova chave pix com dados invalidos`() {

        val request = RegistraChavePixRequest.newBuilder()
            .setClienteId("")
            .setTipoDeChave(CELULAR)
            .setChave("")
            .build()

        val error = assertThrows<StatusRuntimeException> {
            clientGrpc.registra(request)
        }

        with(error) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
        }
    }

    @Factory
    class ClientsFactoryTest {
        @Singleton
        fun blockingStubs(@GrpcChannel(GrpcServerChannel.NAME) channel: Channel)
                : PixKeyManagerRegistraGrpcServiceGrpc
        .PixKeyManagerRegistraGrpcServiceBlockingStub {
            return PixKeyManagerRegistraGrpcServiceGrpc.newBlockingStub(channel)
        }
    }

    private fun itauResponse(): DadosDaContaResponse {
        return DadosDaContaResponse(
            tipo = "CONTA_CORRENTE",
            instituicao = InstituicaoResponse("UNIBANCO ITAU SA", ContaAssociada.ITAU_UNIBANCO_ISPB),
            agencia = "1218",
            numero = "291900",
            titular = TitularResponse("Rafael Ponte", "63657520325")
        )
    }

    private fun bancoCentralRequest(): CreatePixKeyRequest {
        return CreatePixKeyRequest(
            keyType = PixKeyType.EMAIL,
            key = "rponte@gmail.com",
            bankAccount = bankAccount(),
            owner = owner()
        )
    }

    private fun bancoCentralResponse(): CreatePixKeyResponse {
        return CreatePixKeyResponse(
            keyType = PixKeyType.EMAIL,
            key = "rponte@gmail.com",
            bankAccount = bankAccount(),
            owner = owner(),
            createdAt = LocalDateTime.now()
        )
    }

    private fun bankAccount(): BankAccount {
        return BankAccount(
            participant = ContaAssociada.ITAU_UNIBANCO_ISPB,
            branch = "1218",
            accountNumber = "291900",
            accountType = BankAccount.AccountType.CACC
        )
    }

    private fun owner(): Owner {
        return Owner(
            type = Owner.OwnerType.NATURAL_PERSON,
            name = "Rafael Ponte",
            taxIdNumber = "63657520325"
        )
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
            tipoDeConta = TipoConta.CONTA_CORRENTE,
            conta = ContaAssociada(
                instituicao = "UNIBANCO ITAU",
                nomeDoTitular = "Rafael Ponte",
                cpfDoTitular = "63657520325",
                agencia = "1218",
                numeroDaConta = "291900"
            )
        )
    }
}