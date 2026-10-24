package bibliotech.repositorio;

import bibliotech.model.Funcionario;
import java.util.Optional;

/** Funcionários podem ser consultados e gravados, mas não excluídos (como no sistema desktop). */
public interface FuncionarioRepositorio
        extends LeituraRepositorio<Funcionario>, EscritaRepositorio<Funcionario> {

    Optional<Funcionario> buscarPorMatricula(String matricula);
}
