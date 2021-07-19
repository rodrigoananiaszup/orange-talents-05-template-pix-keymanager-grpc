package br.com.zup.edu.rodrigo.pix.registra

import br.com.zup.edu.rodrigo.PixKeyManagerRegistraGrpcServiceGrpc
import br.com.zup.edu.rodrigo.RegistraChavePixRequest
import br.com.zup.edu.rodrigo.TipoChave.*
import br.com.zup.edu.rodrigo.TipoConta.CONTA_CORRENTE
import br.com.zup.edu.rodrigo.TipoConta.CONTA_POUPANCA
import br.com.zup.edu.rodrigo.integration.itau.DadosDaContaResponse
import br.com.zup.edu.rodrigo.integration.itau.ItauContasClient
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
internal class RegistraChaveEndpointTest(
    private val chavePixRepository: ChavePixRepository,
    private val clientGrpc: PixKeyManagerRegistraGrpcServiceGrpc
    .PixKeyManagerRegistraGrpcServiceBlockingStub
) {
    @field:Inject
    lateinit var itauContasClient: ItauContasClient


    //cenario
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
    fun `deve registrar chave pix tipo celular`() {

        val request = RegistraChavePixRequest.newBuilder()
            .setClienteId("c56dfef4-7901-44fb-84e2-a2cefb157890")
            .setTipoDeChave(CELULAR)
            .setChave("+5517981664479")
            .setTipoDeConta(CONTA_CORRENTE)
            .build()

        val itauResponse = DadosDaContaResponse(
            tipo = TipoConta.CONTA_CORRENTE.toString(),
            instituicao = DadosDaContaResponse.InstituicaoResponse(
                nome = "Instituicao Itau Teste",
                ispb = "60701190"
            ),
            agencia = "0001",
            numero = "291900",
            titular = DadosDaContaResponse.TitularResponse(
                id = "c56dfef4-7901-44fb-84e2-a2cefb157890",
                nome = "Titular Teste",
                cpf = "42353525808"
            )
        )

        Mockito.`when`(itauContasClient.buscaContaPorTipo(request.clienteId, request.tipoDeConta.toString()))
            .thenReturn(HttpResponse.ok(itauResponse))

        val response = clientGrpc.registra(request)

        with(response) {
            assertNotNull(pixId)
            assertTrue(chavePixRepository.existsById(UUID.fromString(pixId)))
        }
    }

    @Test
    fun `deve registrar chave pix tipo cpf`() {


        //Ação
        val request = RegistraChavePixRequest.newBuilder()
            .setClienteId("c56dfef4-7901-44fb-84e2-a2cefb157890")
            .setTipoDeChave(CPF)
            .setChave("67116944060")
            .setTipoDeConta(CONTA_CORRENTE)
            .build()

        val itauResponse = DadosDaContaResponse(
            tipo = TipoConta.CONTA_CORRENTE.toString(),
            instituicao = DadosDaContaResponse.InstituicaoResponse(
                nome = "Instituicao Itau Teste",
                ispb = "60701190"
            ),
            agencia = "0001",
            numero = "291900",
            titular = DadosDaContaResponse.TitularResponse(
                id = "c56dfef4-7901-44fb-84e2-a2cefb157890",
                nome = "Titular Teste",
                cpf = "42353525808"
            )
        )

        Mockito.`when`(itauContasClient.buscaContaPorTipo(request.clienteId, request.tipoDeConta.toString()))
            .thenReturn(HttpResponse.ok(itauResponse))

        val response = clientGrpc.registra(request)

        //Validação
        with(response) {
            assertNotNull(pixId)
            assertTrue(chavePixRepository.existsById(UUID.fromString(pixId)))
        }
    }

    @Test
    fun `deve registrar chave pix tipo email`() {

        val request = RegistraChavePixRequest.newBuilder()
            .setClienteId("c56dfef4-7901-44fb-84e2-a2cefb157890")
            .setTipoDeChave(EMAIL)
            .setChave("rodrigo.ananias@zup.com.br")
            .setTipoDeConta(CONTA_CORRENTE)
            .build()

        val itauResponse = DadosDaContaResponse(
            tipo = TipoConta.CONTA_CORRENTE.toString(),
            instituicao = DadosDaContaResponse.InstituicaoResponse(
                nome = "Instituicao Itau Teste",
                ispb = "60701190"
            ),
            agencia = "0001",
            numero = "291900",
            titular = DadosDaContaResponse.TitularResponse(
                id = "c56dfef4-7901-44fb-84e2-a2cefb157890",
                nome = "Titular Teste",
                cpf = "42353525808"
            )
        )

        Mockito.`when`(itauContasClient.buscaContaPorTipo(request.clienteId, request.tipoDeConta.toString()))
            .thenReturn(HttpResponse.ok(itauResponse))

        val response = clientGrpc.registra(request)

        with(response) {
            assertNotNull(pixId)
            assertTrue(chavePixRepository.existsById(UUID.fromString(pixId)))
        }
    }

    @Test
    fun `deve registrar chave pix tipo aleatoria`() {

        val request = RegistraChavePixRequest.newBuilder()
            .setClienteId("c56dfef4-7901-44fb-84e2-a2cefb157890")
            .setTipoDeChave(ALEATORIA)
            .setTipoDeConta(CONTA_CORRENTE)
            .build()

        val itauResponse = DadosDaContaResponse(
            tipo = TipoConta.CONTA_CORRENTE.toString(),
            instituicao = DadosDaContaResponse.InstituicaoResponse(
                nome = "Instituicao Itau Teste",
                ispb = "60701190"
            ),
            agencia = "0001",
            numero = "291900",
            titular = DadosDaContaResponse.TitularResponse(
                id = "c56dfef4-7901-44fb-84e2-a2cefb157890",
                nome = "Titular Teste",
                cpf = "42353525808"
            )
        )

        Mockito.`when`(itauContasClient.buscaContaPorTipo(request.clienteId, request.tipoDeConta.toString()))
            .thenReturn(HttpResponse.ok(itauResponse))

        val response = clientGrpc.registra(request)

        with(response) {
            assertNotNull(pixId)
            assertTrue(chavePixRepository.existsById(UUID.fromString(pixId)))
        }
    }

    /**
     * FLUXO ALTERNATIVO
     */

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
            assertEquals("registra.novaChave: Chave Pix inválida", status.description)
        }
    }

    @Test
    fun `nao deve adicionar uma chave pix existente`() {

        val chaveExistente = ChavePix(
            clienteId = UUID.fromString("2ac09233-21b2-4276-84fb-d83dbd9f8bab"),
            tipoChave = TipoChave.EMAIL,
            chave = "teste@email.com",
            tipoConta = TipoConta.CONTA_POUPANCA,
            conta = ContaAssociada(
                tipo = TipoConta.CONTA_CORRENTE.toString(),
                instituicao = ContaAssociada.Instituicao(
                    nomeInstituicao = "INSTITUICAO TESTE",
                    ispb = "60701190"
                ),
                agencia = "0001",
                numero = "291900",
                titular = ContaAssociada.Titular(
                    titularId = UUID.fromString("2ac09233-21b2-4276-84fb-d83dbd9f8bab"),
                    nomeTitular = "Titular Teste",
                    cpf = "83082363083"
                )
            )
        )

        chavePixRepository.save(chaveExistente)

        val request = RegistraChavePixRequest.newBuilder()
            .setClienteId("2ac09233-21b2-4276-84fb-d83dbd9f8bab")
            .setTipoDeChave(EMAIL)
            .setChave(chaveExistente.chave)
            .setTipoDeConta(CONTA_POUPANCA)
            .build()

        val error = assertThrows<StatusRuntimeException> {
            clientGrpc.registra(request)
        }

        with(error) {
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
            assertTrue(status.description!!.contains("registra.novaChave.tipoConta: não deve ser nulo"))
            assertTrue(status.description!!.contains("registra.novaChave: Chave Pix inválida"))
            assertTrue(status.description!!.contains("registra.novaChave.clienteId: não deve estar em branco"))
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
            : PixKeyManagerRegistraGrpcServiceGrpc
    .PixKeyManagerRegistraGrpcServiceBlockingStub {
        return PixKeyManagerRegistraGrpcServiceGrpc.newBlockingStub(channel)
    }
}