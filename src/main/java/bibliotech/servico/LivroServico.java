package bibliotech.servico;

import bibliotech.excecao.RecursoNaoEncontradoException;
import bibliotech.excecao.RegraNegocioException;
import bibliotech.model.Livro;
import bibliotech.repositorio.EmprestimoRepositorio;
import bibliotech.repositorio.LivroRepositorio;
import bibliotech.util.Validacao;
import java.util.List;

/** Casos de uso do acervo de livros. */
public class LivroServico {

    private final LivroRepositorio livros;
    private final EmprestimoRepositorio emprestimos;

    public LivroServico(LivroRepositorio livros, EmprestimoRepositorio emprestimos) {
        this.livros = livros;
        this.emprestimos = emprestimos;
    }

    public Livro cadastrar(String titulo, String autor, String genero, int ano, int quantidade) {
        return livros.salvar(new Livro(titulo, autor, genero, ano, quantidade));
    }

    public Livro atualizar(int id, String titulo, String autor, String genero, int ano, int quantidade) {
        buscarPorId(id);
        return livros.salvar(new Livro(id, titulo, autor, genero, ano, quantidade));
    }

    public void excluir(int id) {
        buscarPorId(id);
        boolean emUso = emprestimos.listar().stream()
                .anyMatch(e -> e.getItens().stream().anyMatch(i -> i.getLivro().getId() == id));
        if (emUso) {
            throw new RegraNegocioException(
                    "Não é possível excluir o livro: ele consta em empréstimos registrados.");
        }
        livros.excluir(id);
    }

    public Livro buscarPorId(int id) {
        return livros.buscarPorId(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Livro não encontrado: " + id));
    }

    public List<Livro> listar() {
        return livros.listar();
    }

    public List<Livro> buscarPorTitulo(String trecho) {
        return livros.buscarPorTitulo(Validacao.obrigatorio(trecho, "título"));
    }
}
