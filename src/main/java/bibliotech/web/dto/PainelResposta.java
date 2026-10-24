package bibliotech.web.dto;

import java.util.List;

public record PainelResposta(int exemplaresEmEstoque, int alunos, int pendentes, int atrasados,
                             List<EmprestimoResposta> urgentes, List<LivroDto> estoqueBaixo) {
}
