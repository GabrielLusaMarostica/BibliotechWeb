package bibliotech.servico;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;

import bibliotech.excecao.RegraNegocioException;
import bibliotech.model.Usuario;
import bibliotech.repositorio.memoria.UsuarioMemoria;
import org.junit.Before;
import org.junit.Test;

public class AutenticacaoServicoTest {

    private AutenticacaoServico servico;

    @Before
    public void preparar() {
        servico = new AutenticacaoServico(new UsuarioMemoria(), new CodificadorSenhaPbkdf2(1_000));
    }

    @Test
    public void usuarioCadastradoConsegueFazerLogin() {
        servico.cadastrar("gabriel", "senha-segura-1");
        assertEquals("gabriel", servico.autenticar("gabriel", "senha-segura-1").getLogin());
    }

    @Test
    public void senhaNaoEGuardadaEmTextoPuro() {
        Usuario u = servico.cadastrar("gabriel", "senha-segura-1");
        assertFalse(u.getSenhaHash().contains("senha-segura-1"));
    }

    @Test
    public void senhaErradaELoginInexistenteDaoAMesmaMensagem() {
        servico.cadastrar("gabriel", "senha-segura-1");
        RegraNegocioException senhaErrada = assertThrows(RegraNegocioException.class,
                () -> servico.autenticar("gabriel", "outra-senha"));
        RegraNegocioException semUsuario = assertThrows(RegraNegocioException.class,
                () -> servico.autenticar("ninguem", "senha-segura-1"));
        assertEquals(senhaErrada.getMessage(), semUsuario.getMessage());
    }

    @Test
    public void loginOuSenhaVaziosSaoRecusados() {
        assertThrows(RegraNegocioException.class, () -> servico.autenticar("", "x"));
        assertThrows(RegraNegocioException.class, () -> servico.autenticar(null, "x"));
        assertThrows(RegraNegocioException.class, () -> servico.autenticar("gabriel", null));
    }

    @Test
    public void senhaComMenosDeSeisCaracteresNaoEAceita() {
        assertThrows(RegraNegocioException.class, () -> servico.cadastrar("novo", "12345"));
    }

    @Test
    public void loginDuplicadoNaoEAceito() {
        servico.cadastrar("gabriel", "senha-segura-1");
        assertThrows(RegraNegocioException.class, () -> servico.cadastrar("gabriel", "outra-senha-2"));
    }

    @Test
    public void loginObrigatorioNoCadastro() {
        assertThrows(RegraNegocioException.class, () -> servico.cadastrar("  ", "senha-segura-1"));
    }
}
