package br.com.zup.edu.rodrigo.pix.registra

import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository
import java.util.*

@Repository
interface ChavePixRepository : JpaRepository<ChavePix, UUID> {

    fun existsByChave(chave: String): Boolean

    fun findByIdAndClienteId(id: UUID, clientId: UUID): Optional<ChavePix>

}
