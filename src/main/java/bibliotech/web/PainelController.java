package bibliotech.web;

import bibliotech.config.ContextoAplicacao;
import bibliotech.model.Emprestimo;
import bibliotech.model.Livro;
import bibliotech.web.dto.EmprestimoResposta;
import bibliotech.web.dto.LivroDto;
import bibliotech.web.dto.PainelResposta;
import java.util.Comparator;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/painel")
public class PainelController {

    private static final int ESTOQUE_BAIXO = 1;
    private static final int MAXIMO_URGENTES = 5;

    private final ContextoAplicacao contexto;
    private final SessaoServico sessoes;
    private final Apresentador apresentador;

    public PainelController(ContextoAplicacao contexto, SessaoServico sessoes, Apresentador apresentador) {
        this.contexto = contexto;
        this.sessoes = sessoes;
        this.apresentador = apresentador;
    }

    @GetMapping
    public PainelResposta painel(@RequestHeader(value = "Authorization", required = false) String auth) {
        sessoes.exigir(auth);
        List<Livro> livros = contexto.livros().listar();
        List<EmprestimoResposta> pendentes = contexto.emprestimos().listarPendentes().stream()
                .sorted(Comparator.comparing(Emprestimo::getDataPrevista))
                .map(e -> apresentador.emprestimo(e, false)).toList();
        int atrasados = (int) pendentes.stream().filter(p -> p.diasAtraso() > 0).count();
        return new PainelResposta(
                livros.stream().mapToInt(Livro::getQuantidade).sum(),
                contexto.alunos().listar().size(),
                pendentes.size(),
                atrasados,
                pendentes.stream().limit(MAXIMO_URGENTES).toList(),
                livros.stream().filter(l -> l.getQuantidade() <= ESTOQUE_BAIXO).map(LivroDto::de).toList());
    }
}
