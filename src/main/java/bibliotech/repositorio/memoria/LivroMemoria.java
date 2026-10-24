package bibliotech.repositorio.memoria;

import bibliotech.model.Livro;
import bibliotech.repositorio.LivroRepositorio;
import java.util.List;
import java.util.Locale;

public class LivroMemoria extends RepositorioMemoria<Livro> implements LivroRepositorio {

    @Override
    public void excluir(int id) {
        remover(id);
    }

    @Override
    public List<Livro> buscarPorTitulo(String trecho) {
        String t = trecho.toLowerCase(Locale.ROOT);
        return listar().stream().filter(l -> l.getTitulo().toLowerCase(Locale.ROOT).contains(t)).toList();
    }
}
