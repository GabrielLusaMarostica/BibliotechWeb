package bibliotech.servico;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import bibliotech.excecao.RecursoNaoEncontradoException;
import bibliotech.excecao.RegraNegocioException;
import bibliotech.model.Aluno;
import bibliotech.repositorio.memoria.AlunoMemoria;
import bibliotech.repositorio.memoria.EmprestimoMemoria;
import org.junit.Before;
import org.junit.Test;

public class AlunoServicoTest {

    private AlunoServico servico;

    @Before
    public void preparar() {
        servico = new AlunoServico(new AlunoMemoria(), new EmprestimoMemoria());
    }

    @Test
    public void cadastrarAtribuiId() {
        Aluno a = servico.cadastrar("Ana Souza", "A1", "2º ano", "ana@escola.com");
        assertTrue(a.getId() > 0);
        assertEquals("Ana Souza", servico.buscarPorId(a.getId()).getNome());
    }

    @Test
    public void matriculaDuplicadaERejeitada() {
        servico.cadastrar("Ana", "A1", "", "");
        assertThrows(RegraNegocioException.class, () -> servico.cadastrar("Outra", "A1", "", ""));
    }

    @Test
    public void nomeVazioERejeitado() {
        assertThrows(RegraNegocioException.class, () -> servico.cadastrar("  ", "A1", "", ""));
        assertTrue(servico.listar().isEmpty());
    }

    @Test
    public void atualizarMantendoAMesmaMatriculaEPermitido() {
        Aluno a = servico.cadastrar("Ana", "A1", "1º", "");
        servico.atualizar(a.getId(), "Ana Souza", "A1", "2º", "novo@contato.com");
        assertEquals("Ana Souza", servico.buscarPorId(a.getId()).getNome());
        assertEquals(1, servico.listar().size());
    }

    @Test
    public void atualizarParaMatriculaDeOutroAlunoERejeitado() {
        servico.cadastrar("Ana", "A1", "", "");
        Aluno b = servico.cadastrar("Bia", "B1", "", "");
        assertThrows(RegraNegocioException.class,
                () -> servico.atualizar(b.getId(), "Bia", "A1", "", ""));
    }

    @Test
    public void atualizarAlunoInexistenteLancaExcecao() {
        assertThrows(RecursoNaoEncontradoException.class,
                () -> servico.atualizar(99, "X", "X1", "", ""));
    }

    @Test
    public void excluirRemoveOAluno() {
        Aluno a = servico.cadastrar("Ana", "A1", "", "");
        servico.excluir(a.getId());
        assertThrows(RecursoNaoEncontradoException.class, () -> servico.buscarPorId(a.getId()));
    }

    @Test
    public void excluirAlunoInexistenteLancaExcecao() {
        assertThrows(RecursoNaoEncontradoException.class, () -> servico.excluir(99));
    }

    @Test
    public void buscarPorNomeIgnoraMaiusculasEAcha() {
        servico.cadastrar("Ana Souza", "A1", "", "");
        servico.cadastrar("Bruno Lima", "B1", "", "");
        assertEquals(1, servico.buscarPorNome("SOUZA").size());
        assertEquals(0, servico.buscarPorNome("zzz").size());
    }

    @Test
    public void buscarPorNomeVazioERejeitado() {
        assertThrows(RegraNegocioException.class, () -> servico.buscarPorNome(" "));
    }
}
