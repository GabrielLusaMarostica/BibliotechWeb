package bibliotech.repositorio.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/** Preenche os parâmetros ({@code ?}) de um {@link PreparedStatement}. */
@FunctionalInterface
public interface ConfiguradorStatement {

    ConfiguradorStatement SEM_PARAMETROS = ps -> { };

    void configurar(PreparedStatement ps) throws SQLException;
}
