package bibliotech.web;

import bibliotech.config.ContextoAplicacao;
import bibliotech.model.Livro;
import bibliotech.web.dto.LivroDto;
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
@RequestMapping("/api/livros")
public class LivroController {

    private final ContextoAplicacao contexto;
    private final SessaoServico sessoes;

    public LivroController(ContextoAplicacao contexto, SessaoServico sessoes) {
        this.contexto = contexto;
        this.sessoes = sessoes;
    }

    @GetMapping
    public List<LivroDto> listar(@RequestHeader(value = "Authorization", required = false) String auth,
                                 @RequestParam(value = "busca", required = false) String busca) {
        sessoes.exigir(auth);
        List<Livro> livros = (busca == null || busca.isBlank())
                ? contexto.livros().listar() : contexto.livros().buscarPorTitulo(busca);
        return livros.stream().map(LivroDto::de).toList();
    }

    @GetMapping("/{id}")
    public LivroDto buscar(@RequestHeader(value = "Authorization", required = false) String auth,
                           @PathVariable("id") int id) {
        sessoes.exigir(auth);
        return LivroDto.de(contexto.livros().buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<LivroDto> cadastrar(@RequestHeader(value = "Authorization", required = false) String auth,
                                              @RequestBody LivroDto corpo) {
        sessoes.exigir(auth);
        Livro novo = contexto.livros().cadastrar(corpo.titulo(), corpo.autor(), corpo.genero(),
                Campos.inteiro(corpo.ano(), "ano"), Campos.inteiro(corpo.quantidade(), "quantidade"));
        return ResponseEntity.status(HttpStatus.CREATED).body(LivroDto.de(novo));
    }

    @PutMapping("/{id}")
    public LivroDto atualizar(@RequestHeader(value = "Authorization", required = false) String auth,
                              @PathVariable("id") int id, @RequestBody LivroDto corpo) {
        sessoes.exigir(auth);
        return LivroDto.de(contexto.livros().atualizar(id, corpo.titulo(), corpo.autor(), corpo.genero(),
                Campos.inteiro(corpo.ano(), "ano"), Campos.inteiro(corpo.quantidade(), "quantidade")));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@RequestHeader(value = "Authorization", required = false) String auth,
                                        @PathVariable("id") int id) {
        sessoes.exigir(auth);
        contexto.livros().excluir(id);
        return ResponseEntity.noContent().build();
    }
}
