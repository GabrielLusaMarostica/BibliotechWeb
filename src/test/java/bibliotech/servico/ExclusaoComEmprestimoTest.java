package bibliotech.servico;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import bibliotech.config.ContextoAplicacao;
import bibliotech.excecao.RegraNegocioException;
import bibliotech.model.Aluno;
import bibliotech.model.Funcionario;
import bibliotech.model.Livro;
import bibliotech.teste.RelogioAjustavel;
import java.time.LocalDate;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;

/** Regressão do BUG-001: excluir aluno/livro com empréstimo deixava registros órfãos (modo memória). */
public class ExclusaoComEmprestimoTest {

    private ContextoAplicacao ctx;
    private Aluno aluno;
    private Livro livro;

    @Before
    public void preparar() {
        ctx = ContextoAplicacao.emMemoria(new RelogioAjustavel(LocalDate.of(2026, 10, 1)),
                new CodificadorSenhaPbkdf2(1_000), new MultaPorDiaFixo());
        aluno = ctx.alunos().cadastrar("Ana", "A1", "1º ano", "");
        Funcionario f = ctx.funcionarios().cadastrar("Carlos", "F1", "Bibliotecário");
        livro = ctx.livros().cadastrar("Dom Casmurro", "Machado", "Romance", 1899, 3);
        ctx.emprestimos().registrar(aluno.getId(), f.getId(), LocalDate.of(2026, 10, 8), Map.of(livro.getId(), 1));
    }

    @Test
    public void alunoComEmprestimoNaoPodeSerExcluido() {
        RegraNegocioException e = assertThrows(RegraNegocioException.class, () -> ctx.alunos().excluir(aluno.getId()));
        assertTrue(e.getMessage().contains("empréstimos"));
        assertEquals("Ana", ctx.alunos().buscarPorId(aluno.getId()).getNome());
    }

    @Test
    public void livroEmprestadoNaoPodeSerExcluido() {
        RegraNegocioException e = assertThrows(RegraNegocioException.class, () -> ctx.livros().excluir(livro.getId()));
        assertTrue(e.getMessage().contains("empréstimos"));
        assertEquals(2, ctx.livros().buscarPorId(livro.getId()).getQuantidade());
    }

    @Test
    public void alunoELivroSemEmprestimoContinuamPodendoSerExcluidos() {
        Aluno outro = ctx.alunos().cadastrar("Bruno", "B1", "2º ano", "");
        Livro outroLivro = ctx.livros().cadastrar("Vidas Secas", "Graciliano", "Romance", 1938, 1);
        ctx.alunos().excluir(outro.getId());
        ctx.livros().excluir(outroLivro.getId());
        assertEquals(1, ctx.alunos().listar().size());
        assertEquals(1, ctx.livros().listar().size());
    }
}
