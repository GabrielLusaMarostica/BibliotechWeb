package bibliotech.model;

import bibliotech.excecao.RegraNegocioException;
import bibliotech.util.Validacao;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Registro da devolução de um empréstimo.
 *
 * <p>A multa é recebida já calculada: quem decide o valor é a
 * {@code PoliticaMulta} (Strategy), não esta classe. O valor monetário usa
 * {@link BigDecimal}, não {@code double}.</p>
 */
public class Devolucao implements Entidade {

    private int id;
    private final Emprestimo emprestimo;
    private final LocalDate dataDevolucao;
    private final int diasAtraso;
    private final BigDecimal multa;

    public Devolucao(int id, Emprestimo emprestimo, LocalDate dataDevolucao,
                     int diasAtraso, BigDecimal multa) {
        this.id = id;
        this.emprestimo = Validacao.naoNulo(emprestimo, "empréstimo");
        this.dataDevolucao = Validacao.naoNulo(dataDevolucao, "data de devolução");
        this.diasAtraso = Validacao.naoNegativo(diasAtraso, "dias de atraso");
        Validacao.naoNulo(multa, "multa");
        if (multa.signum() < 0) {
            throw new RegraNegocioException("A multa não pode ser negativa.");
        }
        this.multa = multa;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }

    public Emprestimo getEmprestimo() {
        return emprestimo;
    }

    public LocalDate getDataDevolucao() {
        return dataDevolucao;
    }

    public int getDiasAtraso() {
        return diasAtraso;
    }

    public BigDecimal getMulta() {
        return multa;
    }
}
