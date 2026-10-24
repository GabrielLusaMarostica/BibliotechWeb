package bibliotech.web.dto;

import java.util.List;

/** Corpo do POST /api/emprestimos. A data vem como texto ISO (aaaa-mm-dd). */
public record EmprestimoRequisicao(Integer idAluno, Integer idFuncionario, String dataPrevista,
                                   List<ItemRequisicao> itens) {
}
