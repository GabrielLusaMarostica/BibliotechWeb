package bibliotech.web;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import bibliotech.config.ContextoAplicacao;
import bibliotech.servico.CodificadorSenhaPbkdf2;
import bibliotech.servico.MultaPorDiaFixo;
import bibliotech.teste.RelogioAjustavel;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/**
 * Testa a API REST de ponta a ponta (controladores + serviços + repositórios em memória) com
 * o MockMvc do Spring: sem abrir porta de rede e sem banco.
 */
public class ApiRestTest {

    private static final ObjectMapper JSON = new ObjectMapper();

    private RelogioAjustavel relogio;
    private MockMvc mvc;
    private String token;

    @Before
    public void montar() throws Exception {
        relogio = new RelogioAjustavel(LocalDate.of(2026, 10, 1));
        MultaPorDiaFixo multa = new MultaPorDiaFixo();
        ContextoAplicacao ctx = ContextoAplicacao.emMemoria(relogio, new CodificadorSenhaPbkdf2(1_000), multa);
        ctx.autenticacao().cadastrar("admin", "admin123");
        SessaoServico sessoes = new SessaoServico(relogio);
        Apresentador apresentador = new Apresentador(relogio, multa);
        mvc = MockMvcBuilders.standaloneSetup(
                        new AutenticacaoController(ctx, sessoes), new AlunoController(ctx, sessoes),
                        new LivroController(ctx, sessoes), new FuncionarioController(ctx, sessoes),
                        new EmprestimoController(ctx, sessoes, apresentador),
                        new PainelController(ctx, sessoes, apresentador))
                .setControllerAdvice(new ControladorExcecoes()).build();
        token = login("admin", "admin123").get("token").asText();
    }

    // ---------------------------------------------------------------- utilitários

    private MockHttpServletResponse executar(MockHttpServletRequestBuilder req, boolean autenticado, String corpo)
            throws Exception {
        if (autenticado) {
            req.header("Authorization", "Bearer " + token);
        }
        if (corpo != null) {
            req.contentType(MediaType.APPLICATION_JSON).content(corpo);
        }
        return mvc.perform(req).andReturn().getResponse();
    }

    private JsonNode json(MockHttpServletResponse r) throws Exception {
        return JSON.readTree(r.getContentAsString());
    }

    private JsonNode login(String login, String senha) throws Exception {
        return json(executar(post("/api/login"), false,
                "{\"login\":\"" + login + "\",\"senha\":\"" + senha + "\"}"));
    }

    /** Passa os dias no relógio e entra de novo (a sessão dura só 8 horas). */
    private void passarDias(int dias) throws Exception {
        relogio.avancarDias(dias);
        token = login("admin", "admin123").get("token").asText();
    }

    private int criarAluno(String nome, String matricula) throws Exception {
        MockHttpServletResponse r = executar(post("/api/alunos"), true,
                "{\"nome\":\"" + nome + "\",\"matricula\":\"" + matricula + "\",\"serie\":\"3A\",\"contato\":\"x\"}");
        assertEquals(201, r.getStatus());
        return json(r).get("id").asInt();
    }

    private int criarFuncionario() throws Exception {
        MockHttpServletResponse r = executar(post("/api/funcionarios"), true,
                "{\"nome\":\"Marta\",\"matricula\":\"F1\",\"funcao\":\"Bibliotecária\"}");
        assertEquals(201, r.getStatus());
        return json(r).get("id").asInt();
    }

    private int criarLivro(String titulo, int quantidade) throws Exception {
        MockHttpServletResponse r = executar(post("/api/livros"), true,
                "{\"titulo\":\"" + titulo + "\",\"autor\":\"Autor\",\"genero\":\"Romance\",\"ano\":1900,\"quantidade\":"
                        + quantidade + "}");
        assertEquals(201, r.getStatus());
        return json(r).get("id").asInt();
    }

