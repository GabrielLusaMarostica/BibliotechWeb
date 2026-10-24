package bibliotech.excecao;

/** Aluno, livro, funcionário ou empréstimo inexistente. */
public class RecursoNaoEncontradoException extends RegraNegocioException {

    private static final long serialVersionUID = 1L;

    public RecursoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
