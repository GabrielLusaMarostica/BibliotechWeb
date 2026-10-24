package bibliotech.servico;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import bibliotech.excecao.RegraNegocioException;
import java.math.BigDecimal;
import org.junit.Test;

/** Testes do cálculo da multa (regra de negócio pura, sem banco). */
public class MultaPorDiaFixoTest {

    private static BigDecimal valor(String texto) {
        return new BigDecimal(texto);
    }

    @Test
    public void semAtrasoNaoHaMulta() {
        assertEquals(0, new MultaPorDiaFixo().calcular(0).compareTo(BigDecimal.ZERO));
    }

    @Test
    public void diasNegativosNaoGeramMulta() {
        assertEquals(0, new MultaPorDiaFixo().calcular(-5).signum());
    }

    @Test
    public void tresDiasDeAtrasoNoValorPadraoDaSeisReais() {
        assertEquals(valor("6.00"), new MultaPorDiaFixo().calcular(3));
    }

    @Test
    public void umDiaDeAtrasoCobraUmaDiaria() {
        assertEquals(valor("2.00"), new MultaPorDiaFixo().calcular(1));
    }

    @Test
    public void valorPorDiaPodeSerConfigurado() {
        assertEquals(valor("2.00"), new MultaPorDiaFixo(valor("0.50")).calcular(4));
    }

    @Test
    public void resultadoSempreTemDuasCasasDecimais() {
        assertEquals(2, new MultaPorDiaFixo(valor("0.333")).calcular(3).scale());
        assertEquals(valor("1.00"), new MultaPorDiaFixo(valor("0.333")).calcular(3));
    }

    @Test
    public void valorPorDiaNegativoERejeitado() {
        assertThrows(RegraNegocioException.class, () -> new MultaPorDiaFixo(valor("-1")));
    }

    @Test
    public void valorPorDiaNuloERejeitado() {
        assertThrows(RegraNegocioException.class, () -> new MultaPorDiaFixo(null));
    }

    @Test
    public void politicaPodeSerSubstituidaPorOutraEstrategia() {
        // Strategy: nova regra (teto de R$ 10,00) sem alterar nenhuma classe existente
        PoliticaMulta comTeto = dias -> new MultaPorDiaFixo().calcular(dias).min(valor("10.00"));
        assertEquals(valor("10.00"), comTeto.calcular(30));
        assertEquals(valor("4.00"), comTeto.calcular(2));
    }
}
