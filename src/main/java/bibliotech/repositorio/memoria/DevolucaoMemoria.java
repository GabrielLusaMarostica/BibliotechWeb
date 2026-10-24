package bibliotech.repositorio.memoria;

import bibliotech.excecao.RegraNegocioException;
import bibliotech.model.Devolucao;
import bibliotech.repositorio.DevolucaoRepositorio;
import java.util.Optional;

public class DevolucaoMemoria extends RepositorioMemoria<Devolucao> implements DevolucaoRepositorio {

    @Override
    public Devolucao salvar(Devolucao devolucao) {
        if (devolucao.getId() != 0) {
            throw new RegraNegocioException("Uma devolução já registrada não pode ser alterada.");
        }
        if (buscarPorEmprestimo(devolucao.getEmprestimo().getId()).isPresent()) {
            throw new RegraNegocioException("Este empréstimo já foi devolvido.");
        }
        return super.salvar(devolucao);
    }

    @Override
    public Optional<Devolucao> buscarPorEmprestimo(int idEmprestimo) {
        return listar().stream().filter(d -> d.getEmprestimo().getId() == idEmprestimo).findFirst();
    }
}
