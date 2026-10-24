package bibliotech.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import bibliotech.excecao.RegraNegocioException;
import bibliotech.util.Validacao;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.Test;

/** Validações de dados das entidades e da classe utilitária Validacao. */
public class EntidadesValidacaoTest {

    @Test
    public void alunoExigeNomeEMatricula() {
        assertThrows(RegraNegocioException.class, () -> new Aluno("", "A1", "1º", ""));
        assertThrows(RegraNegocioException.class, () -> new Aluno("Ana", "   ", "1º", ""));
        assertThrows(RegraNegocioException.class, () -> new Aluno(null, "A1", "1º", ""));
    }

    @Test
    public void alunoAparaEspacosENormalizaCamposOpcionais() {
        Aluno a = new Aluno("  Ana Souza  ", " A1 ", null, null);
        assertEquals("Ana Souza", a.getNome());
        assertEquals("A1", a.getMatricula());
        assertEquals("", a.getSerie());
        assertEquals("", a.getContato());
    }

    @Test
    public void novaEntidadeNaoTemIdAteSerSalva() {
        assertEquals(0, new Aluno("Ana", "A1", "", "").getId());
    }

    @Test
    public void funcionarioExigeNomeEMatricula() {
        assertThrows(RegraNegocioException.class, () -> new Funcionario("", "F1", "x"));
        assertThrows(RegraNegocioException.class, () -> new Funcionario("Carlos", "", "x"));
    }

    @Test
    public void usuarioExigeLoginESenha() {
        assertThrows(RegraNegocioException.class, () -> new Usuario("", "hash"));
        assertThrows(RegraNegocioException.class, () -> new Usuario("login", null));
    }

    @Test
    public void devolucaoNaoAceitaMultaOuAtrasoNegativos() {
        Aluno a = new Aluno("Ana", "A1", "", "");
        Funcionario f = new Funcionario("Carlos", "F1", "");
        LocalDate hoje = LocalDate.of(2026, 10, 1);
        Emprestimo e = new Emprestimo(1, a, f, hoje, hoje.plusDays(7));
        assertThrows(RegraNegocioException.class, () -> new Devolucao(0, e, hoje, -1, BigDecimal.ZERO));
        assertThrows(RegraNegocioException.class, () -> new Devolucao(0, e, hoje, 0, new BigDecimal("-1")));
        assertThrows(RegraNegocioException.class, () -> new Devolucao(0, null, hoje, 0, BigDecimal.ZERO));
    }

    @Test
    public void validacaoObrigatorioRejeitaNuloEVazio() {
        assertThrows(RegraNegocioException.class, () -> Validacao.obrigatorio(null, "campo"));
        assertThrows(RegraNegocioException.class, () -> Validacao.obrigatorio("   ", "campo"));
        assertEquals("ok", Validacao.obrigatorio(" ok ", "campo"));
    }

    @Test
    public void validacaoDeNumeros() {
        assertEquals(5, Validacao.positivo(5, "n"));
        assertEquals(0, Validacao.naoNegativo(0, "n"));
        assertThrows(RegraNegocioException.class, () -> Validacao.positivo(0, "n"));
        assertThrows(RegraNegocioException.class, () -> Validacao.naoNegativo(-1, "n"));
    }

    @Test
    public void mensagemDeErroCitaOCampo() {
        RegraNegocioException e = assertThrows(RegraNegocioException.class,
                () -> Validacao.obrigatorio("", "matrícula"));
        assertEquals("O campo 'matrícula' é obrigatório.", e.getMessage());
    }
}
