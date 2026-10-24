package bibliotech.model;

import bibliotech.util.Validacao;

/** Um livro e a quantidade de exemplares levados em um empréstimo. */
public class ItemEmprestimo {

    private final Livro livro;
    private final int quantidade;

    public ItemEmprestimo(Livro livro, int quantidade) {
        this.livro = Validacao.naoNulo(livro, "livro");
        this.quantidade = Validacao.positivo(quantidade, "quantidade");
    }

    public Livro getLivro() {
        return livro;
    }

    public int getQuantidade() {
        return quantidade;
    }
}
