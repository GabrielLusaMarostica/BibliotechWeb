package bibliotech.repositorio.jdbc;

import bibliotech.model.Usuario;
import bibliotech.repositorio.UsuarioRepositorio;
import java.util.Optional;

public class UsuarioJdbc implements UsuarioRepositorio {

    private final ExecutorSql sql;

    public UsuarioJdbc(ExecutorSql sql) {
        this.sql = sql;
    }

    @Override
    public Optional<Usuario> buscarPorLogin(String login) {
        return sql.consultarUm("SELECT login, senha FROM usuario WHERE login = ?",
                ps -> ps.setString(1, login),
                rs -> new Usuario(rs.getString("login"), rs.getString("senha")));
    }

    @Override
    public void salvar(Usuario u) {
        int linhas = sql.atualizar("UPDATE usuario SET senha = ? WHERE login = ?", ps -> {
            ps.setString(1, u.getSenhaHash());
            ps.setString(2, u.getLogin());
        });
        if (linhas == 0) {
            sql.atualizar("INSERT INTO usuario (login, senha) VALUES (?, ?)", ps -> {
                ps.setString(1, u.getLogin());
                ps.setString(2, u.getSenhaHash());
            });
        }
    }
}
