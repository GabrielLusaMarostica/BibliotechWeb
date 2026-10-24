package bibliotech.servico;

import bibliotech.excecao.RecursoNaoEncontradoException;
import bibliotech.excecao.RegraNegocioException;
import bibliotech.model.Devolucao;
import bibliotech.model.Emprestimo;
import bibliotech.model.ItemEmprestimo;
import bibliotech.repositorio.DevolucaoRepositorio;
import bibliotech.repositorio.EmprestimoRepositorio;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Caso de uso "registrar devolução". Calcula o atraso de verdade (data de hoje
 * menos data prevista), pede o valor da multa à {@link PoliticaMulta} e devolve
 * os livros ao estoque.
 */
public class DevolucaoServico {

    private final EmprestimoRepositorio emprestimos;
    private final DevolucaoRepositorio devolucoes;
    private final PoliticaMulta politicaMulta;
    private final Clock relogio;

    public DevolucaoServico(EmprestimoRepositorio emprestimos, DevolucaoRepositorio devolucoes,
                            PoliticaMulta politicaMulta, Clock relogio) {
        this.emprestimos = emprestimos;
        this.devolucoes = devolucoes;
        this.politicaMulta = politicaMulta;
        this.relogio = relogio;
    }

    public Devolucao registrar(int idEmprestimo) {
        Emprestimo emprestimo = emprestimos.buscarPorId(idEmprestimo).orElseThrow(
                () -> new RecursoNaoEncontradoException("Empréstimo não encontrado: " + idEmprestimo));
        if (devolucoes.buscarPorEmprestimo(idEmprestimo).isPresent()) {
            throw new RegraNegocioException("Este empréstimo já foi devolvido.");
        }

        LocalDate hoje = LocalDate.now(relogio);
        int diasAtraso = calcularDiasAtraso(emprestimo.getDataPrevista(), hoje);
        BigDecimal multa = politicaMulta.calcular(diasAtraso);
        Devolucao devolucao = new Devolucao(0, emprestimo, hoje, diasAtraso, multa);

        for (ItemEmprestimo item : emprestimo.getItens()) {
            item.getLivro().devolver(item.getQuantidade());
        }
        try {
            return devolucoes.salvar(devolucao);
        } catch (RuntimeException e) {
            for (ItemEmprestimo item : emprestimo.getItens()) {
                item.getLivro().emprestar(item.getQuantidade());
            }
            throw e;
        }
    }

    /** Dias entre a data prevista e a devolução; nunca negativo. */
    public static int calcularDiasAtraso(LocalDate dataPrevista, LocalDate dataDevolucao) {
        return (int) Math.max(0, ChronoUnit.DAYS.between(dataPrevista, dataDevolucao));
    }
}
