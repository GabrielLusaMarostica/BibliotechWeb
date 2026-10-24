package bibliotech.repositorio.jdbc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import bibliotech.config.ContextoAplicacao;
import bibliotech.excecao.EstoqueInsuficienteException;
import bibliotech.excecao.RecursoNaoEncontradoException;
import bibliotech.excecao.RegraNegocioException;
import bibliotech.model.Aluno;
import bibliotech.model.Devolucao;
import bibliotech.model.Emprestimo;
import bibliotech.model.Funcionario;
import bibliotech.model.Livro;
import bibliotech.servico.CodificadorSenhaPbkdf2;
import bibliotech.servico.MultaPorDiaFixo;
import bibliotech.teste.RelogioAjustavel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Before;
import org.junit.Test;

/**
 * Testa as classes JDBC de verdade (SQL, transações, chaves estrangeiras) usando o H2 em modo
 * MySQL, com o mesmo {@code banco/schema.sql} do MySQL. Na etapa 7 esses testes foram dispensados;
 * agora que há banco na aplicação, eles protegem o acesso a dados.
 */
public class RepositoriosJdbcTest {

    private static final AtomicInteger BANCOS = new AtomicInteger();

    private RelogioAjustavel relogio;
    private ContextoAplicacao ctx;

    @Before
    public void prepararBancoLimpo() throws Exception {
        String url = "jdbc:h2:mem:bibliotech" + BANCOS.incrementAndGet() + ";MODE=MySQL;DB_CLOSE_DELAY=-1";
        criarTabelas(url);
        relogio = new RelogioAjustavel(LocalDate.of(2026, 10, 1));
        ctx = ContextoAplicacao.comJdbc(new ConfiguracaoBanco(url, "sa", ""), relogio,
                new CodificadorSenhaPbkdf2(1_000), new MultaPorDiaFixo());
    }

    private static void criarTabelas(String url) throws Exception {
        String script = Files.readString(Path.of("banco", "schema.sql"), StandardCharsets.UTF_8);
        try (Connection c = DriverManager.getConnection(url, "sa", ""); Statement st = c.createStatement()) {
            for (String comando : script.split(";")) {
                String limpo = comando.replaceAll("(?m)^\\s*--.*$", "").trim();
                if (limpo.isEmpty() || limpo.startsWith("CREATE DATABASE") || limpo.startsWith("USE ")) {
                    continue;
                }
                st.execute(limpo);
            }
        }
    }

    private Aluno aluno() {
        return ctx.alunos().cadastrar("Ana Souza", "2024001", "2º ano", "ana@escola.com");
    }

    private Funcionario funcionario() {
        return ctx.funcionarios().cadastrar("Carlos Lima", "F001", "Bibliotecário");
    }

    @Test
    public void alunoEGravadoLidoAtualizadoEExcluido() {
        Aluno a = aluno();
        assertTrue(a.getId() > 0);
        assertEquals("Ana Souza", ctx.alunos().buscarPorId(a.getId()).getNome());
        assertEquals(1, ctx.alunos().buscarPorNome("souza").size());
        ctx.alunos().atualizar(a.getId(), "Ana S. Lima", "2024001", "3º ano", "");
        assertEquals("Ana S. Lima", ctx.alunos().buscarPorId(a.getId()).getNome());
        ctx.alunos().excluir(a.getId());
        assertThrows(RecursoNaoEncontradoException.class, () -> ctx.alunos().buscarPorId(a.getId()));
    }

    @Test
    public void matriculaDuplicadaERecusada() {
        aluno();
        assertThrows(RegraNegocioException.class,
                () -> ctx.alunos().cadastrar("Outra", "2024001", "1º ano", ""));
    }

    @Test
    public void livroEFuncionarioSaoGravados() {
        Livro l = ctx.livros().cadastrar("Dom Casmurro", "Machado de Assis", "Romance", 1899, 3);
        assertEquals(3, ctx.livros().buscarPorId(l.getId()).getQuantidade());
        assertEquals(1, ctx.livros().buscarPorTitulo("casmurro").size());
        Funcionario f = funcionario();
        assertTrue(ctx.funcionarios().listar().stream().anyMatch(x -> x.getId() == f.getId()));
    }

    @Test
    public void emprestimoGravaItensEBaixaOEstoqueNoBanco() {
        Aluno a = aluno();
        Funcionario f = funcionario();
        Livro dom = ctx.livros().cadastrar("Dom Casmurro", "Machado de Assis", "Romance", 1899, 3);
        Livro vidas = ctx.livros().cadastrar("Vidas Secas", "Graciliano Ramos", "Romance", 1938, 1);
        Map<Integer, Integer> pedido = new LinkedHashMap<>();
        pedido.put(dom.getId(), 2);
        pedido.put(vidas.getId(), 1);
        Emprestimo e = ctx.emprestimos().registrar(a.getId(), f.getId(), LocalDate.of(2026, 10, 8), pedido);
        assertEquals(2, ctx.emprestimos().buscarPorId(e.getId()).getItens().size());
        assertEquals(1, ctx.livros().buscarPorId(dom.getId()).getQuantidade());
        assertEquals(0, ctx.livros().buscarPorId(vidas.getId()).getQuantidade());
        assertEquals(1, ctx.emprestimos().listarPendentes().size());
    }

