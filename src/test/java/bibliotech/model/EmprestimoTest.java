package bibliotech.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import bibliotech.excecao.RegraNegocioException;
import java.time.LocalDate;
import org.junit.Before;
import org.junit.Test;

public class EmprestimoTest {

    private static final LocalDate HOJE = LocalDate.of(2026, 10, 1);

    private Aluno aluno;
    private Funcionario funcionario;
    private Livro livro;

    @Before
    public void criarObjetos() {
        aluno = new Aluno("Ana Souza", "A1", "2º ano", "");
        funcionario = new Funcionario("Carlos Lima", "F1", "Bibliotecário");
        livro = new Livro("Dom Casmurro", "Machado de Assis", "Romance", 1899, 3);
    }

    @Test
    public void emprestimoValidoGuardaSeusDados() {
        Emprestimo e = new Emprestimo(0, aluno, funcionario, HOJE, HOJE.plusDays(7));
        assertEquals(aluno, e.getAluno());
        assertEquals(HOJE.plusDays(7), e.getDataPrevista());
        assertTrue(e.getItens().isEmpty());
    }

    @Test
    public void dataPrevistaAnteriorAoEmprestimoERejeitada() {
        assertThrows(RegraNegocioException.class,
                () -> new Emprestimo(0, aluno, funcionario, HOJE, HOJE.minusDays(1)));
    }

    @Test
    public void dataPrevistaNoMesmoDiaEPermitida() {
        new Emprestimo(0, aluno, funcionario, HOJE, HOJE);
    }

    @Test
    public void emprestimoExigeAlunoEFuncionario() {
        assertThrows(RegraNegocioException.class,
                () -> new Emprestimo(0, null, funcionario, HOJE, HOJE.plusDays(1)));
        assertThrows(RegraNegocioException.class,
                () -> new Emprestimo(0, aluno, null, HOJE, HOJE.plusDays(1)));
    }

    @Test
    public void adicionarItemNaoMexeNoEstoque() {
        Emprestimo e = new Emprestimo(0, aluno, funcionario, HOJE, HOJE.plusDays(7));
        e.adicionarItem(new ItemEmprestimo(livro, 2));
        assertEquals(1, e.getItens().size());
        assertEquals(3, livro.getQuantidade()); // a baixa é feita pelo serviço, não aqui
    }

    @Test
    public void omesmoLivroNaoPodeSerAdicionadoDuasVezes() {
        Emprestimo e = new Emprestimo(0, aluno, funcionario, HOJE, HOJE.plusDays(7));
        e.adicionarItem(new ItemEmprestimo(livro, 1));
        assertThrows(RegraNegocioException.class, () -> e.adicionarItem(new ItemEmprestimo(livro, 1)));
    }

    @Test
    public void listaDeItensNaoPodeSerAlteradaPorFora() {
        Emprestimo e = new Emprestimo(0, aluno, funcionario, HOJE, HOJE.plusDays(7));
        assertThrows(UnsupportedOperationException.class,
                () -> e.getItens().add(new ItemEmprestimo(livro, 1)));
    }

    @Test
    public void resumoListaAlunoFuncionarioELivros() {
        Emprestimo e = new Emprestimo(0, aluno, funcionario, HOJE, HOJE.plusDays(7));
        e.adicionarItem(new ItemEmprestimo(livro, 2));
        String resumo = e.resumo();
        assertTrue(resumo.contains("Ana Souza"));
        assertTrue(resumo.contains("Carlos Lima"));
        assertTrue(resumo.contains("Dom Casmurro (x2)"));
    }

    @Test
    public void itemExigeQuantidadePositiva() {
        assertThrows(RegraNegocioException.class, () -> new ItemEmprestimo(livro, 0));
    }
}
