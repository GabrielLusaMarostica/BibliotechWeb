package bibliotech.web;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Sessões em memória: depois do login o cliente recebe um token aleatório e o
 * envia no cabeçalho {@code Authorization: Bearer <token>}. As sessões expiram
 * e somem se o servidor for reiniciado (suficiente para este projeto).
 */
public class SessaoServico {

    private static final Duration VALIDADE = Duration.ofHours(8);

    private record Sessao(String login, Instant expira) {
    }

    private final Map<String, Sessao> sessoes = new ConcurrentHashMap<>();
    private final SecureRandom aleatorio = new SecureRandom();
    private final Clock relogio;

    public SessaoServico(Clock relogio) {
        this.relogio = relogio;
    }

    public String criar(String login) {
        byte[] bytes = new byte[32];
        aleatorio.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        sessoes.put(token, new Sessao(login, relogio.instant().plus(VALIDADE)));
        return token;
    }

    /** Valida o cabeçalho Authorization e devolve o login logado. */
    public String exigir(String cabecalho) {
        if (cabecalho == null || !cabecalho.startsWith("Bearer ")) {
            throw new NaoAutenticadoException("Faça login para continuar.");
        }
        String token = cabecalho.substring("Bearer ".length()).trim();
        Sessao sessao = sessoes.get(token);
        if (sessao == null || sessao.expira().isBefore(relogio.instant())) {
            sessoes.remove(token);
            throw new NaoAutenticadoException("Sessão expirada. Faça login novamente.");
        }
        return sessao.login();
    }

    public void encerrar(String cabecalho) {
        if (cabecalho != null && cabecalho.startsWith("Bearer ")) {
            sessoes.remove(cabecalho.substring("Bearer ".length()).trim());
        }
    }
}
