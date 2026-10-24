package bibliotech.web.dto;

import bibliotech.model.Livro;

public record LivroDto(Integer id, String titulo, String autor, String genero, Integer ano, Integer quantidade) {

    public static LivroDto de(Livro l) {
        return new LivroDto(l.getId(), l.getTitulo(), l.getAutor(), l.getGenero(), l.getAno(), l.getQuantidade());
    }
}
