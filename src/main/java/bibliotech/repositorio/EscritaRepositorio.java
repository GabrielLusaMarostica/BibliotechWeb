package bibliotech.repositorio;

/** Contrato de gravação. */
public interface EscritaRepositorio<T> {

    /**
     * Insere a entidade se ela ainda não tem id (id = 0), atribuindo o id gerado,
     * ou atualiza a existente. Devolve a própria entidade.
     *
     * @throws bibliotech.excecao.RecursoNaoEncontradoException se o id informado não existe
     */
    T salvar(T entidade);
}
