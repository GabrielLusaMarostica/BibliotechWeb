package bibliotech.servico;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import bibliotech.config.ContextoAplicacao;
import bibliotech.excecao.EstoqueInsuficienteException;
import bibliotech.excecao.RecursoNaoEncontradoException;
import bibliotech.excecao.RegraNegocioException;
import bibliotech.model.Aluno;
import bibliotech.model.Emprestimo;
import bibliotech.model.Funcionario;
import bibliotech.model.Livro;
import bibliotech.teste.RelogioAjustavel;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;

/** Caso de uso "registrar empréstimo", com repositórios em memória (sem banco). */
public class EmprestimoServicoTest {

    private RelogioAjustavel relogio;
    private ContextoAplicacao ctx;
    private LocalDate hoje;
    private Aluno ana;
    private Funcionario carlos;
    private Livro dom;
    private Livro vidas;

    @Before
    public void preparar() {
        relogio = new RelogioAjustavel(LocalDate.of(2026, 10, 1));
        ctx = ContextoAplicacao.emMemoria(relogio, new CodificadorSenhaPbkdf2(1_000), new MultaPorDiaFixo());
        hoje = LocalDate.now(relogio);
        ana = ctx.alunos().cadastrar("Ana Souza", "A1", "2º ano", "");
        carlos = ctx.funcionarios().cadastrar("Carlos Lima", "F1", "Bibliotecário");
        dom = ctx.livros().cadastrar("Dom Casmurro", "Machado de Assis", "Romance", 1899, 3);
        vidas = ctx.livros().cadastrar("Vidas Secas", "Graciliano Ramos", "Romance", 1938, 1);
    }

    private int estoque(Livro livro) {
        return ctx.livros().buscarPorId(livro.getId()).getQuantidade();
    }

    @Test
    public void registrarEmprestimoGravaEBaixaOEstoque() {
        Map<Integer, Integer> pedido = new LinkedHashMap<>();
        pedido.put(dom.getId(), 2);
        pedido.put(vidas.getId(), 1);

        Emprestimo e = ctx.emprestimos().registrar(ana.getId(), carlos.getId(), hoje.plusDays(7), pedido);

        assertTrue(e.getId() > 0);
        assertEquals(2, ctx.emprestimos().buscarPorId(e.getId()).getItens().size());
        assertEquals(1, estoque(dom));
        assertEquals(0, estoque(vidas));
    }

    @Test
    public void dataDoEmprestimoVemDoRelogioInjetado() {
        Emprestimo e = ctx.emprestimos().registrar(ana.getId(), carlos.getId(), hoje.plusDays(7),
                Map.of(dom.getId(), 1));
        assertEquals(hoje, e.getDataEmprestimo());
    }

    @Test
    public void pedidoAcimaDoEstoqueELancaExcecaoENaoAlteraNada() {
        assertThrows(EstoqueInsuficienteException.class, () ->
                ctx.emprestimos().registrar(ana.getId(), carlos.getId(), hoje.plusDays(7), Map.of(dom.getId(), 5)));
        assertEquals(3, estoque(dom));
        assertTrue(ctx.emprestimos().listar().isEmpty());
    }

    @Test
    public void pedidoComUmItemSemEstoqueEhRecusadoPorInteiro() {
        ctx.emprestimos().registrar(ana.getId(), carlos.getId(), hoje.plusDays(7), Map.of(vidas.getId(), 1));
        Map<Integer, Integer> misto = new LinkedHashMap<>();
        misto.put(dom.getId(), 1);    // há estoque
        misto.put(vidas.getId(), 1);  // acabou

        assertThrows(EstoqueInsuficienteException.class, () ->
                ctx.emprestimos().registrar(ana.getId(), carlos.getId(), hoje.plusDays(7), misto));

        assertEquals("o livro com estoque não pode ter sido baixado", 3, estoque(dom));
    }

    @Test
    public void dataPrevistaAnteriorAHojeERejeitada() {
        assertThrows(RegraNegocioException.class, () ->
                ctx.emprestimos().registrar(ana.getId(), carlos.getId(), hoje.minusDays(1), Map.of(dom.getId(), 1)));
        assertEquals(3, estoque(dom));
    }

    @Test
    public void emprestimoSemLivrosERejeitado() {
        assertThrows(RegraNegocioException.class, () ->
                ctx.emprestimos().registrar(ana.getId(), carlos.getId(), hoje.plusDays(7), Map.of()));
        assertThrows(RegraNegocioException.class, () ->
                ctx.emprestimos().registrar(ana.getId(), carlos.getId(), hoje.plusDays(7), null));
    }

    @Test
    public void alunoFuncionarioOuLivroInexistentesSaoRejeitados() {
        assertThrows(RecursoNaoEncontradoException.class, () ->
                ctx.emprestimos().registrar(999, carlos.getId(), hoje.plusDays(7), Map.of(dom.getId(), 1)));
        assertThrows(RecursoNaoEncontradoException.class, () ->
                ctx.emprestimos().registrar(ana.getId(), 999, hoje.plusDays(7), Map.of(dom.getId(), 1)));
        assertThrows(RecursoNaoEncontradoException.class, () ->
                ctx.emprestimos().registrar(ana.getId(), carlos.getId(), hoje.plusDays(7), Map.of(999, 1)));
    }

    @Test
    public void quantidadeZeroNoPedidoERejeitada() {
        assertThrows(RegraNegocioException.class, () ->
                ctx.emprestimos().registrar(ana.getId(), carlos.getId(), hoje.plusDays(7), Map.of(dom.getId(), 0)));
        assertEquals(3, estoque(dom));
    }

    @Test
    public void emprestimoRecemFeitoApareceComoPendente() {
        Emprestimo e = ctx.emprestimos().registrar(ana.getId(), carlos.getId(), hoje.plusDays(7),
                Map.of(dom.getId(), 1));
        assertTrue(ctx.emprestimos().listarPendentes().stream().anyMatch(p -> p.getId() == e.getId()));
    }

    @Test
    public void emprestimoDevolvidoSaiDaListaDePendentes() {
        Emprestimo e = ctx.emprestimos().registrar(ana.getId(), carlos.getId(), hoje.plusDays(7),
                Map.of(dom.getId(), 1));
        ctx.devolucoes().registrar(e.getId());
        assertFalse(ctx.emprestimos().listarPendentes().stream().anyMatch(p -> p.getId() == e.getId()));
        assertEquals(1, ctx.emprestimos().listar().size());
    }

    @Test
    public void buscarEmprestimoInexistenteLancaExcecao() {
        assertThrows(RecursoNaoEncontradoException.class, () -> ctx.emprestimos().buscarPorId(42));
    }

    @Test
    public void semAtrasoOValorDaMultaPadraoEZero() {
        assertEquals(0, new MultaPorDiaFixo().calcular(0).compareTo(BigDecimal.ZERO));
    }
}
