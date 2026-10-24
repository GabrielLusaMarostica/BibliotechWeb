package bibliotech.config;

import bibliotech.repositorio.AlunoRepositorio;
import bibliotech.repositorio.DevolucaoRepositorio;
import bibliotech.repositorio.EmprestimoRepositorio;
import bibliotech.repositorio.FuncionarioRepositorio;
import bibliotech.repositorio.LivroRepositorio;
import bibliotech.repositorio.UsuarioRepositorio;
import bibliotech.repositorio.jdbc.AlunoJdbc;
import bibliotech.repositorio.jdbc.ConfiguracaoBanco;
import bibliotech.repositorio.jdbc.DevolucaoJdbc;
import bibliotech.repositorio.jdbc.EmprestimoJdbc;
import bibliotech.repositorio.jdbc.ExecutorSql;
import bibliotech.repositorio.jdbc.FabricaConexaoDriverManager;
import bibliotech.repositorio.jdbc.FuncionarioJdbc;
import bibliotech.repositorio.jdbc.LivroJdbc;
import bibliotech.repositorio.jdbc.UsuarioJdbc;
import bibliotech.repositorio.memoria.AlunoMemoria;
import bibliotech.repositorio.memoria.DevolucaoMemoria;
import bibliotech.repositorio.memoria.EmprestimoMemoria;
import bibliotech.repositorio.memoria.FuncionarioMemoria;
import bibliotech.repositorio.memoria.LivroMemoria;
import bibliotech.repositorio.memoria.UsuarioMemoria;
import bibliotech.servico.AlunoServico;
import bibliotech.servico.AutenticacaoServico;
import bibliotech.servico.CodificadorSenha;
import bibliotech.servico.CodificadorSenhaPbkdf2;
import bibliotech.servico.DevolucaoServico;
import bibliotech.servico.EmprestimoServico;
import bibliotech.servico.FuncionarioServico;
import bibliotech.servico.LivroServico;
import bibliotech.servico.MultaPorDiaFixo;
import bibliotech.servico.PoliticaMulta;
import java.time.Clock;

/**
 * Raiz de composição: o único lugar que sabe qual implementação concreta de
 * cada interface usar e como ligá-las (injeção de dependência manual).
 *
 * <p>Na versão web, um {@code ServletContextListener} cria um único contexto na
 * inicialização e os servlets/controllers apenas pedem os serviços a ele.
 * As telas e controllers nunca instanciam DAOs.</p>
 */
public final class ContextoAplicacao {

    private final AlunoServico alunos;
    private final FuncionarioServico funcionarios;
    private final LivroServico livros;
    private final EmprestimoServico emprestimos;
    private final DevolucaoServico devolucoes;
    private final AutenticacaoServico autenticacao;

    private ContextoAplicacao(AlunoRepositorio alunoRepo, FuncionarioRepositorio funcionarioRepo,
                              LivroRepositorio livroRepo, EmprestimoRepositorio emprestimoRepo,
                              DevolucaoRepositorio devolucaoRepo, UsuarioRepositorio usuarioRepo,
                              Clock relogio, CodificadorSenha codificador, PoliticaMulta politicaMulta) {
        this.alunos = new AlunoServico(alunoRepo, emprestimoRepo);
        this.funcionarios = new FuncionarioServico(funcionarioRepo);
        this.livros = new LivroServico(livroRepo, emprestimoRepo);
        this.emprestimos = new EmprestimoServico(emprestimoRepo, alunoRepo, funcionarioRepo,
                livroRepo, devolucaoRepo, relogio);
        this.devolucoes = new DevolucaoServico(emprestimoRepo, devolucaoRepo, politicaMulta, relogio);
        this.autenticacao = new AutenticacaoServico(usuarioRepo, codificador);
    }

    /** Contexto com repositórios em memória (testes e demonstração, sem banco). */
    public static ContextoAplicacao emMemoria() {
        return emMemoria(Clock.systemDefaultZone(), new CodificadorSenhaPbkdf2(), new MultaPorDiaFixo());
    }

    public static ContextoAplicacao emMemoria(Clock relogio, CodificadorSenha codificador, PoliticaMulta politica) {
        return new ContextoAplicacao(new AlunoMemoria(), new FuncionarioMemoria(), new LivroMemoria(),
                new EmprestimoMemoria(), new DevolucaoMemoria(), new UsuarioMemoria(),
                relogio, codificador, politica);
    }

    /** Contexto com persistência JDBC (MySQL). */
    public static ContextoAplicacao comJdbc(ConfiguracaoBanco configuracao) {
        return comJdbc(configuracao, Clock.systemDefaultZone(), new CodificadorSenhaPbkdf2(), new MultaPorDiaFixo());
    }

    public static ContextoAplicacao comJdbc(ConfiguracaoBanco configuracao, Clock relogio,
                                            CodificadorSenha codificador, PoliticaMulta politica) {
        ExecutorSql sql = new ExecutorSql(new FabricaConexaoDriverManager(configuracao));
        AlunoRepositorio alunoRepo = new AlunoJdbc(sql);
        FuncionarioRepositorio funcionarioRepo = new FuncionarioJdbc(sql);
        LivroRepositorio livroRepo = new LivroJdbc(sql);
        EmprestimoRepositorio emprestimoRepo = new EmprestimoJdbc(sql, alunoRepo, funcionarioRepo, livroRepo);
        DevolucaoRepositorio devolucaoRepo = new DevolucaoJdbc(sql, emprestimoRepo);
        return new ContextoAplicacao(alunoRepo, funcionarioRepo, livroRepo, emprestimoRepo, devolucaoRepo,
                new UsuarioJdbc(sql), relogio, codificador, politica);
    }

    public AlunoServico alunos() {
        return alunos;
    }

    public FuncionarioServico funcionarios() {
        return funcionarios;
    }

    public LivroServico livros() {
        return livros;
    }

    public EmprestimoServico emprestimos() {
        return emprestimos;
    }

    public DevolucaoServico devolucoes() {
        return devolucoes;
    }

    public AutenticacaoServico autenticacao() {
        return autenticacao;
    }
}