    private MockHttpServletResponse emprestar(int aluno, int func, String prevista, String itens) throws Exception {
        return executar(post("/api/emprestimos"), true, "{\"idAluno\":" + aluno + ",\"idFuncionario\":" + func
                + ",\"dataPrevista\":\"" + prevista + "\",\"itens\":" + itens + "}");
    }

    private int estoque(int idLivro) throws Exception {
        return json(executar(get("/api/livros/" + idLivro), true, null)).get("quantidade").asInt();
    }

    // ---------------------------------------------------------------- login e sessão

    @Test
    public void loginCorretoDevolveToken() throws Exception {
        assertTrue(token.length() > 20);
    }

    @Test
    public void loginComSenhaErradaDa400ComMensagemUnica() throws Exception {
        MockHttpServletResponse r = executar(post("/api/login"), false, "{\"login\":\"admin\",\"senha\":\"errada\"}");
        MockHttpServletResponse r2 = executar(post("/api/login"), false, "{\"login\":\"fantasma\",\"senha\":\"errada\"}");
        assertEquals(400, r.getStatus());
        assertEquals(json(r).get("mensagem").asText(), json(r2).get("mensagem").asText());
    }

    @Test
    public void rotasProtegidasExigemToken() throws Exception {
        assertEquals(401, executar(get("/api/alunos"), false, null).getStatus());
        assertEquals(401, executar(get("/api/livros"), false, null).getStatus());
        assertEquals(401, executar(get("/api/painel"), false, null).getStatus());
        assertEquals(401, mvc.perform(get("/api/alunos").header("Authorization", "Bearer invalido"))
                .andReturn().getResponse().getStatus());
    }

    @Test
    public void logoutInvalidaOToken() throws Exception {
        assertEquals(204, executar(post("/api/logout"), true, null).getStatus());
        assertEquals(401, executar(get("/api/alunos"), true, null).getStatus());
    }

    @Test
    public void tokenExpiraDepoisDeOitoHoras() throws Exception {
        relogio.avancarDias(1);
        assertEquals(401, executar(get("/api/alunos"), true, null).getStatus());
    }

    // ---------------------------------------------------------------- cadastros

    @Test
    public void alunoCicloCompleto() throws Exception {
        int id = criarAluno("Ana Souza", "2024001");
        assertEquals(1, json(executar(get("/api/alunos"), true, null)).size());
        assertEquals(1, json(executar(get("/api/alunos").param("busca", "souza"), true, null)).size());
        assertEquals(0, json(executar(get("/api/alunos").param("busca", "zzz"), true, null)).size());
        MockHttpServletResponse atualizado = executar(put("/api/alunos/" + id), true,
                "{\"nome\":\"Ana S. Lima\",\"matricula\":\"2024001\",\"serie\":\"3B\",\"contato\":\"y\"}");
        assertEquals(200, atualizado.getStatus());
        assertEquals("Ana S. Lima", json(atualizado).get("nome").asText());
        assertEquals(204, executar(delete("/api/alunos/" + id), true, null).getStatus());
        assertEquals(404, executar(get("/api/alunos/" + id), true, null).getStatus());
    }

    @Test
    public void alunoInvalidoOuDuplicadoDa400() throws Exception {
        criarAluno("Ana", "2024001");
        assertEquals(400, executar(post("/api/alunos"), true, "{\"nome\":\"  \",\"matricula\":\"1\"}").getStatus());
        MockHttpServletResponse dup = executar(post("/api/alunos"), true, "{\"nome\":\"Outra\",\"matricula\":\"2024001\"}");
        assertEquals(400, dup.getStatus());
        assertTrue(json(dup).get("mensagem").asText().contains("matrícula"));
    }

