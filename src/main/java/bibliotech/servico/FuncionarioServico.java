package bibliotech.servico;

import bibliotech.excecao.RecursoNaoEncontradoException;
import bibliotech.excecao.RegraNegocioException;
import bibliotech.model.Funcionario;
import bibliotech.repositorio.FuncionarioRepositorio;
import java.util.List;

public class FuncionarioServico {

    private final FuncionarioRepositorio funcionarios;

    public FuncionarioServico(FuncionarioRepositorio funcionarios) {
        this.funcionarios = funcionarios;
    }

    public Funcionario cadastrar(String nome, String matricula, String funcao) {
        Funcionario novo = new Funcionario(nome, matricula, funcao);
        if (funcionarios.buscarPorMatricula(novo.getMatricula()).isPresent()) {
            throw new RegraNegocioException("Já existe um funcionário com a matrícula " + novo.getMatricula() + ".");
        }
        return funcionarios.salvar(novo);
    }

    /** Atualiza nome, matrícula e função; a matrícula não pode pertencer a outro funcionário. */
    public Funcionario atualizar(int id, String nome, String matricula, String funcao) {
        buscarPorId(id);
        Funcionario atualizado = new Funcionario(id, nome, matricula, funcao);
        funcionarios.buscarPorMatricula(atualizado.getMatricula()).ifPresent(existente -> {
            if (existente.getId() != id) {
                throw new RegraNegocioException("Já existe um funcionário com a matrícula "
                        + atualizado.getMatricula() + ".");
            }
        });
        return funcionarios.salvar(atualizado);
    }

    public Funcionario buscarPorId(int id) {
        return funcionarios.buscarPorId(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Funcionário não encontrado: " + id));
    }

    public List<Funcionario> listar() {
        return funcionarios.listar();
    }
}
