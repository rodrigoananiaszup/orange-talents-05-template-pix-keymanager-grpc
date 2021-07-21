package br.com.zup.edu.rodrigo.pix

import java.time.LocalDateTime
import java.util.*
import javax.persistence.*
import javax.validation.Valid
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

@Entity
@Table(uniqueConstraints = [UniqueConstraint(
    name = "uk_chave_pix",
    columnNames = ["chave"]
)])
class ChavePix(
    @field:NotNull
    @Column(nullable = false)
    val clienteId: UUID,

    @field:NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val tipoDeChave: TipoChave,

    @field:NotBlank
    @Column(unique = true, nullable = false)
    var chave: String,

    @field:NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val tipoDeConta: TipoConta,

    @field:Valid
    @Embedded
    val conta: ContaAssociada
) {
    @Id
    @GeneratedValue
    val id: UUID? = null

    @Column(nullable = false)
    val criadaEm: LocalDateTime = LocalDateTime.now()

    override fun toString(): String {
        return "ChavePix(clienteId=$clienteId, tipo=$tipoDeChave, chave='$chave', tipoDeConta=$tipoDeConta, conta=$conta, id=$id, criadaEm=$criadaEm)"
    }

    fun pertenceAo(clienteId: UUID) = this.clienteId.equals(clienteId)



    fun isAleatoria(): Boolean {
        return tipoDeChave == TipoChave.ALEATORIA
    }


    fun atualiza(chave: String): Boolean {
        if (isAleatoria()) {
            this.chave = chave
            return true
        }
        return false
    }

}
