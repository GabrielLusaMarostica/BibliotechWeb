package bibliotech.repositorio.memoria;

import bibliotech.model.Usuario;
import bibliotech.repositorio.UsuarioRepositorio;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class UsuarioMemoria implements UsuarioRepositorio {

    private final Map<String, Usuario> usuarios = new ConcurrentHashMap<>();

    @Override
    public Optional<Usuario> buscarPorLogin(String login) {
        return Optional.ofNullable(usuarios.get(login));
    }

    @Override
    public void salvar(Usuario usuario) {
        usuarios.put(usuario.getLogin(), usuario);
    }
}
