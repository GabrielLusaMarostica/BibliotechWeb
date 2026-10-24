package bibliotech.servico;

import bibliotech.excecao.RecursoNaoEncontradoException;
import bibliotech.excecao.RegraNegocioException;
import bibliotech.model.Aluno;
import bibliotech.model.Emprestimo;
import bibliotech.model.Funcionario;
import bibliotech.model.ItemEmprestimo;
import bibliotech.model.Livro;
import bibliotech.repositorio.AlunoRepositorio;
import bibliotech.repositorio.DevolucaoRepositorio;
import bibliotech.repositorio.EmprestimoRepositorio;
import bibliotech.repositorio.FuncionarioRepositorio;
import bibliotech.repositorio.LivroRepositorio;
import bibliotech.util.Validacao;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Caso de uso "registrar empréstimo". Orquestra as regras que antes viviam no
 * botão Cadastrar de {@code TelaEmprestimo}: validar dados, conferir estoque,
 * dar baixa e gravar.
 */
public class EmprestimoServico {

    private final EmprestimoRepositorio emprestimos;
    private final AlunoRepositorio alunos;
    private final FuncionarioRepositorio funcionarios;
    private final LivroRepositorio livros;
    private final DevolucaoRepositorio devolucoes;
    private final Clock relogio;

    public EmprestimoServico(EmprestimoRepositorio emprestimos, AlunoRepositorio alunos,
                             FuncionarioRepositorio funcionarios, LivroRepositorio livros,
                             DevolucaoRepositorio devolucoes, Clock relogio) {
        this.emprestimos = emprestimos;
        this.alunos = alunos;
        this.funcionarios = funcionarios;
        this.livros = livros;
        this.devolucoes = devolucoes;
        this.relogio = relogio;
    }

    /**
     * @param livrosEQuantidades mapa id do livro → quantidade de exemplares
     * @return o empréstimo gravado, já com id
     */
    public Emprestimo registrar(int idAluno, int idFuncionario, LocalDate dataPrevista,
                                Map<Integer, Integer> livrosEQuantidades) {
        if (livrosEQuantidades == null || livrosEQuantidades.isEmpty()) {
            throw new RegraNegocioException("Informe ao menos um livro para o empréstimo.");
        }
        Validacao.naoNulo(dataPrevista, "data prevista de devolução");

        Aluno aluno = alunos.buscarPorId(idAluno).orElseThrow(
                () -> new RecursoNaoEncontradoException("Aluno não encontrado: " + idAluno));
        Funcionario funcionario = funcionarios.buscarPorId(idFuncionario).orElseThrow(
                () -> new RecursoNaoEncontradoException("Funcionário não encontrado: " + idFuncionario));

        Emprestimo emprestimo = new Emprestimo(0, aluno, funcionario, LocalDate.now(relogio), dataPrevista);
        for (Map.Entry<Integer, Integer> pedido : livrosEQuantidades.entrySet()) {
            Livro livro = livros.buscarPorId(pedido.getKey()).orElseThrow(
                    () -> new RecursoNaoEncontradoException("Livro não encontrado: " + pedido.getKey()));
            emprestimo.adicionarItem(new ItemEmprestimo(livro, pedido.getValue()));
        }

        // 1) confere o estoque de TODOS os itens antes de alterar qualquer um
        for (ItemEmprestimo item : emprestimo.getItens()) {
            item.getLivro().verificarDisponibilidade(item.getQuantidade());
        }
        // 2) dá baixa nos livros
        for (ItemEmprestimo item : emprestimo.getItens()) {
            item.getLivro().emprestar(item.getQuantidade());
        }
        // 3) grava; se falhar, desfaz a baixa feita nos objetos
        try {
            return emprestimos.salvar(emprestimo);
        } catch (RuntimeException e) {
            for (ItemEmprestimo item : emprestimo.getItens()) {
                item.getLivro().devolver(item.getQuantidade());
            }
            throw e;
        }
    }

    public Emprestimo buscarPorId(int id) {
        return emprestimos.buscarPorId(id).orElseThrow(
                () -> new RecursoNaoEncontradoException("Empréstimo não encontrado: " + id));
    }

    public List<Emprestimo> listar() {
        return emprestimos.listar();
    }

    /** Empréstimos ainda sem devolução (lista exibida na tela de devolução). */
    public List<Emprestimo> listarPendentes() {
        return emprestimos.listar().stream()
                .filter(e -> devolucoes.buscarPorEmprestimo(e.getId()).isEmpty())
                .toList();
    }
}
