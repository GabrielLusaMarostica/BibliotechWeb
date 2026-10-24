package bibliotech.web;

/** Requisição sem sessão válida (HTTP 401). */
public class NaoAutenticadoException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public NaoAutenticadoException(String mensagem) {
        super(mensagem);
    }
}