    @Test
    public void pedidosSimultaneosPeloUltimoExemplarDeixamUmSoVencedor() throws Exception {
        // BUG-006: no MySQL, pedidos simultâneos davam deadlock e a API respondia 500
        Aluno a = aluno();
        Funcionario f = funcionario();
        Livro ultimo = ctx.livros().cadastrar("Ultimo Exemplar", "Autor", "Romance", 2000, 1);
        int tentativas = 8;
        java.util.concurrent.ExecutorService pool = java.util.concurrent.Executors.newFixedThreadPool(tentativas);
        java.util.concurrent.CountDownLatch largada = new java.util.concurrent.CountDownLatch(1);
        java.util.List<java.util.concurrent.Future<String>> resultados = new java.util.ArrayList<>();
        for (int i = 0; i < tentativas; i++) {
            resultados.add(pool.submit(() -> {
                largada.await();
                try {
                    ctx.emprestimos().registrar(a.getId(), f.getId(), LocalDate.of(2026, 10, 8),
                            java.util.Map.of(ultimo.getId(), 1));
                    return "ok";
                } catch (EstoqueInsuficienteException e) {
                    return "sem-estoque";
                }
            }));
        }
        largada.countDown();
        int vencedores = 0;
        for (java.util.concurrent.Future<String> r : resultados) {
            String resultado = r.get(30, java.util.concurrent.TimeUnit.SECONDS);
            if (resultado.equals("ok")) {
                vencedores++;
            }
        }
        pool.shutdown();
        assertEquals(1, vencedores);
        assertEquals(0, ctx.livros().buscarPorId(ultimo.getId()).getQuantidade());
        assertEquals(1, ctx.emprestimos().listarPendentes().size());
    }

    @Test
    public void pedidoMistoSemEstoqueNaoAlteraNada() {
        Aluno a = aluno();
        Funcionario f = funcionario();
        Livro dom = ctx.livros().cadastrar("Dom Casmurro", "Machado de Assis", "Romance", 1899, 3);
        Livro vidas = ctx.livros().cadastrar("Vidas Secas", "Graciliano Ramos", "Romance", 1938, 0);
        Map<Integer, Integer> pedido = new LinkedHashMap<>();
        pedido.put(dom.getId(), 1);
        pedido.put(vidas.getId(), 1);
        assertThrows(EstoqueInsuficienteException.class,
                () -> ctx.emprestimos().registrar(a.getId(), f.getId(), LocalDate.of(2026, 10, 8), pedido));
        assertEquals(3, ctx.livros().buscarPorId(dom.getId()).getQuantidade());
        assertTrue(ctx.emprestimos().listar().isEmpty());
    }

    @Test
    public void devolucaoComAtrasoGravaMultaEDevolveOEstoque() {
        Aluno a = aluno();
        Funcionario f = funcionario();
        Livro dom = ctx.livros().cadastrar("Dom Casmurro", "Machado de Assis", "Romance", 1899, 3);
        Emprestimo e = ctx.emprestimos().registrar(a.getId(), f.getId(), LocalDate.of(2026, 10, 8),
                Map.of(dom.getId(), 2));
        relogio.avancarDias(10); // 11/10: 3 dias depois do prazo
        Devolucao d = ctx.devolucoes().registrar(e.getId());
        assertEquals(3, d.getDiasAtraso());
        assertEquals(0, d.getMulta().compareTo(new java.math.BigDecimal("6.00")));
        assertEquals(3, ctx.livros().buscarPorId(dom.getId()).getQuantidade());
        assertTrue(ctx.emprestimos().listarPendentes().isEmpty());
        assertThrows(RegraNegocioException.class, () -> ctx.devolucoes().registrar(e.getId()));
    }

    @Test
    public void alunoComEmprestimoNaoPodeSerExcluidoEMantemOsDados() {
        Aluno a = aluno();
        Funcionario f = funcionario();
        Livro dom = ctx.livros().cadastrar("Dom Casmurro", "Machado de Assis", "Romance", 1899, 3);
        ctx.emprestimos().registrar(a.getId(), f.getId(), LocalDate.of(2026, 10, 8), Map.of(dom.getId(), 1));
        RegraNegocioException erro = assertThrows(RegraNegocioException.class, () -> ctx.alunos().excluir(a.getId()));
        assertTrue(erro.getMessage().toLowerCase().contains("empréstimo"));
        assertEquals("Ana Souza", ctx.alunos().buscarPorId(a.getId()).getNome());
    }

    @Test
    public void livroEmprestadoNaoPodeSerExcluido() {
        Aluno a = aluno();
        Funcionario f = funcionario();
        Livro dom = ctx.livros().cadastrar("Dom Casmurro", "Machado de Assis", "Romance", 1899, 3);
        ctx.emprestimos().registrar(a.getId(), f.getId(), LocalDate.of(2026, 10, 8), Map.of(dom.getId(), 1));
        RegraNegocioException erro = assertThrows(RegraNegocioException.class, () -> ctx.livros().excluir(dom.getId()));
        assertTrue(erro.getMessage().toLowerCase().contains("empréstimo"));
    }

    @Test
    public void senhaECadastradaComHashELoginFunciona() {
        ctx.autenticacao().cadastrar("gabriel", "senha-segura-1");
        assertEquals("gabriel", ctx.autenticacao().autenticar("gabriel", "senha-segura-1").getLogin());
        assertThrows(RegraNegocioException.class, () -> ctx.autenticacao().autenticar("gabriel", "errada"));
    }
}
