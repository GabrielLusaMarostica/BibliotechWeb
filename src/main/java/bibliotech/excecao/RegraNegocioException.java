package bibliotech.excecao;

/**
 * Violação de uma regra de negócio ou de validação (dado obrigatório ausente,
 * data inválida, devolução duplicada etc.).
 *
 * <p>É uma exceção não verificada de propósito: a camada de apresentação
 * (Swing, Servlet, controller web) decide como exibir a mensagem, e o código de
 * negócio não precisa poluir todas as assinaturas com {@code throws}.</p>
 */
public class RegraNegocioException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}
