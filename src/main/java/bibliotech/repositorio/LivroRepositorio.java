package bibliotech.repositorio;

import bibliotech.model.Livro;
import java.util.List;

public interface LivroRepositorio
        extends LeituraRepositorio<Livro>, EscritaRepositorio<Livro>, ExclusaoRepositorio {

    /** Busca por trecho do título, sem diferenciar maiúsculas de minúsculas. */
    List<Livro> buscarPorTitulo(String trecho);
}
