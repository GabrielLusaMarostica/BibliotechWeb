package bibliotech.web;

import bibliotech.config.ContextoAplicacao;
import bibliotech.model.Funcionario;
import bibliotech.web.dto.FuncionarioDto;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/funcionarios")
public class FuncionarioController {

    private final ContextoAplicacao contexto;
    private final SessaoServico sessoes;

    public FuncionarioController(ContextoAplicacao contexto, SessaoServico sessoes) {
        this.contexto = contexto;
        this.sessoes = sessoes;
    }

    @GetMapping
    public List<FuncionarioDto> listar(@RequestHeader(value = "Authorization", required = false) String auth) {
        sessoes.exigir(auth);
        return contexto.funcionarios().listar().stream().map(FuncionarioDto::de).toList();
    }

    @GetMapping("/{id}")
    public FuncionarioDto buscar(@RequestHeader(value = "Authorization", required = false) String auth,
                                 @PathVariable("id") int id) {
        sessoes.exigir(auth);
        return FuncionarioDto.de(contexto.funcionarios().buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<FuncionarioDto> cadastrar(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @RequestBody FuncionarioDto corpo) {
        sessoes.exigir(auth);
        Funcionario novo = contexto.funcionarios().cadastrar(corpo.nome(), corpo.matricula(), corpo.funcao());
        return ResponseEntity.status(HttpStatus.CREATED).body(FuncionarioDto.de(novo));
    }

    @PutMapping("/{id}")
    public FuncionarioDto atualizar(@RequestHeader(value = "Authorization", required = false) String auth,
                                    @PathVariable("id") int id, @RequestBody FuncionarioDto corpo) {
        sessoes.exigir(auth);
        return FuncionarioDto.de(contexto.funcionarios().atualizar(id, corpo.nome(), corpo.matricula(),
                corpo.funcao()));
    }
}
