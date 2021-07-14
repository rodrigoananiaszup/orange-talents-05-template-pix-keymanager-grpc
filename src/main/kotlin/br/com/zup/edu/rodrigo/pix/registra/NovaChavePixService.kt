package br.com.zup.edu.rodrigo.pix.registra

import br.com.zup.edu.rodrigo.integration.itau.ItauContasClient
import io.micronaut.validation.Validated
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton
import javax.transaction.Transactional
import javax.validation.Valid

@Validated
@Singleton
class NovaChavePixService(
    @Inject val chavePixRepository: ChavePixRepository,
    @Inject val itauContasClient: ItauContasClient
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
        logger.info("Chave salva com sucesso: ${chave.id}")
        return chave
    }

}
