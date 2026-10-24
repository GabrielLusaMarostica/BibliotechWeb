package bibliotech.repositorio.memoria;

import bibliotech.excecao.RegraNegocioException;
import bibliotech.model.Emprestimo;
import bibliotech.repositorio.EmprestimoRepositorio;

/**
 * O estoque dos livros já foi baixado pelo serviço nas mesmas instâncias que
 * este repositório guarda, então aqui basta armazenar o empréstimo.
 */
public class EmprestimoMemoria extends RepositorioMemoria<Emprestimo> implements EmprestimoRepositorio {

    @Override
    public Emprestimo salvar(Emprestimo emprestimo) {
        if (emprestimo.getId() != 0) {
            throw new RegraNegocioException("Um empréstimo já registrado não pode ser alterado.");
        }
        return super.salvar(emprestimo);
    }
}
