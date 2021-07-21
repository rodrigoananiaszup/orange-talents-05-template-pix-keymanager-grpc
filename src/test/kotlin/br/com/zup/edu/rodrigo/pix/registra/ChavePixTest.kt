package br.com.zup.edu.rodrigo.pix.registra

import br.com.zup.edu.rodrigo.pix.ChavePix
import br.com.zup.edu.rodrigo.pix.ContaAssociada
import br.com.zup.edu.rodrigo.pix.TipoChave
import br.com.zup.edu.rodrigo.pix.TipoConta
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.*

internal class ChavePixTest {

    companion object {
        val TIPOS_DE_CHAVE_EXCETO_ALEATORIO = TipoChave.values().filterNot { it == TipoChave.ALEATORIA }
    }

    @Test
    fun `chave deve ser do tipo aleatorio`() {
        val chavePix = novaChave(TipoChave.ALEATORIA)
        with(chavePix) {
            assertTrue(this.isAleatoria())
        }
    }

    @Test
    fun `chave nao deve ser do tipo aleatorio`() {
        TIPOS_DE_CHAVE_EXCETO_ALEATORIO.forEach { tipoChave ->
            val chavePix = novaChave(tipo = tipoChave)
            assertFalse(chavePix.isAleatoria())
        }
    }

    @Test
    fun `deve atualizar chave do tipo aleatorio`() {
        val valorAntigo = UUID.randomUUID().toString()
        val valorNovo = UUID.randomUUID().toString()
        val chavePix = novaChave(
            tipo = TipoChave.ALEATORIA,
            chave = valorAntigo
        )
        chavePix.atualiza(valorNovo)
        with(chavePix) {
            assertFalse(chavePix.chave.equals(valorAntigo))
            assertTrue(chavePix.chave.equals(valorNovo))
        }
    }

    @Test
    fun `nao deve atualizar chave quando tipo nao for aleatorio`() {
        val valorAntigo = UUID.randomUUID().toString()
        val valorNovo = UUID.randomUUID().toString()

        TIPOS_DE_CHAVE_EXCETO_ALEATORIO.forEach { tipoChave ->
            val chavePix = novaChave(
                tipo = tipoChave,
                chave = valorAntigo
            )
            chavePix.atualiza(valorNovo)
            with(chavePix) {
                assertTrue(chavePix.chave.equals(valorAntigo))
                assertFalse(chavePix.chave.equals(valorNovo))
            }
        }
    }

    private fun novaChave(
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
                cpfDoTitular = "63657520325",
                agencia = "1218",
                numeroDaConta = "291900"
            )
        )
    }
}