package bibliotech.model;

import bibliotech.excecao.RegraNegocioException;
import bibliotech.util.Validacao;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Empréstimo de um ou mais livros a um aluno.
 *
 * <p>Diferenças em relação à versão desktop: não há construtor vazio (que
 * permitia criar empréstimo sem aluno), a lista de itens não é exposta para
 * modificação externa, {@link #adicionarItem(ItemEmprestimo)} não tem efeito
 * colateral sobre o estoque (isso é feito pelo serviço) e o resumo é devolvido
 * como texto em vez de ser impresso no console.</p>
 */
public class Emprestimo implements Entidade {

    private int id;
    private final Aluno aluno;
    private final Funcionario funcionario;
    private final LocalDate dataEmprestimo;
    private final LocalDate dataPrevista;
    private final List<ItemEmprestimo> itens = new ArrayList<>();

    public Emprestimo(int id, Aluno aluno, Funcionario funcionario,
                      LocalDate dataEmprestimo, LocalDate dataPrevista) {
        this.id = id;
        this.aluno = Validacao.naoNulo(aluno, "aluno");
        this.funcionario = Validacao.naoNulo(funcionario, "funcionário");
        this.dataEmprestimo = Validacao.naoNulo(dataEmprestimo, "data do empréstimo");
        this.dataPrevista = Validacao.naoNulo(dataPrevista, "data prevista de devolução");
        if (dataPrevista.isBefore(dataEmprestimo)) {
            throw new RegraNegocioException(
                    "A data prevista de devolução não pode ser anterior à data do empréstimo.");
        }
    }

    /** Adiciona um item; o mesmo livro não pode aparecer duas vezes. */
    public void adicionarItem(ItemEmprestimo item) {
        Validacao.naoNulo(item, "item");
        for (ItemEmprestimo existente : itens) {
            Livro a = existente.getLivro();
            Livro b = item.getLivro();
            if (a == b || (a.getId() != 0 && a.getId() == b.getId())) {
                throw new RegraNegocioException(
                        "O livro '" + b.getTitulo() + "' já foi adicionado a este empréstimo.");
            }
        }
        itens.add(item);
    }

    /** Texto de resumo para exibição por qualquer camada de apresentação. */
    public String resumo() {
        StringBuilder sb = new StringBuilder();
        sb.append("Aluno: ").append(aluno.getNome()).append('\n');
        sb.append("Funcionário: ").append(funcionario.getNome()).append('\n');
        sb.append("Livros emprestados:");
        for (ItemEmprestimo item : itens) {
            sb.append("\n- ").append(item.getLivro().getTitulo())
              .append(" (x").append(item.getQuantidade()).append(')');
        }
        return sb.toString();
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }

    public Aluno getAluno() {
        return aluno;
    }

    public Funcionario getFuncionario() {
        return funcionario;
    }

    public LocalDate getDataEmprestimo() {
        return dataEmprestimo;
    }

    public LocalDate getDataPrevista() {
        return dataPrevista;
    }

    /** Visão somente leitura dos itens. */
    public List<ItemEmprestimo> getItens() {
        return Collections.unmodifiableList(itens);
    }
}
