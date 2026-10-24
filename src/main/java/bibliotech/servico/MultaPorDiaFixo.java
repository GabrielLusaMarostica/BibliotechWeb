package bibliotech.servico;

import bibliotech.excecao.RegraNegocioException;
import java.math.BigDecimal;
import java.math.RoundingMode;

/** Multa de um valor fixo por dia de atraso (a versão desktop usava R$ 2,00, escrito direto no código). */
public class MultaPorDiaFixo implements PoliticaMulta {

    public static final BigDecimal VALOR_PADRAO_POR_DIA = new BigDecimal("2.00");

    private final BigDecimal valorPorDia;

    public MultaPorDiaFixo() {
        this(VALOR_PADRAO_POR_DIA);
    }

    public MultaPorDiaFixo(BigDecimal valorPorDia) {
        if (valorPorDia == null || valorPorDia.signum() < 0) {
            throw new RegraNegocioException("O valor da multa por dia não pode ser negativo.");
        }
        this.valorPorDia = valorPorDia;
    }

    @Override
    public BigDecimal calcular(int diasAtraso) {
        if (diasAtraso <= 0) {
            return BigDecimal.ZERO.setScale(2);
        }
        return valorPorDia.multiply(BigDecimal.valueOf(diasAtraso)).setScale(2, RoundingMode.HALF_UP);
    }
}
