package bibliotech.web;

import bibliotech.config.ContextoAplicacao;
import bibliotech.excecao.RegraNegocioException;
import bibliotech.model.Emprestimo;
import bibliotech.web.dto.DevolucaoResposta;
import bibliotech.web.dto.EmprestimoRequisicao;
import bibliotech.web.dto.EmprestimoResposta;
import bibliotech.web.dto.ItemRequisicao;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/emprestimos")
public class EmprestimoController {

    private final ContextoAplicacao contexto;
    private final SessaoServico sessoes;
    private final Apresentador apresentador;

    public EmprestimoController(ContextoAplicacao contexto, SessaoServico sessoes, Apresentador apresentador) {
        this.contexto = contexto;
        this.sessoes = sessoes;
        this.apresentador = apresentador;
    }

    /** {@code situacao=pendentes} (padrão) lista só os não devolvidos; {@code todos} lista tudo. */
    @GetMapping
    public List<EmprestimoResposta> listar(@RequestHeader(value = "Authorization", required = false) String auth,
                                           @RequestParam(value = "situacao", defaultValue = "pendentes") String situacao) {
        sessoes.exigir(auth);
        List<Emprestimo> pendentes = contexto.emprestimos().listarPendentes();
        if ("pendentes".equals(situacao)) {
            return pendentes.stream().map(e -> apresentador.emprestimo(e, false)).toList();
        }
        if (!"todos".equals(situacao)) {
            throw new RegraNegocioException("Situação inválida: use 'pendentes' ou 'todos'.");
        }
        Set<Integer> idsPendentes = pendentes.stream().map(Emprestimo::getId).collect(Collectors.toSet());
        return contexto.emprestimos().listar().stream()
                .map(e -> apresentador.emprestimo(e, !idsPendentes.contains(e.getId()))).toList();
    }

    @GetMapping("/{id}")
    public EmprestimoResposta buscar(@RequestHeader(value = "Authorization", required = false) String auth,
                                     @PathVariable("id") int id) {
        sessoes.exigir(auth);
        Emprestimo e = contexto.emprestimos().buscarPorId(id);
        boolean pendente = contexto.emprestimos().listarPendentes().stream().anyMatch(p -> p.getId() == id);
        return apresentador.emprestimo(e, !pendente);
    }

    @PostMapping
    public ResponseEntity<EmprestimoResposta> registrar(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @RequestBody EmprestimoRequisicao corpo) {
        sessoes.exigir(auth);
        Map<Integer, Integer> pedido = new LinkedHashMap<>();
        if (corpo.itens() != null) {
            for (ItemRequisicao item : corpo.itens()) {
                int idLivro = Campos.inteiro(item.idLivro(), "idLivro");
                if (pedido.put(idLivro, Campos.inteiro(item.quantidade(), "quantidade")) != null) {
                    throw new RegraNegocioException("O mesmo livro aparece mais de uma vez no pedido.");
                }
            }
        }
        Emprestimo novo = contexto.emprestimos().registrar(Campos.inteiro(corpo.idAluno(), "idAluno"),
                Campos.inteiro(corpo.idFuncionario(), "idFuncionario"),
                Campos.data(corpo.dataPrevista(), "dataPrevista"), pedido);
        return ResponseEntity.status(HttpStatus.CREATED).body(apresentador.emprestimo(novo, false));
    }

    @PostMapping("/{id}/devolucao")
    public ResponseEntity<DevolucaoResposta> devolver(
            @RequestHeader(value = "Authorization", required = false) String auth, @PathVariable("id") int id) {
        sessoes.exigir(auth);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(DevolucaoResposta.de(contexto.devolucoes().registrar(id)));
    }
}
