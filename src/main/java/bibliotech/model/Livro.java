package bibliotech.model;

import bibliotech.excecao.EstoqueInsuficienteException;
import bibliotech.util.Validacao;

/**
 * Livro do acervo com controle de estoque.
 *
 * <p>Regra de estoque mora aqui (e não na tela): {@link #emprestar(int)} e
 * {@link #devolver(int)} mantêm a quantidade consistente. Quando não há
 * exemplares, lança {@link EstoqueInsuficienteException} em vez de imprimir
 * "Livro indisponível!" no console como fazia a versão desktop.</p>
 */
public class Livro implements Entidade {

    private int id;
    private final String titulo;
    private final String autor;
    private final String genero;
    private final int ano;
    private int quantidade;

    public Livro(int id, String titulo, String autor, String genero, int ano, int quantidade) {
        this.id = id;
        this.titulo = Validacao.obrigatorio(titulo, "título", 200);
        this.autor = Validacao.obrigatorio(autor, "autor", 120);
        this.genero = Validacao.opcional(genero, "gênero", 60);
        this.ano = Validacao.positivo(ano, "ano");
        this.quantidade = Validacao.naoNegativo(quantidade, "quantidade");
    }

    public Livro(String titulo, String autor, String genero, int ano, int quantidade) {
        this(0, titulo, autor, genero, ano, quantidade);
    }

    /** Confere se há exemplares suficientes, sem alterar o estoque. */
    public void verificarDisponibilidade(int exemplares) {
        Validacao.positivo(exemplares, "quantidade");
        if (quantidade < exemplares) {
            throw new EstoqueInsuficienteException("Livro '" + titulo + "' indisponível: pedidos "
                    + exemplares + ", em estoque " + quantidade + ".");
        }
    }

    /** Baixa {@code exemplares} do estoque. */
    public void emprestar(int exemplares) {
        verificarDisponibilidade(exemplares);
        quantidade -= exemplares;
    }

    /** Devolve {@code exemplares} ao estoque. */
    public void devolver(int exemplares) {
        Validacao.positivo(exemplares, "quantidade");
        quantidade += exemplares;
    }

    public boolean isDisponivel() {
        return quantidade > 0;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getAutor() {
        return autor;
    }

    public String getGenero() {
        return genero;
    }

    public int getAno() {
        return ano;
    }

    public int getQuantidade() {
        return quantidade;
    }

    @Override
    public String toString() {
        return "Livro{id=" + id + ", titulo=" + titulo + ", quantidade=" + quantidade + "}";
    }
}
