package br.com.zup.edu.rodrigo.pix.registra


import br.com.zup.edu.rodrigo.integration.bcb.BancoCentralClient
import br.com.zup.edu.rodrigo.integration.bcb.CreatePixRequest
import br.com.zup.edu.rodrigo.integration.itau.ItauContasClient
import io.micronaut.http.HttpStatus
import io.micronaut.validation.Validated
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton
import javax.transaction.Transactional
import javax.validation.Valid

@Validated
@Singleton
class RegistraChave(
    @Inject val chavePixRepository: ChavePixRepository,
    @Inject val itauContasClient: ItauContasClient,
    @Inject val bcbClient: BancoCentralClient
) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    @Transactional
    fun registra(@Valid novaChave: NovaChavePix): ChavePix {

        if (chavePixRepository.existsByChave(novaChave.chave)) {
            throw ChavePixExistenteException("Chave Pix ${novaChave.chave} já existente.")
        }

        val response = itauContasClient.buscaContaPorTipo(novaChave.clienteId, novaChave.tipoConta!!.name)
        val conta = response.body()?.toModel() ?: throw IllegalStateException("Cliente não encontrado no Itaú.")

        val chave = novaChave.toModel(conta)
        chavePixRepository.save(chave)

        //registra chave no BCB

        val bcbRequest = CreatePixRequest.of(chave).also {
            logger.info("Registrando nova chave Pix no Banco Central do Brasil: $it")
        }

        val bcbResponse = bcbClient.create(bcbRequest)
        if (bcbResponse.status != HttpStatus.CREATED) {
            throw java.lang.IllegalStateException("Erro ao registrar chave pix no bcb")
        }

        chave.atualiza(bcbResponse.body().key)

        logger.info("Chave salva com sucesso: ${chave.id}")
        return chave
    }

}