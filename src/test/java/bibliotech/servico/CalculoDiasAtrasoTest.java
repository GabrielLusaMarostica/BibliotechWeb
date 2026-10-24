package bibliotech.servico;

import static org.junit.Assert.assertEquals;

import java.time.LocalDate;
import org.junit.Test;

/**
 * Testes do cálculo de dias de atraso ({@link DevolucaoServico#calcularDiasAtraso}).
 * Era o ponto onde a versão desktop errava (contava dias desde 1970).
 */
public class CalculoDiasAtrasoTest {

    @Test
    public void devolucaoNoDiaPrevistoNaoTemAtraso() {
        LocalDate dia = LocalDate.of(2026, 10, 8);
        assertEquals(0, DevolucaoServico.calcularDiasAtraso(dia, dia));
    }

    @Test
    public void devolucaoAntecipadaNaoTemAtraso() {
        assertEquals(0, DevolucaoServico.calcularDiasAtraso(
                LocalDate.of(2026, 10, 20), LocalDate.of(2026, 10, 11)));
    }

    @Test
    public void tresDiasDepoisDoPrazoSaoTresDiasDeAtraso() {
        assertEquals(3, DevolucaoServico.calcularDiasAtraso(
                LocalDate.of(2026, 10, 8), LocalDate.of(2026, 10, 11)));
    }

    @Test
    public void atrasoAtravessandoMudancaDeMes() {
        assertEquals(5, DevolucaoServico.calcularDiasAtraso(
                LocalDate.of(2026, 10, 29), LocalDate.of(2026, 11, 3)));
    }

    @Test
    public void atrasoAtravessandoMudancaDeAno() {
        assertEquals(3, DevolucaoServico.calcularDiasAtraso(
                LocalDate.of(2026, 12, 30), LocalDate.of(2027, 1, 2)));
    }

    @Test
    public void atrasoEmAnoBissexto() {
        // 2028 é bissexto: 28/02 -> 01/03 são 2 dias (existe 29/02)
        assertEquals(2, DevolucaoServico.calcularDiasAtraso(
                LocalDate.of(2028, 2, 28), LocalDate.of(2028, 3, 1)));
    }

    @Test
    public void atrasoNuncaEAbsurdoComoNaVersaoDesktop() {
        int dias = DevolucaoServico.calcularDiasAtraso(
                LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 11));
        assertEquals(10, dias); // e não ~20 mil dias desde 1970
    }
}
