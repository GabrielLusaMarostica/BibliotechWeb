package bibliotech.repositorio.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;

/** Converte a linha atual de um {@link ResultSet} em um objeto de domínio. */
@FunctionalInterface
public interface MapeadorLinha<T> {

    T mapear(ResultSet rs) throws SQLException;
}
