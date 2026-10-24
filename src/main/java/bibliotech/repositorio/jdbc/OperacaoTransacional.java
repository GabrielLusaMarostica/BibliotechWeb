package bibliotech.repositorio.jdbc;

import java.sql.Connection;
import java.sql.SQLException;

/** Bloco de código executado dentro de uma única transação. */
@FunctionalInterface
public interface OperacaoTransacional<R> {

    R executar(Connection conexao) throws SQLException;
}
