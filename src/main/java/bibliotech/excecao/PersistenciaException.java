package bibliotech.excecao;

/**
 * Falha de infraestrutura ao acessar o banco de dados (conexão, SQL, transação).
 *
 * <p>Substitui o antigo {@code catch (Exception e) { System.out.println(...); }}
 * dos DAOs, que escondia o erro de quem chamava.</p>
 */
public class PersistenciaException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public PersistenciaException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }

    public PersistenciaException(String mensagem) {
        super(mensagem);
    }
}
