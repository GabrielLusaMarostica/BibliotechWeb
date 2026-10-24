package bibliotech.repositorio;

import bibliotech.model.Usuario;
import java.util.Optional;

public interface UsuarioRepositorio {

    Optional<Usuario> buscarPorLogin(String login);

    /** Cria o usuário ou atualiza o hash da senha se o login já existir. */
    void salvar(Usuario usuario);
}
