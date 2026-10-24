package bibliotech.model;

import bibliotech.util.Validacao;

/** Funcionário da biblioteca que registra empréstimos e devoluções. */
public class Funcionario implements Entidade {

    private int id;
    private final String nome;
    private final String matricula;
    private final String funcao;

    public Funcionario(int id, String nome, String matricula, String funcao) {
        this.id = id;
        this.nome = Validacao.obrigatorio(nome, "nome", 120);
        this.matricula = Validacao.obrigatorio(matricula, "matrícula", 30);
        this.funcao = Validacao.opcional(funcao, "função", 60);
    }

    public Funcionario(String nome, String matricula, String funcao) {
        this(0, nome, matricula, funcao);
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

    public String getFuncao() {
        return funcao;
    }

    @Override
    public String toString() {
        return "Funcionario{id=" + id + ", nome=" + nome + ", funcao=" + funcao + "}";
    }
}
