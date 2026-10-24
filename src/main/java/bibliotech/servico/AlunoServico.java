package bibliotech.servico;

import bibliotech.excecao.RecursoNaoEncontradoException;
import bibliotech.excecao.RegraNegocioException;
import bibliotech.model.Aluno;
import bibliotech.repositorio.AlunoRepositorio;
import bibliotech.repositorio.EmprestimoRepositorio;
import bibliotech.util.Validacao;
import java.util.List;

/** Casos de uso de alunos (antes espalhados em TelaAlunos/TelaCadastroAlunos). */
public class AlunoServico {

    private final AlunoRepositorio alunos;
    private final EmprestimoRepositorio emprestimos;

    public AlunoServico(AlunoRepositorio alunos, EmprestimoRepositorio emprestimos) {
        this.alunos = alunos;
        this.emprestimos = emprestimos;
    }

    public Aluno cadastrar(String nome, String matricula, String serie, String contato) {
        Aluno novo = new Aluno(nome, matricula, serie, contato);
        garantirMatriculaLivre(novo.getMatricula(), 0);
        return alunos.salvar(novo);
    }

    public Aluno atualizar(int id, String nome, String matricula, String serie, String contato) {
        buscarPorId(id);
        Aluno atualizado = new Aluno(id, nome, matricula, serie, contato);
        garantirMatriculaLivre(atualizado.getMatricula(), id);
        return alunos.salvar(atualizado);
    }

    public void excluir(int id) {
        buscarPorId(id);
        if (emprestimos.listar().stream().anyMatch(e -> e.getAluno().getId() == id)) {
            throw new RegraNegocioException(
                    "Não é possível excluir o aluno: ele possui empréstimos registrados.");
        }
        alunos.excluir(id);
    }

    public Aluno buscarPorId(int id) {
        return alunos.buscarPorId(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Aluno não encontrado: " + id));
    }

    public List<Aluno> listar() {
        return alunos.listar();
    }

    public List<Aluno> buscarPorNome(String trecho) {
        return alunos.buscarPorNome(Validacao.obrigatorio(trecho, "nome"));
    }

    private void garantirMatriculaLivre(String matricula, int idDoProprio) {
        alunos.buscarPorMatricula(matricula).ifPresent(existente -> {
            if (existente.getId() != idDoProprio) {
                throw new RegraNegocioException("Já existe um aluno com a matrícula " + matricula + ".");
            }
        });
    }
}
