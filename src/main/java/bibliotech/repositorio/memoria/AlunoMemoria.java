package bibliotech.repositorio.memoria;

import bibliotech.model.Aluno;
import bibliotech.repositorio.AlunoRepositorio;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class AlunoMemoria extends RepositorioMemoria<Aluno> implements AlunoRepositorio {

    @Override
    public void excluir(int id) {
        remover(id);
    }

    @Override
    public Optional<Aluno> buscarPorMatricula(String matricula) {
        return listar().stream().filter(a -> a.getMatricula().equalsIgnoreCase(matricula)).findFirst();
    }

    @Override
    public List<Aluno> buscarPorNome(String trecho) {
        String t = trecho.toLowerCase(Locale.ROOT);
        return listar().stream().filter(a -> a.getNome().toLowerCase(Locale.ROOT).contains(t)).toList();
    }
}
