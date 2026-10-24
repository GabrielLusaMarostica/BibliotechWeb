package bibliotech.model;

import bibliotech.util.Validacao;

/**
 * Usuário do sistema (quem faz login). Guarda apenas o <em>hash</em> da senha,
 * nunca o texto original.
 */
public class Usuario {

    private final String login;
    private final String senhaHash;

    public Usuario(String login, String senhaHash) {
        this.login = Validacao.obrigatorio(login, "login", 50);
        this.senhaHash = Validacao.obrigatorio(senhaHash, "senha");
    }

    public String getLogin() {
        return login;
    }

    public String getSenhaHash() {
        return senhaHash;
    }
}
