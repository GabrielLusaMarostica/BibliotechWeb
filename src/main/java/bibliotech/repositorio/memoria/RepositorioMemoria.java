package bibliotech.repositorio.memoria;

import bibliotech.excecao.RecursoNaoEncontradoException;
import bibliotech.model.Entidade;
import bibliotech.repositorio.EscritaRepositorio;
import bibliotech.repositorio.LeituraRepositorio;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Base comum dos repositórios em memória (usados nos testes do {@code main()} e
 * em demonstrações sem banco). Mantém o mesmo comportamento observável das
 * implementações JDBC, o que permite trocar uma pela outra (LSP).
 */
public abstract class RepositorioMemoria<T extends Entidade>
        implements LeituraRepositorio<T>, EscritaRepositorio<T> {

    private final Map<Integer, T> dados = new LinkedHashMap<>();
    private int proximoId = 1;

    @Override
    public synchronized List<T> listar() {
        return new ArrayList<>(dados.values());
    }

    @Override
    public synchronized Optional<T> buscarPorId(int id) {
        return Optional.ofNullable(dados.get(id));
    }

    @Override
    public synchronized T salvar(T entidade) {
        if (entidade.getId() == 0) {
            entidade.setId(proximoId++);
        } else if (!dados.containsKey(entidade.getId())) {
            throw new RecursoNaoEncontradoException(
                    "Registro não encontrado: id " + entidade.getId());
        }
        dados.put(entidade.getId(), entidade);
        return entidade;
    }

    protected synchronized void remover(int id) {
        if (dados.remove(id) == null) {
            throw new RecursoNaoEncontradoException("Registro não encontrado: id " + id);
        }
    }
}
