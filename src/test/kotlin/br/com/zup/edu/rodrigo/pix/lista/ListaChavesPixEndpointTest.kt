package br.com.zup.edu.rodrigo.pix.lista

import br.com.zup.edu.rodrigo.ListaChavePixRequest
import br.com.zup.edu.rodrigo.PixKeyManagerListaGrpcServiceGrpc
import br.com.zup.edu.rodrigo.TipoChave
import br.com.zup.edu.rodrigo.pix.ChavePix
import br.com.zup.edu.rodrigo.pix.ChavePixRepository
import br.com.zup.edu.rodrigo.pix.ContaAssociada
import br.com.zup.edu.rodrigo.pix.TipoConta
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsInAnyOrder
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*

@MicronautTest(transactional = false)
internal class ListaChavesPixEndpointTest(
    val chavePixRepository: ChavePixRepository,
    val grpcClient: PixKeyManagerListaGrpcServiceGrpc.PixKeyManagerListaGrpcServiceBlockingStub,
) {

    companion object {
        val CLIENTE_ID = UUID.randomUUID()
    }


    @BeforeEach
    fun setup() {
        chavePixRepository.save(chave(tipo = br.com.zup.edu.rodrigo.pix.TipoChave.EMAIL, chave = "rodrigo.ananias@zup.com.br", clienteId = CLIENTE_ID))
        chavePixRepository.save(chave(tipo = br.com.zup.edu.rodrigo.pix.TipoChave.ALEATORIA, chave = "randomkey-2", clienteId = UUID.randomUUID()))
        chavePixRepository.save(chave(tipo = br.com.zup.edu.rodrigo.pix.TipoChave.ALEATORIA, chave = "randomkey-3", clienteId = CLIENTE_ID))
    }


    @AfterEach
    fun cleanUp() {
        chavePixRepository.deleteAll()
    }

    @Test
    fun `deve listar todas as chaves do cliente`() {
        // cenário
        val clienteId = CLIENTE_ID.toString()

        // ação
        val response = grpcClient.lista(
            ListaChavePixRequest.newBuilder()
                .setClienteId(clienteId)
                .build()
        )

        // validação
        with(response.chavesList) {
            assertThat(this, hasSize(2))
            assertThat(
                this.map { Pair(it.tipo, it.chave) }.toList(),
                containsInAnyOrder(
                    Pair(TipoChave.ALEATORIA, "randomkey-3"),
                    Pair(TipoChave.EMAIL, "rodrigo.ananias@zup.com.br" +
                            "")
                )
            )
        }
    }

    @Test
    fun `nao deve listar as chaves do cliente quando cliente nao possuir chaves`() {
        // cenário
        val clienteSemChaves = UUID.randomUUID().toString()

        // ação
        val response = grpcClient.lista(
            ListaChavePixRequest.newBuilder()
                .setClienteId(clienteSemChaves)
                .build()
        )

        // validação
        assertEquals(0, response.chavesCount)
    }

    @Test
    fun `nao deve listar todas as chaves do cliente quando clienteId for invalido`() {
        // cenário
        val clienteIdInvalido = ""

        // ação
        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.lista(
                ListaChavePixRequest.newBuilder()
                    .setClienteId(clienteIdInvalido)
                    .build()
            )
        }

        // validação
        with(thrown) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("Cliente ID não pode ser nulo ou vazio", status.description)
        }
    }

    @Factory
    class Clients {
        @Bean
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): PixKeyManagerListaGrpcServiceGrpc.PixKeyManagerListaGrpcServiceBlockingStub {
            return PixKeyManagerListaGrpcServiceGrpc.newBlockingStub(channel)
        }
    }

    private fun chave(
        tipo: br.com.zup.edu.rodrigo.pix.TipoChave,
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