    @Test
    public void textoMaiorQueOCampoDoBancoDa400ENaoErro500() throws Exception {
        // BUG-004: nome com 400 caracteres estourava VARCHAR(120) e virava HTTP 500
        String longo = "x".repeat(400);
        MockHttpServletResponse r = executar(post("/api/alunos"), true, "{\"nome\":\"" + longo + "\",\"matricula\":\"L1\"}");
        assertEquals(400, r.getStatus());
        assertTrue(json(r).get("mensagem").asText().contains("no máximo 120"));
        assertEquals(400, executar(post("/api/livros"), true, "{\"titulo\":\"" + "t".repeat(201)
                + "\",\"autor\":\"A\",\"ano\":1900,\"quantidade\":1}").getStatus());
        assertEquals(0, json(executar(get("/api/alunos"), true, null)).size());
    }

    @Test
    public void idNaoNumericoDa400EmJson() throws Exception {
        // BUG-005: /api/alunos/abc devolvia a página HTML de erro do servidor
        MockHttpServletResponse r = executar(get("/api/alunos/abc"), true, null);
        assertEquals(400, r.getStatus());
        assertTrue(r.getContentType().startsWith("application/json"));
        assertTrue(json(r).get("mensagem").asText().contains("id"));
    }

    @Test
    public void jsonInvalidoDa400() throws Exception {
        assertEquals(400, executar(post("/api/alunos"), true, "{isto não é json").getStatus());
    }

    @Test
    public void livroExigeAnoEQuantidadeValidos() throws Exception {
        criarLivro("Dom Casmurro", 3);
        assertEquals(400, executar(post("/api/livros"), true, "{\"titulo\":\"X\",\"autor\":\"Y\",\"quantidade\":1}").getStatus());
        assertEquals(400, executar(post("/api/livros"), true,
                "{\"titulo\":\"X\",\"autor\":\"Y\",\"ano\":1900,\"quantidade\":-2}").getStatus());
        assertEquals(1, json(executar(get("/api/livros").param("busca", "casmurro"), true, null)).size());
    }

    @Test
    public void funcionarioCadastraEMatriculaNaoRepete() throws Exception {
        criarFuncionario();
        assertEquals(400, executar(post("/api/funcionarios"), true,
                "{\"nome\":\"Outro\",\"matricula\":\"F1\",\"funcao\":\"x\"}").getStatus());
        assertEquals(1, json(executar(get("/api/funcionarios"), true, null)).size());
    }

    // ---------------------------------------------------------------- empréstimo e devolução

    @Test
    public void emprestimoBaixaEstoqueEDevolucaoRestaura() throws Exception {
        int aluno = criarAluno("Ana", "1"), func = criarFuncionario();
        int livro = criarLivro("Dom Casmurro", 3);
        MockHttpServletResponse r = emprestar(aluno, func, "2026-10-08", "[{\"idLivro\":" + livro + ",\"quantidade\":2}]");
        assertEquals(201, r.getStatus());
        int idEmp = json(r).get("id").asInt();
        assertEquals(1, estoque(livro));
        assertEquals(1, json(executar(get("/api/emprestimos"), true, null)).size());
        MockHttpServletResponse dev = executar(post("/api/emprestimos/" + idEmp + "/devolucao"), true, null);
        assertEquals(201, dev.getStatus());
        assertEquals(0, json(dev).get("diasAtraso").asInt());
        assertEquals(3, estoque(livro));
        assertEquals(0, json(executar(get("/api/emprestimos"), true, null)).size());
        assertEquals(1, json(executar(get("/api/emprestimos").param("situacao", "todos"), true, null)).size());
    }

    @Test
    public void devolucaoComTresDiasDeAtrasoCobraSeisReais() throws Exception {
        int aluno = criarAluno("Ana", "1"), func = criarFuncionario();
        int livro = criarLivro("Dom Casmurro", 3);
        int idEmp = json(emprestar(aluno, func, "2026-10-08", "[{\"idLivro\":" + livro + ",\"quantidade\":1}]")).get("id").asInt();
        passarDias(10);
        JsonNode pendente = json(executar(get("/api/emprestimos/" + idEmp), true, null));
        assertEquals(3, pendente.get("diasAtraso").asInt());
        assertEquals(0, pendente.get("multaPrevista").decimalValue().compareTo(new java.math.BigDecimal("6")));
        JsonNode dev = json(executar(post("/api/emprestimos/" + idEmp + "/devolucao"), true, null));
        assertEquals(3, dev.get("diasAtraso").asInt());
        assertEquals(0, dev.get("multa").decimalValue().compareTo(new java.math.BigDecimal("6")));
    }

