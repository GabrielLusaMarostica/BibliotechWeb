package bibliotech.servico;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class CodificadorSenhaPbkdf2Test {

    // poucas iterações para o teste rodar rápido; em produção usa-se o padrão (600.000)
    private final CodificadorSenha codificador = new CodificadorSenhaPbkdf2(1_000);

    @Test
    public void hashNaoContemASenhaOriginal() {
        assertFalse(codificador.codificar("senha-segura-1").contains("senha-segura-1"));
    }

    @Test
    public void senhaCorretaConfereComOHash() {
        String hash = codificador.codificar("senha-segura-1");
        assertTrue(codificador.confere("senha-segura-1", hash));
    }

    @Test
    public void senhaErradaNaoConfere() {
        String hash = codificador.codificar("senha-segura-1");
        assertFalse(codificador.confere("senha-segura-2", hash));
        assertFalse(codificador.confere("", hash));
    }

    @Test
    public void mesmaSenhaGeraHashesDiferentesPorCausaDoSal() {
        assertNotEquals(codificador.codificar("abc123"), codificador.codificar("abc123"));
    }

    @Test
    public void hashMalFormadoNuncaConfere() {
        assertFalse(codificador.confere("x", "isso-nao-e-um-hash"));
        assertFalse(codificador.confere("x", "1000:%%%:%%%"));
        assertFalse(codificador.confere("x", "abc:AAAA:AAAA"));
    }

    @Test
    public void hashGuardaAsIteracoesUsadas() {
        assertEquals("1000", codificador.codificar("x").split(":")[0]);
    }
}
