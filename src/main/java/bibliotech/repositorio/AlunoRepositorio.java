package bibliotech.repositorio;

import bibliotech.model.Aluno;
import java.util.List;
import java.util.Optional;

public interface AlunoRepositorio
        extends LeituraRepositorio<Aluno>, EscritaRepositorio<Aluno>, ExclusaoRepositorio {

    Optional<Aluno> buscarPorMatricula(String matricula);

    /** Busca por trecho do nome, sem diferenciar maiúsculas de minúsculas. */
    List<Aluno> buscarPorNome(String trecho);
}
