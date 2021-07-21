package br.com.zup.edu.rodrigo.pix

import br.com.zup.edu.rodrigo.pix.ChavePix
import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository
import java.util.*

@Repository
interface ChavePixRepository : JpaRepository<ChavePix, UUID> {

    fun existsByChave(chave: String): Boolean

    fun findByIdAndClienteId(id: UUID, clientId: UUID): Optional<ChavePix>

    fun findByChave(chave: String): Optional<ChavePix>

}
