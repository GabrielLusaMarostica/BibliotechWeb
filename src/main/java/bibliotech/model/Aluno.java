package bibliotech.model;

import bibliotech.util.Validacao;

/**
 * Aluno atendido pela biblioteca.
 *
 * <p>Segue a convenção JavaBean ({@code getX}) para poder ser usado
 * diretamente em páginas JSP/JSTL ou templates na versão web.</p>
 */
public class Aluno implements Entidade {

    private int id;
    private final String nome;
    private final String matricula;
    private final String serie;
    private final String contato;

    public Aluno(int id, String nome, String matricula, String serie, String contato) {
        this.id = id;
        this.nome = Validacao.obrigatorio(nome, "nome", 120);
        this.matricula = Validacao.obrigatorio(matricula, "matrícula", 30);
        this.serie = Validacao.opcional(serie, "série", 30);
        this.contato = Validacao.opcional(contato, "contato", 120);
    }

    public Aluno(String nome, String matricula, String serie, String contato) {
        this(0, nome, matricula, serie, contato);
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public String getMatricula() {
        return matricula;
    }

    public String getSerie() {
        return serie;
    }

    public String getContato() {
        return contato;
    }

    @Override
    public String toString() {
        return "Aluno{id=" + id + ", nome=" + nome + ", matricula=" + matricula + "}";
    }
}
