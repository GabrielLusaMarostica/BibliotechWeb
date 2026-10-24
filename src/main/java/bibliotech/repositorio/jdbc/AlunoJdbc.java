package bibliotech.repositorio.jdbc;

import bibliotech.excecao.RecursoNaoEncontradoException;
import bibliotech.model.Aluno;
import bibliotech.repositorio.AlunoRepositorio;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class AlunoJdbc implements AlunoRepositorio {

    private static final String SELECT = "SELECT id, nome, matricula, serie, contato FROM aluno";

    private final ExecutorSql sql;

    public AlunoJdbc(ExecutorSql sql) {
        this.sql = sql;
    }

    private static Aluno mapear(ResultSet rs) throws SQLException {
        return new Aluno(rs.getInt("id"), rs.getString("nome"), rs.getString("matricula"),
                rs.getString("serie"), rs.getString("contato"));
    }

    @Override
    public List<Aluno> listar() {
        return sql.consultar(SELECT + " ORDER BY nome", ConfiguradorStatement.SEM_PARAMETROS, AlunoJdbc::mapear);
    }

    @Override
    public Optional<Aluno> buscarPorId(int id) {
        return sql.consultarUm(SELECT + " WHERE id = ?", ps -> ps.setInt(1, id), AlunoJdbc::mapear);
    }

    @Override
    public Optional<Aluno> buscarPorMatricula(String matricula) {
        return sql.consultarUm(SELECT + " WHERE matricula = ?", ps -> ps.setString(1, matricula), AlunoJdbc::mapear);
    }

    @Override
    public List<Aluno> buscarPorNome(String trecho) {
        return sql.consultar(SELECT + " WHERE LOWER(nome) LIKE ? ORDER BY nome",
                ps -> ps.setString(1, "%" + trecho.toLowerCase() + "%"), AlunoJdbc::mapear);
    }

    @Override
    public Aluno salvar(Aluno a) {
        if (a.getId() == 0) {
            a.setId(sql.inserir(
                    "INSERT INTO aluno (nome, matricula, serie, contato) VALUES (?, ?, ?, ?)",
                    ps -> {
                        ps.setString(1, a.getNome());
                        ps.setString(2, a.getMatricula());
                        ps.setString(3, a.getSerie());
                        ps.setString(4, a.getContato());
                    }));
        } else {
            int linhas = sql.atualizar(
                    "UPDATE aluno SET nome = ?, matricula = ?, serie = ?, contato = ? WHERE id = ?",
                    ps -> {
                        ps.setString(1, a.getNome());
                        ps.setString(2, a.getMatricula());
                        ps.setString(3, a.getSerie());
                        ps.setString(4, a.getContato());
                        ps.setInt(5, a.getId());
                    });
            if (linhas == 0) {
                throw new RecursoNaoEncontradoException("Aluno não encontrado: id " + a.getId());
            }
        }
        return a;
    }

    @Override
    public void excluir(int id) {
        if (sql.atualizar("DELETE FROM aluno WHERE id = ?", ps -> ps.setInt(1, id)) == 0) {
            throw new RecursoNaoEncontradoException("Aluno não encontrado: id " + id);
        }
    }
}
