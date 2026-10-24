package bibliotech.servico;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import bibliotech.config.ContextoAplicacao;
import bibliotech.excecao.RecursoNaoEncontradoException;
import bibliotech.excecao.RegraNegocioException;
import bibliotech.model.Aluno;
import bibliotech.model.Devolucao;
import bibliotech.model.Emprestimo;
import bibliotech.model.Funcionario;
import bibliotech.model.Livro;
import bibliotech.teste.RelogioAjustavel;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;

/** Caso de uso "registrar devolução": atraso, multa e retorno ao estoque. */
public class DevolucaoServicoTest {

    private RelogioAjustavel relogio;
    private ContextoAplicacao ctx;
    private Aluno ana;
    private Funcionario carlos;
    private Livro dom;

    @Before
    public void preparar() {
        relogio = new RelogioAjustavel(LocalDate.of(2026, 10, 1));
        ctx = ContextoAplicacao.emMemoria(relogio, new CodificadorSenhaPbkdf2(1_000), new MultaPorDiaFixo());
        ana = ctx.alunos().cadastrar("Ana Souza", "A1", "2º ano", "");
        carlos = ctx.funcionarios().cadastrar("Carlos Lima", "F1", "Bibliotecário");
        dom = ctx.livros().cadastrar("Dom Casmurro", "Machado de Assis", "Romance", 1899, 3);
    }

    private Emprestimo emprestarPorSeteDias(int exemplares) {
        return ctx.emprestimos().registrar(ana.getId(), carlos.getId(),
                LocalDate.now(relogio).plusDays(7), Map.of(dom.getId(), exemplares));
    }

    private int estoque() {
        return ctx.livros().buscarPorId(dom.getId()).getQuantidade();
    }

    @Test
    public void devolucaoNoPrazoNaoTemAtrasoNemMulta() {
        Emprestimo e = emprestarPorSeteDias(1);
        relogio.avancarDias(7); // exatamente a data prevista

        Devolucao d = ctx.devolucoes().registrar(e.getId());

        assertEquals(0, d.getDiasAtraso());
        assertEquals(0, d.getMulta().signum());
    }

    @Test
    public void devolucaoAntesDoPrazoNaoTemMulta() {
        Emprestimo e = emprestarPorSeteDias(1);
        relogio.avancarDias(2);
        assertEquals(0, ctx.devolucoes().registrar(e.getId()).getDiasAtraso());
    }

    @Test
    public void tresDiasDeAtrasoGeramMultaDeSeisReais() {
        Emprestimo e = emprestarPorSeteDias(1);
        relogio.avancarDias(10);

        Devolucao d = ctx.devolucoes().registrar(e.getId());

        assertEquals(3, d.getDiasAtraso());
        assertEquals(new BigDecimal("6.00"), d.getMulta());
        assertEquals(LocalDate.of(2026, 10, 11), d.getDataDevolucao());
    }

    @Test
    public void devolverRestauraOEstoqueNaQuantidadeEmprestada() {
        Emprestimo e = emprestarPorSeteDias(2);
        assertEquals(1, estoque());
        ctx.devolucoes().registrar(e.getId());
        assertEquals(3, estoque());
    }

    @Test
    public void devolverDuasVezesOMesmoEmprestimoERejeitado() {
        Emprestimo e = emprestarPorSeteDias(1);
        ctx.devolucoes().registrar(e.getId());
        assertThrows(RegraNegocioException.class, () -> ctx.devolucoes().registrar(e.getId()));
        assertEquals("a segunda tentativa não pode devolver ao estoque de novo", 3, estoque());
    }

    @Test
    public void devolverEmprestimoInexistenteLancaExcecao() {
        assertThrows(RecursoNaoEncontradoException.class, () -> ctx.devolucoes().registrar(999));
    }

    @Test
    public void politicaDeMultaPersonalizadaEhRespeitada() {
        ContextoAplicacao outro = ContextoAplicacao.emMemoria(relogio, new CodificadorSenhaPbkdf2(1_000),
                new MultaPorDiaFixo(new BigDecimal("0.50")));
        Aluno a = outro.alunos().cadastrar("Bia", "B1", "", "");
        Funcionario f = outro.funcionarios().cadastrar("Duda", "F2", "");
        Livro l = outro.livros().cadastrar("Livro", "Autor", "G", 2000, 1);
        Emprestimo e = outro.emprestimos().registrar(a.getId(), f.getId(),
                LocalDate.now(relogio).plusDays(1), Map.of(l.getId(), 1));
        relogio.avancarDias(5); // 4 dias de atraso

        assertEquals(new BigDecimal("2.00"), outro.devolucoes().registrar(e.getId()).getMulta());
    }
}
