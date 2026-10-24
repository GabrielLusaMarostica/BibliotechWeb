package bibliotech.web;

import bibliotech.config.ContextoAplicacao;
import bibliotech.model.Usuario;
import bibliotech.web.dto.LoginRequisicao;
import bibliotech.web.dto.LoginResposta;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class AutenticacaoController {

    private final ContextoAplicacao contexto;
    private final SessaoServico sessoes;

    public AutenticacaoController(ContextoAplicacao contexto, SessaoServico sessoes) {
        this.contexto = contexto;
        this.sessoes = sessoes;
    }

    @PostMapping("/login")
    public LoginResposta login(@RequestBody LoginRequisicao corpo) {
        Usuario usuario = contexto.autenticacao().autenticar(corpo.login(), corpo.senha());
        return new LoginResposta(sessoes.criar(usuario.getLogin()), usuario.getLogin());
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader(value = "Authorization", required = false) String auth) {
        sessoes.encerrar(auth);
        return ResponseEntity.noContent().build();
    }

    /** Usado pelo front-end para saber se o token guardado ainda vale. */
    @GetMapping("/sessao")
    public Map<String, String> sessao(@RequestHeader(value = "Authorization", required = false) String auth) {
        return Map.of("login", sessoes.exigir(auth));
    }
}
