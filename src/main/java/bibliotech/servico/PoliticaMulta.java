package bibliotech.servico;

import java.math.BigDecimal;

/**
 * Estratégia de cálculo de multa por atraso (padrão Strategy). Para mudar a
 * regra (valor por dia, teto, carência, isenção) basta criar outra
 * implementação; {@link DevolucaoServico} não precisa ser alterado (OCP).
 */
@FunctionalInterface
public interface PoliticaMulta {

    BigDecimal calcular(int diasAtraso);
}
