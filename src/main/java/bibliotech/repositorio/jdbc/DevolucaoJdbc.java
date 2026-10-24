package bibliotech.repositorio.jdbc;

import bibliotech.excecao.PersistenciaException;
import bibliotech.excecao.RegraNegocioException;
import bibliotech.model.Devolucao;
import bibliotech.model.Emprestimo;
import bibliotech.model.ItemEmprestimo;
import bibliotech.repositorio.DevolucaoRepositorio;
import bibliotech.repositorio.EmprestimoRepositorio;
import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.Optional;

public class DevolucaoJdbc implements DevolucaoRepositorio {

    private record Linha(int id, int idEmprestimo, LocalDate data, int diasAtraso, BigDecimal multa) { }

    private final ExecutorSql sql;
    private final EmprestimoRepositorio emprestimos;

    public DevolucaoJdbc(ExecutorSql sql, EmprestimoRepositorio emprestimos) {
        this.sql = sql;
        this.emprestimos = emprestimos;
    }

    @Override
    public Optional<Devolucao> buscarPorEmprestimo(int idEmprestimo) {
        return sql.consultarUm(
                "SELECT id, id_emprestimo, data_devolucao, dias_atraso, multa_total FROM devolucao WHERE id_emprestimo = ?",
                ps -> ps.setInt(1, idEmprestimo),
                rs -> new Linha(rs.getInt("id"), rs.getInt("id_emprestimo"),
                        rs.getDate("data_devolucao").toLocalDate(), rs.getInt("dias_atraso"),
                        rs.getBigDecimal("multa_total")))
                .map(l -> {
                    Emprestimo emprestimo = emprestimos.buscarPorId(l.idEmprestimo()).orElseThrow(
                            () -> new PersistenciaException("Empréstimo " + l.idEmprestimo() + " da devolução não existe."));
                    return new Devolucao(l.id(), emprestimo, l.data(), l.diasAtraso(), l.multa());
                });
    }

    /** Grava a devolução e devolve ao estoque os livros do empréstimo, em uma transação. */
    @Override
    public Devolucao salvar(Devolucao d) {
        if (d.getId() != 0) {
            throw new RegraNegocioException("Uma devolução já registrada não pode ser alterada.");
        }
        int id = sql.emTransacao(c -> {
            int novoId = sql.inserir(c,
                    "INSERT INTO devolucao (id_emprestimo, data_devolucao, dias_atraso, multa_total) VALUES (?, ?, ?, ?)",
                    ps -> {
                        ps.setInt(1, d.getEmprestimo().getId());
                        ps.setDate(2, Date.valueOf(d.getDataDevolucao()));
                        ps.setInt(3, d.getDiasAtraso());
                        ps.setBigDecimal(4, d.getMulta());
                    });
            for (ItemEmprestimo item : d.getEmprestimo().getItens()) {
                sql.atualizar(c, "UPDATE livro SET quantidade = quantidade + ? WHERE id = ?", ps -> {
                    ps.setInt(1, item.getQuantidade());
                    ps.setInt(2, item.getLivro().getId());
                });
            }
            return novoId;
        });
        d.setId(id);
        return d;
    }
}
