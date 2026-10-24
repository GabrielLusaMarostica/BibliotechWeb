package bibliotech.repositorio;

import java.util.List;
import java.util.Optional;

/** Contrato de consulta. Separado da escrita (ISP): quem só lê não depende de salvar. */
public interface LeituraRepositorio<T> {

    List<T> listar();

    Optional<T> buscarPorId(int id);
}