    @Test
    public void devolverDuasVezesDa400() throws Exception {
        int aluno = criarAluno("Ana", "1"), func = criarFuncionario();
        int livro = criarLivro("Dom Casmurro", 3);
        int idEmp = json(emprestar(aluno, func, "2026-10-08", "[{\"idLivro\":" + livro + ",\"quantidade\":1}]")).get("id").asInt();
        assertEquals(201, executar(post("/api/emprestimos/" + idEmp + "/devolucao"), true, null).getStatus());
        MockHttpServletResponse segunda = executar(post("/api/emprestimos/" + idEmp + "/devolucao"), true, null);
        assertEquals(400, segunda.getStatus());
        assertEquals(3, estoque(livro));
    }

    @Test
    public void estoqueInsuficienteDa409ENadaMuda() throws Exception {
        int aluno = criarAluno("Ana", "1"), func = criarFuncionario();
        int dom = criarLivro("Dom", 3), vidas = criarLivro("Vidas", 0);
        MockHttpServletResponse r = emprestar(aluno, func, "2026-10-08",
                "[{\"idLivro\":" + dom + ",\"quantidade\":1},{\"idLivro\":" + vidas + ",\"quantidade\":1}]");
        assertEquals(409, r.getStatus());
        assertEquals(3, estoque(dom));
        assertEquals(0, json(executar(get("/api/emprestimos"), true, null)).size());
    }

    @Test
    public void emprestimoInvalidoDa400Ou404() throws Exception {
        int aluno = criarAluno("Ana", "1"), func = criarFuncionario();
        int livro = criarLivro("Dom", 3);
        String itens = "[{\"idLivro\":" + livro + ",\"quantidade\":1}]";
        assertEquals(400, emprestar(aluno, func, "2026-10-08", "[]").getStatus());
        assertEquals(400, emprestar(aluno, func, "amanhã", itens).getStatus());
        assertEquals(400, emprestar(aluno, func, "2026-09-30", itens).getStatus());
        assertEquals(400, emprestar(aluno, func, "2026-10-08",
                "[{\"idLivro\":" + livro + ",\"quantidade\":1},{\"idLivro\":" + livro + ",\"quantidade\":1}]").getStatus());
        assertEquals(404, emprestar(999, func, "2026-10-08", itens).getStatus());
        assertEquals(404, emprestar(aluno, func, "2026-10-08", "[{\"idLivro\":999,\"quantidade\":1}]").getStatus());
        assertEquals(404, executar(post("/api/emprestimos/999/devolucao"), true, null).getStatus());
        assertEquals(3, estoque(livro));
    }

    // ---------------------------------------------------------------- painel

    @Test
    public void painelResumeEstoquePendentesEAtrasados() throws Exception {
        int aluno = criarAluno("Ana", "1"), func = criarFuncionario();
        int dom = criarLivro("Dom", 4), vidas = criarLivro("Vidas", 1);
        emprestar(aluno, func, "2026-10-08", "[{\"idLivro\":" + dom + ",\"quantidade\":1}]");
        passarDias(10);
        JsonNode p = json(executar(get("/api/painel"), true, null));
        assertEquals(4, p.get("exemplaresEmEstoque").asInt()); // 3 + 1
        assertEquals(1, p.get("alunos").asInt());
        assertEquals(1, p.get("pendentes").asInt());
        assertEquals(1, p.get("atrasados").asInt());
        assertEquals(1, p.get("urgentes").size());
        assertEquals(1, p.get("estoqueBaixo").size()); // só "Vidas" (1 un.)
        assertEquals("Vidas", p.get("estoqueBaixo").get(0).get("titulo").asText());
    }
}
