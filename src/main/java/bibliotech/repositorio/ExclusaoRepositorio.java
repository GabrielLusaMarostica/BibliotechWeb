package bibliotech.repositorio;

/** Contrato de exclusão, implementado só pelos repositórios que permitem apagar. */
public interface ExclusaoRepositorio {

    void excluir(int id);
}
