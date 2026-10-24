package bibliotech.repositorio.jdbc;

import bibliotech.excecao.PersistenciaException;

/**
 * Parâmetros de conexão com o banco.
 *
 * <p>Na versão desktop, URL, usuário e senha estavam escritos no código de
 * {@code Conexao.java} (e foram parar no GitHub). Aqui eles vêm de fora do código:
 * variáveis de ambiente {@code BIBLIOTECH_DB_URL}, {@code BIBLIOTECH_DB_USER},
 * {@code BIBLIOTECH_DB_PASSWORD} ou propriedades do Java
 * ({@code -Dbibliotech.db.url=...}, {@code -Dbibliotech.db.user=...},
 * {@code -Dbibliotech.db.password=...}).</p>
 */
public final class ConfiguracaoBanco {

    public static final String URL_PADRAO = "jdbc:mysql://localhost:3306/bibliotech?useSSL=false&allowPublicKeyRetrieval=true&characterEncoding=UTF-8";
    public static final String USUARIO_PADRAO = "bibliotech";

    private final String url;
    private final String usuario;
    private final String senha;

    public ConfiguracaoBanco(String url, String usuario, String senha) {
        this.url = url;
        this.usuario = usuario;
        this.senha = senha;
    }

    /** Há senha configurada no ambiente? (indica que o banco foi configurado). */
    public static boolean estaConfigurado() {
        return ler("BIBLIOTECH_DB_PASSWORD", "bibliotech.db.password") != null;
    }

    public static ConfiguracaoBanco doAmbiente() {
        String senha = ler("BIBLIOTECH_DB_PASSWORD", "bibliotech.db.password");
        if (senha == null) {
            throw new PersistenciaException("Senha do banco não configurada. Defina a variável de "
                    + "ambiente BIBLIOTECH_DB_PASSWORD (ou -Dbibliotech.db.password).");
        }
        String url = ler("BIBLIOTECH_DB_URL", "bibliotech.db.url");
        String usuario = ler("BIBLIOTECH_DB_USER", "bibliotech.db.user");
        return new ConfiguracaoBanco(url != null ? url : URL_PADRAO,
                usuario != null ? usuario : USUARIO_PADRAO, senha);
    }

    private static String ler(String variavel, String propriedade) {
        String valor = System.getenv(variavel);
        return valor != null ? valor : System.getProperty(propriedade);
    }

    public String getUrl() {
        return url;
    }

    public String getUsuario() {
        return usuario;
    }

    public String getSenha() {
        return senha;
    }
}
