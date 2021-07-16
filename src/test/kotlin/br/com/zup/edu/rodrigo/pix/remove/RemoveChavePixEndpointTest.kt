package br.com.zup.edu.rodrigo.pix.remove

import br.com.zup.edu.rodrigo.*
import br.com.zup.edu.rodrigo.integration.itau.DadosDaContaResponse
import br.com.zup.edu.rodrigo.integration.itau.ItauContasClient
import br.com.zup.edu.rodrigo.pix.registra.*
import br.com.zup.edu.rodrigo.pix.registra.TipoChave
import br.com.zup.edu.rodrigo.pix.registra.TipoConta
import io.grpc.Channel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.http.HttpResponse
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.annotation.TransactionMode
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@MicronautTest(
    rollback = false,
    transactional = false,
    transactionMode = TransactionMode.SINGLE_TRANSACTION
)
internal class RemoveChavePixEndpointTest(
    private val chavePixRepository: ChavePixRepository,
    private val clientGrpc: PixKeyManagerRemoveGrpcServiceGrpc
    .PixKeyManagerRemoveGrpcServiceBlockingStub
) {

    @field:Inject
    lateinit var itauContasClient: ItauContasClient

    @BeforeEach
    fun setUp() {
        chavePixRepository.deleteAll()
    }

    @AfterEach
    internal fun cleanUp() {
        chavePixRepository.deleteAll()
    }

    /**
     * Happy-Path
     */

    @Test
    fun `deve remover uma chave pix`() {

        val chaveExistente = ChavePix(
            clienteId = UUID.fromString("2ac09233-21b2-4276-84fb-d83dbd9f8bab"),
            tipoChave = TipoChave.EMAIL,
            chave = "teste@email.com",
            tipoConta = TipoConta.CONTA_POUPANCA,
            conta = ContaAssociada(
                tipo = TipoConta.CONTA_CORRENTE.toString(),
                instituicao = ContaAssociada.Instituicao(
                    nomeInstituicao = "INSTITUICAO ITAU TESTE",
                    ispb = "60701190"
                ),
                agencia = "0001",
                numero = "291900",
                titular = ContaAssociada.Titular(
                    titularId = UUID.fromString("2ac09233-21b2-4276-84fb-d83dbd9f8bab"),
                    nomeTitular = "Titular Teste",
                    cpf = "83198870038"
                )
            )
        )

        chavePixRepository.save(chaveExistente)

        val request = RemoveChavePixRequest.newBuilder()
            .setClienteId("2ac09233-21b2-4276-84fb-d83dbd9f8bab")
            .setPixId(chaveExistente.id.toString())
            .build()

        val response = clientGrpc.remove(request)

        with(response) {
            val possivelChavePix = chavePixRepository.findById(UUID.fromString(pixId))
            assertTrue(possivelChavePix.isEmpty)
            assertEquals(0, chavePixRepository.count())
        }

    }

    /**
     * Fluxo alternativo
     */

    @Test
    fun `nao deve remover chave pix de cliente diferente`() {

        val chaveExistente = ChavePix(
            clienteId = UUID.fromString("2ac09233-21b2-4276-84fb-d83dbd9f8bab"),
            tipoChave = TipoChave.EMAIL,
            chave = "teste@email.com",
            tipoConta = TipoConta.CONTA_POUPANCA,
            conta = ContaAssociada(
                tipo = TipoConta.CONTA_CORRENTE.toString(),
                instituicao = ContaAssociada.Instituicao(
                    nomeInstituicao = "INSTITUICAO ITAU TESTE",
                    ispb = "60701190"
                ),
                agencia = "0001",
                numero = "291900",
                titular = ContaAssociada.Titular(
                    titularId = UUID.fromString("2ac09233-21b2-4276-84fb-d83dbd9f8bab"),
                    nomeTitular = "Titular Teste",
                    cpf = "83198870038"
                )
            )
        )

        chavePixRepository.save(chaveExistente)

        val request = RemoveChavePixRequest.newBuilder()
            .setClienteId("c56dfef4-7901-44fb-84e2-a2cefb157890")
            .setPixId(chaveExistente.id.toString())
            .build()

        val error = assertThrows<StatusRuntimeException> {
            clientGrpc.remove(request)
        }

        with(error) {

            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("Chave Pix não encontrada.", status.description)
        }

        assertEquals(1, chavePixRepository.count())
    }

    @Test
    fun `nao deve aceitar requisicoes com dados brancos`() {

        val request = RemoveChavePixRequest.newBuilder()
            .setClienteId("")
            .setPixId("")
            .build()

        val error = assertThrows<StatusRuntimeException> {
            clientGrpc.remove(request)
        }

        with(error) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
        }
    }

    @Test
    fun `nao deve aceitar requisicoes com dados invalidos`() {
        val request = RemoveChavePixRequest.newBuilder()
            .setClienteId("????")
            .setPixId("????")
            .build()

        val error = assertThrows<StatusRuntimeException> {
            clientGrpc.remove(request)
        }

        with(error) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
        }
    }

    @MockBean(ItauContasClient::class)
    fun itauContasClientMock(): ItauContasClient {
        return Mockito.mock(ItauContasClient::class.java)
    }
}

@Factory
class Clients {
    @Singleton
    fun blockingStubs(@GrpcChannel(GrpcServerChannel.NAME) channel: Channel)
            : PixKeyManagerRemoveGrpcServiceGrpc
    .PixKeyManagerRemoveGrpcServiceBlockingStub {
        return PixKeyManagerRemoveGrpcServiceGrpc.newBlockingStub(channel)
    }
}