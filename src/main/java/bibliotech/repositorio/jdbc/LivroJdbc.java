package bibliotech.repositorio.jdbc;

import bibliotech.excecao.RecursoNaoEncontradoException;
import bibliotech.model.Livro;
import bibliotech.repositorio.LivroRepositorio;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class LivroJdbc implements LivroRepositorio {

    private static final String SELECT = "SELECT id, titulo, autor, genero, ano, quantidade FROM livro";

    private final ExecutorSql sql;

    public LivroJdbc(ExecutorSql sql) {
        this.sql = sql;
    }

    private static Livro mapear(ResultSet rs) throws SQLException {
        return new Livro(rs.getInt("id"), rs.getString("titulo"), rs.getString("autor"),
                rs.getString("genero"), rs.getInt("ano"), rs.getInt("quantidade"));
    }

    @Override
    public List<Livro> listar() {
        return sql.consultar(SELECT + " ORDER BY titulo", ConfiguradorStatement.SEM_PARAMETROS, LivroJdbc::mapear);
    }

    @Override
    public Optional<Livro> buscarPorId(int id) {
        return sql.consultarUm(SELECT + " WHERE id = ?", ps -> ps.setInt(1, id), LivroJdbc::mapear);
    }

    @Override
    public List<Livro> buscarPorTitulo(String trecho) {
        return sql.consultar(SELECT + " WHERE LOWER(titulo) LIKE ? ORDER BY titulo",
                ps -> ps.setString(1, "%" + trecho.toLowerCase() + "%"), LivroJdbc::mapear);
    }

    @Override
    public Livro salvar(Livro l) {
        if (l.getId() == 0) {
            l.setId(sql.inserir(
                    "INSERT INTO livro (titulo, autor, genero, ano, quantidade) VALUES (?, ?, ?, ?, ?)",
                    ps -> {
                        ps.setString(1, l.getTitulo());
                        ps.setString(2, l.getAutor());
                        ps.setString(3, l.getGenero());
                        ps.setInt(4, l.getAno());
                        ps.setInt(5, l.getQuantidade());
                    }));
        } else {
            int linhas = sql.atualizar(
                    "UPDATE livro SET titulo = ?, autor = ?, genero = ?, ano = ?, quantidade = ? WHERE id = ?",
                    ps -> {
                        ps.setString(1, l.getTitulo());
                        ps.setString(2, l.getAutor());
                        ps.setString(3, l.getGenero());
                        ps.setInt(4, l.getAno());
                        ps.setInt(5, l.getQuantidade());
                        ps.setInt(6, l.getId());
                    });
            if (linhas == 0) {
                throw new RecursoNaoEncontradoException("Livro não encontrado: id " + l.getId());
            }
        }
        return l;
    }

    @Override
    public void excluir(int id) {
        if (sql.atualizar("DELETE FROM livro WHERE id = ?", ps -> ps.setInt(1, id)) == 0) {
            throw new RecursoNaoEncontradoException("Livro não encontrado: id " + id);
        }
    }
}
