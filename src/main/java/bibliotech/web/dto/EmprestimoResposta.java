package bibliotech.web.dto;

import java.math.BigDecimal;
import java.util.List;

public record EmprestimoResposta(int id, int idAluno, String aluno, int idFuncionario, String funcionario,
                                 String dataEmprestimo, String dataPrevista, List<ItemResposta> itens,
                                 boolean devolvido, int diasAtraso, BigDecimal multaPrevista) {
}
