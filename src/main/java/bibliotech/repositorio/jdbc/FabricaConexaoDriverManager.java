package bibliotech.repositorio.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class FabricaConexaoDriverManager implements FabricaConexao {

    private final ConfiguracaoBanco configuracao;

    public FabricaConexaoDriverManager(ConfiguracaoBanco configuracao) {
        this.configuracao = configuracao;
    }

    @Override
    public Connection abrir() throws SQLException {
        return DriverManager.getConnection(
                configuracao.getUrl(), configuracao.getUsuario(), configuracao.getSenha());
    }
}
