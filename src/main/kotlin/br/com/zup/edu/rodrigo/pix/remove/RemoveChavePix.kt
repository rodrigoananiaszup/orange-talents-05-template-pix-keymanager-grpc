package br.com.zup.edu.rodrigo.pix.remove

import br.com.zup.edu.rodrigo.pix.registra.ChavePix
import br.com.zup.edu.rodrigo.pix.registra.ChavePixRepository
import io.micronaut.validation.Validated
import javax.inject.Inject
import org.slf4j.LoggerFactory
import java.util.*
import javax.inject.Singleton
import javax.validation.Valid


@Singleton
@Validated
class RemoveChavePix (@Inject val chavePixRepository: ChavePixRepository){

   private val logger = LoggerFactory.getLogger(this::class.java)

    fun remove(@Valid dto: RemoveChavePixDto): ChavePix {

        val possivelChavePix = chavePixRepository.findByIdAndClienteId(
            id = UUID.fromString(dto.pixId),
            clientId = UUID.fromString(dto.clienteId)
        )

        if (possivelChavePix.isEmpty) {
            throw ChavePixNaoEncontradaException("Chave Pix não encontrada.")
        }
        val chavePix = possivelChavePix.get()
        chavePixRepository.delete(chavePix)
        logger.info("Chave Pix (${chavePix.id}) removida.")

        return chavePix
    }
}