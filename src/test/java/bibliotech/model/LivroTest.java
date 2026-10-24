package bibliotech.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import bibliotech.excecao.EstoqueInsuficienteException;
import bibliotech.excecao.RegraNegocioException;
import org.junit.Before;
import org.junit.Test;

/** Regras de estoque da entidade Livro. */
public class LivroTest {

    private Livro livro;

    @Before
    public void criarLivro() {
        livro = new Livro("Dom Casmurro", "Machado de Assis", "Romance", 1899, 3);
    }

    @Test
    public void emprestarBaixaOEstoque() {
        livro.emprestar(2);
        assertEquals(1, livro.getQuantidade());
    }

    @Test
    public void emprestarTodoOEstoqueDeixaOLivroIndisponivel() {
        livro.emprestar(3);
        assertEquals(0, livro.getQuantidade());
        assertFalse(livro.isDisponivel());
    }

    @Test
    public void emprestarMaisDoQueOEstoqueLancaExcecaoENaoAlteraNada() {
        assertThrows(EstoqueInsuficienteException.class, () -> livro.emprestar(4));
        assertEquals(3, livro.getQuantidade());
    }

    @Test
    public void devolverRestauraOEstoque() {
        livro.emprestar(2);
        livro.devolver(2);
        assertEquals(3, livro.getQuantidade());
        assertTrue(livro.isDisponivel());
    }

    @Test
    public void quantidadeZeroOuNegativaNoEmprestimoERejeitada() {
        assertThrows(RegraNegocioException.class, () -> livro.emprestar(0));
        assertThrows(RegraNegocioException.class, () -> livro.emprestar(-1));
        assertThrows(RegraNegocioException.class, () -> livro.devolver(0));
    }

    @Test
    public void verificarDisponibilidadeNaoAlteraOEstoque() {
        livro.verificarDisponibilidade(3);
        assertEquals(3, livro.getQuantidade());
    }

    @Test
    public void livroNaoPodeSerCriadoComQuantidadeNegativa() {
        assertThrows(RegraNegocioException.class, () -> new Livro("X", "Y", "Z", 2000, -1));
    }

    @Test
    public void livroExigeTituloEAutor() {
        assertThrows(RegraNegocioException.class, () -> new Livro("  ", "Autor", "G", 2000, 1));
        assertThrows(RegraNegocioException.class, () -> new Livro("Titulo", null, "G", 2000, 1));
    }

    @Test
    public void livroExigeAnoPositivo() {
        assertThrows(RegraNegocioException.class, () -> new Livro("T", "A", "G", 0, 1));
    }
}
