package br.com.zup.edu.rodrigo.pix.remove

import br.com.zup.edu.rodrigo.PixKeyManagerRemoveGrpcServiceGrpc
import br.com.zup.edu.rodrigo.RemoveChavePixRequest
import br.com.zup.edu.rodrigo.integration.bcb.BancoCentralClient
import br.com.zup.edu.rodrigo.integration.bcb.DeletePixKeyRequest
import br.com.zup.edu.rodrigo.integration.bcb.DeletePixKeyResponse
import br.com.zup.edu.rodrigo.pix.*
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
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject

@MicronautTest(transactional = false)
internal class RemoveChavePixEndpointTest(
    private val chavePixRepository: ChavePixRepository,
    val clientGrpc: PixKeyManagerRemoveGrpcServiceGrpc
    .PixKeyManagerRemoveGrpcServiceBlockingStub
) {

    @Inject
    lateinit var bcbClient: BancoCentralClient

    lateinit var CHAVE_EXISTENTE: ChavePix

    @BeforeEach
    fun setup() {
        CHAVE_EXISTENTE = chavePixRepository.save(
            chave(
                tipo = TipoChave.EMAIL,
                chave = "rodrigo.ananias@zup.com.br",
                clienteId = UUID.randomUUID()
            )
        )
    }

    @AfterEach
    fun cleanUp() {
        chavePixRepository.deleteAll()
    }

    @Test
    fun `deve remover chave pix existente`() {
        `when`(bcbClient.delete("rodrigo.ananias@zup.com.br", DeletePixKeyRequest("rodrigo.ananias@zup.com.br")))
            .thenReturn(
                HttpResponse.ok(
                    DeletePixKeyResponse(
                        key = "rodrigo.ananias@zup.com.br",
                        participant = ContaAssociada.ITAU_UNIBANCO_ISPB,
                        deletedAt = LocalDateTime.now()
                    )
                )
            )

        // ação
        val response = clientGrpc.remove(
            RemoveChavePixRequest.newBuilder()
                .setPixId(CHAVE_EXISTENTE.id.toString())
                .setClienteId(CHAVE_EXISTENTE.clienteId.toString())
                .build()
        )

        // validação
        with(response) {
            assertEquals(CHAVE_EXISTENTE.id.toString(), pixId)
            assertEquals(CHAVE_EXISTENTE.clienteId.toString(), clienteId)
        }
    }

    @Test
    fun `nao deve remover chave pix existente quando ocorrer algum erro no serviço do BCB`() {
        // cenário
        `when`(bcbClient.delete("rodrigo.ananias@zup.com.br", DeletePixKeyRequest("rodrigo.ananias@zup.com.br")))
            .thenReturn(HttpResponse.unprocessableEntity())

        // ação
        val thrown = assertThrows<StatusRuntimeException> {
            clientGrpc.remove(
                RemoveChavePixRequest.newBuilder()
                    .setPixId(CHAVE_EXISTENTE.id.toString())
                    .setClienteId(CHAVE_EXISTENTE.clienteId.toString())
                    .build()
            )
        }

        // validação
        with(thrown) {
            assertEquals(Status.FAILED_PRECONDITION.code, status.code)
            assertEquals("Erro ao remover chave Pix no Banco Central do Brasil (BCB)", status.description)
        }
    }

    @Test
    fun `nao deve remover chave pix quando chave inexistente`() {
        // cenário
        val pixIdNaoExistente = UUID.randomUUID().toString()

        // ação
        val thrown = assertThrows<StatusRuntimeException> {
            clientGrpc.remove(
                RemoveChavePixRequest.newBuilder()
                    .setPixId(pixIdNaoExistente)
                    .setClienteId(CHAVE_EXISTENTE.clienteId.toString())
                    .build()
            )
        }

        // validação
        with(thrown) {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("Chave Pix não encontrada ou não pertence ao cliente", status.description)
        }
    }

    @Test
    fun `nao deve remover chave pix quando chave existente mas pertence a outro cliente`() {
        // cenário
        val outroClienteId = UUID.randomUUID().toString()

        // ação
        val thrown = assertThrows<StatusRuntimeException> {
            clientGrpc.remove(
                RemoveChavePixRequest.newBuilder()
                    .setPixId(CHAVE_EXISTENTE.id.toString())
                    .setClienteId(outroClienteId)
                    .build()
            )
        }

        with(thrown) {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("Chave Pix não encontrada ou não pertence ao cliente", status.description)
        }
    }

    @Test
    fun `nao deve remover chave pix quando parametros inválidos`() {
        // ação
        val thrown = assertThrows<StatusRuntimeException> {
            clientGrpc.remove(RemoveChavePixRequest.newBuilder().build())
        }

        // validação
        with(thrown) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("remove.clienteId: não deve estar em branco, remove.pixId: não deve estar em branco", status.description)
        }
    }

    @MockBean(BancoCentralClient::class)
    fun bcbClient(): BancoCentralClient? {
        return mock(BancoCentralClient::class.java)
    }

    @Factory
    class Clients  {
        @Bean
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): PixKeyManagerRemoveGrpcServiceGrpc
        .PixKeyManagerRemoveGrpcServiceBlockingStub {
            return PixKeyManagerRemoveGrpcServiceGrpc.newBlockingStub(channel)
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
            tipoDeConta = TipoConta.CONTA_CORRENTE,
            conta = ContaAssociada(
                instituicao = "UNIBANCO ITAU",
                nomeDoTitular = "Rafael Ponte",
                cpfDoTitular = "12345678900",
                agencia = "1218",
                numeroDaConta = "123456"
            )
        )
    }
}