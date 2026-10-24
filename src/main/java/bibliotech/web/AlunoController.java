package bibliotech.web;

import bibliotech.config.ContextoAplicacao;
import bibliotech.model.Aluno;
import bibliotech.web.dto.AlunoDto;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/alunos")
public class AlunoController {

    private final ContextoAplicacao contexto;
    private final SessaoServico sessoes;

    public AlunoController(ContextoAplicacao contexto, SessaoServico sessoes) {
        this.contexto = contexto;
        this.sessoes = sessoes;
    }

    @GetMapping
    public List<AlunoDto> listar(@RequestHeader(value = "Authorization", required = false) String auth,
                                 @RequestParam(value = "busca", required = false) String busca) {
        sessoes.exigir(auth);
        List<Aluno> alunos = (busca == null || busca.isBlank())
                ? contexto.alunos().listar() : contexto.alunos().buscarPorNome(busca);
        return alunos.stream().map(AlunoDto::de).toList();
    }

    @GetMapping("/{id}")
    public AlunoDto buscar(@RequestHeader(value = "Authorization", required = false) String auth,
                           @PathVariable("id") int id) {
        sessoes.exigir(auth);
        return AlunoDto.de(contexto.alunos().buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<AlunoDto> cadastrar(@RequestHeader(value = "Authorization", required = false) String auth,
                                              @RequestBody AlunoDto corpo) {
        sessoes.exigir(auth);
        Aluno novo = contexto.alunos().cadastrar(corpo.nome(), corpo.matricula(), corpo.serie(), corpo.contato());
        return ResponseEntity.status(HttpStatus.CREATED).body(AlunoDto.de(novo));
    }

    @PutMapping("/{id}")
    public AlunoDto atualizar(@RequestHeader(value = "Authorization", required = false) String auth,
                              @PathVariable("id") int id, @RequestBody AlunoDto corpo) {
        sessoes.exigir(auth);
        return AlunoDto.de(contexto.alunos().atualizar(id, corpo.nome(), corpo.matricula(),
                corpo.serie(), corpo.contato()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@RequestHeader(value = "Authorization", required = false) String auth,
                                        @PathVariable("id") int id) {
        sessoes.exigir(auth);
        contexto.alunos().excluir(id);
        return ResponseEntity.noContent().build();
    }
}
