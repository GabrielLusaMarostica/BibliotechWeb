package bibliotech.repositorio.jdbc;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Abstração para obter conexões (DIP). Hoje há uma implementação baseada em
 * {@code DriverManager}; na versão web pode ser trocada por um pool
 * ({@code DataSource}) sem alterar nenhum repositório.
 */
public interface FabricaConexao {

    Connection abrir() throws SQLException;
}
