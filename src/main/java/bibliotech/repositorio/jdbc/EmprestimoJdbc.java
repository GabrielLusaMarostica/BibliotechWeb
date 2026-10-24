package bibliotech.repositorio.jdbc;

import bibliotech.excecao.EstoqueInsuficienteException;
import bibliotech.excecao.PersistenciaException;
import bibliotech.excecao.RegraNegocioException;
import bibliotech.model.Aluno;
import bibliotech.model.Emprestimo;
import bibliotech.model.Funcionario;
import bibliotech.model.ItemEmprestimo;
import bibliotech.model.Livro;
import bibliotech.repositorio.AlunoRepositorio;
import bibliotech.repositorio.EmprestimoRepositorio;
import bibliotech.repositorio.FuncionarioRepositorio;
import bibliotech.repositorio.LivroRepositorio;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Repositório do agregado Empréstimo (cabeçalho + itens). Reaproveita os
 * repositórios de aluno, funcionário e livro para montar o objeto completo.
 */
public class EmprestimoJdbc implements EmprestimoRepositorio {

    private static final String SELECT =
            "SELECT id, id_aluno, id_funcionario, data_emprestimo, data_prevista FROM emprestimo";

    private record Linha(int id, int idAluno, int idFuncionario, LocalDate emprestimo, LocalDate prevista) { }

    private record LinhaItem(int idLivro, int quantidade) { }

    private final ExecutorSql sql;
    private final AlunoRepositorio alunos;
    private final FuncionarioRepositorio funcionarios;
    private final LivroRepositorio livros;

    public EmprestimoJdbc(ExecutorSql sql, AlunoRepositorio alunos,
                          FuncionarioRepositorio funcionarios, LivroRepositorio livros) {
        this.sql = sql;
        this.alunos = alunos;
        this.funcionarios = funcionarios;
        this.livros = livros;
    }

    private static Linha mapearLinha(ResultSet rs) throws SQLException {
        return new Linha(rs.getInt("id"), rs.getInt("id_aluno"), rs.getInt("id_funcionario"),
                rs.getDate("data_emprestimo").toLocalDate(), rs.getDate("data_prevista").toLocalDate());
    }

    @Override
    public List<Emprestimo> listar() {
        return sql.consultar(SELECT + " ORDER BY id", ConfiguradorStatement.SEM_PARAMETROS,
                EmprestimoJdbc::mapearLinha).stream().map(this::montar).toList();
    }

    @Override
    public Optional<Emprestimo> buscarPorId(int id) {
        return sql.consultarUm(SELECT + " WHERE id = ?", ps -> ps.setInt(1, id),
                EmprestimoJdbc::mapearLinha).map(this::montar);
    }

    private Emprestimo montar(Linha l) {
        Aluno aluno = alunos.buscarPorId(l.idAluno()).orElseThrow(
                () -> new PersistenciaException("Aluno " + l.idAluno() + " do empréstimo " + l.id() + " não existe."));
        Funcionario func = funcionarios.buscarPorId(l.idFuncionario()).orElseThrow(
                () -> new PersistenciaException("Funcionário " + l.idFuncionario() + " do empréstimo " + l.id() + " não existe."));
        Emprestimo emprestimo = new Emprestimo(l.id(), aluno, func, l.emprestimo(), l.prevista());
        List<LinhaItem> itens = sql.consultar(
                "SELECT id_livro, quantidade FROM item_emprestimo WHERE id_emprestimo = ? ORDER BY id_livro",
                ps -> ps.setInt(1, l.id()),
                rs -> new LinhaItem(rs.getInt("id_livro"), rs.getInt("quantidade")));
        for (LinhaItem item : itens) {
            Livro livro = livros.buscarPorId(item.idLivro()).orElseThrow(
                    () -> new PersistenciaException("Livro " + item.idLivro() + " do empréstimo " + l.id() + " não existe."));
            emprestimo.adicionarItem(new ItemEmprestimo(livro, item.quantidade()));
        }
        return emprestimo;
    }

    /**
     * Grava empréstimo, itens e baixa de estoque em uma só transação. A baixa é
     * condicional ({@code quantidade >= ?}), o que evita estoque negativo mesmo
     * com dois atendimentos simultâneos.
     */
    @Override
    public Emprestimo salvar(Emprestimo e) {
        if (e.getId() != 0) {
            throw new RegraNegocioException("Um empréstimo já registrado não pode ser alterado.");
        }
        // Itens em ordem de livro: dois pedidos simultâneos travam os livros na mesma ordem.
        List<ItemEmprestimo> itens = new ArrayList<>(e.getItens());
        itens.sort(Comparator.comparingInt(i -> i.getLivro().getId()));
        int id = sql.emTransacao(c -> {
            // A baixa de estoque vem ANTES dos INSERTs: a chave estrangeira de item_emprestimo
            // pega um bloqueio compartilhado na linha do livro, e pedir depois o bloqueio
            // exclusivo do UPDATE causava deadlock entre pedidos simultâneos (BUG-006).
            for (ItemEmprestimo item : itens) {
                int baixados = sql.atualizar(c,
                        "UPDATE livro SET quantidade = quantidade - ? WHERE id = ? AND quantidade >= ?",
                        ps -> {
                            ps.setInt(1, item.getQuantidade());
                            ps.setInt(2, item.getLivro().getId());
                            ps.setInt(3, item.getQuantidade());
                        });
                if (baixados == 0) {
                    throw new EstoqueInsuficienteException("Estoque insuficiente para o livro '"
                            + item.getLivro().getTitulo() + "'.");
                }
            }
            int novoId = sql.inserir(c,
                    "INSERT INTO emprestimo (id_aluno, id_funcionario, data_emprestimo, data_prevista) VALUES (?, ?, ?, ?)",
                    ps -> {
                        ps.setInt(1, e.getAluno().getId());
                        ps.setInt(2, e.getFuncionario().getId());
                        ps.setDate(3, Date.valueOf(e.getDataEmprestimo()));
                        ps.setDate(4, Date.valueOf(e.getDataPrevista()));
                    });
            for (ItemEmprestimo item : itens) {
                sql.atualizar(c,
                        "INSERT INTO item_emprestimo (id_emprestimo, id_livro, quantidade) VALUES (?, ?, ?)",
                        ps -> {
                            ps.setInt(1, novoId);
                            ps.setInt(2, item.getLivro().getId());
                            ps.setInt(3, item.getQuantidade());
                        });
            }
            return novoId;
        });
        e.setId(id);
        return e;
    }
}
