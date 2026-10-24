package bibliotech.web.dto;

import bibliotech.model.Devolucao;
import java.math.BigDecimal;

public record DevolucaoResposta(int id, int idEmprestimo, String dataDevolucao, int diasAtraso, BigDecimal multa) {

    public static DevolucaoResposta de(Devolucao d) {
        return new DevolucaoResposta(d.getId(), d.getEmprestimo().getId(), d.getDataDevolucao().toString(),
                d.getDiasAtraso(), d.getMulta());
    }
}
