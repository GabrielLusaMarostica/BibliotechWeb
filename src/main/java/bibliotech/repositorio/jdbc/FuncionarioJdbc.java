package bibliotech.repositorio.jdbc;

import bibliotech.excecao.RecursoNaoEncontradoException;
import bibliotech.model.Funcionario;
import bibliotech.repositorio.FuncionarioRepositorio;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class FuncionarioJdbc implements FuncionarioRepositorio {

    private static final String SELECT = "SELECT id, nome, matricula, funcao FROM funcionario";

    private final ExecutorSql sql;

    public FuncionarioJdbc(ExecutorSql sql) {
        this.sql = sql;
    }

    private static Funcionario mapear(ResultSet rs) throws SQLException {
        return new Funcionario(rs.getInt("id"), rs.getString("nome"),
                rs.getString("matricula"), rs.getString("funcao"));
    }

    @Override
    public List<Funcionario> listar() {
        return sql.consultar(SELECT + " ORDER BY nome", ConfiguradorStatement.SEM_PARAMETROS, FuncionarioJdbc::mapear);
    }

    @Override
    public Optional<Funcionario> buscarPorId(int id) {
        return sql.consultarUm(SELECT + " WHERE id = ?", ps -> ps.setInt(1, id), FuncionarioJdbc::mapear);
    }

    @Override
    public Optional<Funcionario> buscarPorMatricula(String matricula) {
        return sql.consultarUm(SELECT + " WHERE matricula = ?", ps -> ps.setString(1, matricula), FuncionarioJdbc::mapear);
    }

    @Override
    public Funcionario salvar(Funcionario f) {
        if (f.getId() == 0) {
            f.setId(sql.inserir("INSERT INTO funcionario (nome, matricula, funcao) VALUES (?, ?, ?)",
                    ps -> {
                        ps.setString(1, f.getNome());
                        ps.setString(2, f.getMatricula());
                        ps.setString(3, f.getFuncao());
                    }));
        } else {
            int linhas = sql.atualizar("UPDATE funcionario SET nome = ?, matricula = ?, funcao = ? WHERE id = ?",
                    ps -> {
                        ps.setString(1, f.getNome());
                        ps.setString(2, f.getMatricula());
                        ps.setString(3, f.getFuncao());
                        ps.setInt(4, f.getId());
                    });
            if (linhas == 0) {
                throw new RecursoNaoEncontradoException("Funcionário não encontrado: id " + f.getId());
            }
        }
        return f;
    }
}
