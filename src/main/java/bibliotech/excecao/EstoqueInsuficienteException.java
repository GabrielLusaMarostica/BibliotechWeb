package bibliotech.excecao;

/** Tentativa de emprestar mais exemplares do que existem em estoque. */
public class EstoqueInsuficienteException extends RegraNegocioException {

    private static final long serialVersionUID = 1L;

    public EstoqueInsuficienteException(String mensagem) {
        super(mensagem);
    }
}
