package bibliotech.web;

import bibliotech.model.Emprestimo;
import bibliotech.servico.DevolucaoServico;
import bibliotech.servico.PoliticaMulta;
import bibliotech.web.dto.EmprestimoResposta;
import bibliotech.web.dto.ItemResposta;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

/** Converte entidades de domínio em respostas JSON (a camada web não expõe o model direto). */
public class Apresentador {

    private final Clock relogio;
    private final PoliticaMulta politicaMulta;

    public Apresentador(Clock relogio, PoliticaMulta politicaMulta) {
        this.relogio = relogio;
        this.politicaMulta = politicaMulta;
    }

    public EmprestimoResposta emprestimo(Emprestimo e, boolean devolvido) {
        int atraso = 0;
        if (!devolvido) {
            atraso = DevolucaoServico.calcularDiasAtraso(e.getDataPrevista(), LocalDate.now(relogio));
        }
        List<ItemResposta> itens = e.getItens().stream()
                .map(i -> new ItemResposta(i.getLivro().getId(), i.getLivro().getTitulo(), i.getQuantidade()))
                .toList();
        return new EmprestimoResposta(e.getId(), e.getAluno().getId(), e.getAluno().getNome(),
                e.getFuncionario().getId(), e.getFuncionario().getNome(), e.getDataEmprestimo().toString(),
                e.getDataPrevista().toString(), itens, devolvido, atraso, politicaMulta.calcular(atraso));
    }
}
