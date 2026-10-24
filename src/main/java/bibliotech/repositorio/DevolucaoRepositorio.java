package bibliotech.repositorio;

import bibliotech.model.Devolucao;
import java.util.Optional;

/**
 * {@link #salvar(Devolucao)} grava a devolução e devolve ao estoque os livros
 * do empréstimo na mesma transação.
 */
public interface DevolucaoRepositorio extends EscritaRepositorio<Devolucao> {

    Optional<Devolucao> buscarPorEmprestimo(int idEmprestimo);
}
