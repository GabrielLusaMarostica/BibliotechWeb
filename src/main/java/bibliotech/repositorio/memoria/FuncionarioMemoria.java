package bibliotech.repositorio.memoria;

import bibliotech.model.Funcionario;
import bibliotech.repositorio.FuncionarioRepositorio;
import java.util.Optional;

public class FuncionarioMemoria extends RepositorioMemoria<Funcionario> implements FuncionarioRepositorio {

    @Override
    public Optional<Funcionario> buscarPorMatricula(String matricula) {
        return listar().stream().filter(f -> f.getMatricula().equalsIgnoreCase(matricula)).findFirst();
    }
}
