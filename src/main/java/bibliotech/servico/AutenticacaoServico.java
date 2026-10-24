package bibliotech.servico;

import bibliotech.excecao.RegraNegocioException;
import bibliotech.model.Usuario;
import bibliotech.repositorio.UsuarioRepositorio;
import bibliotech.util.Validacao;

/** Login e cadastro de usuários, com senha protegida por hash. */
public class AutenticacaoServico {

    private static final int TAMANHO_MINIMO_SENHA = 6;
    private static final String MENSAGEM_INVALIDO = "Login ou senha inválidos.";

    private final UsuarioRepositorio usuarios;
    private final CodificadorSenha codificador;

    public AutenticacaoServico(UsuarioRepositorio usuarios, CodificadorSenha codificador) {
        this.usuarios = usuarios;
        this.codificador = codificador;
    }

    /** A mesma mensagem é usada para login inexistente e senha errada, para não revelar quais logins existem. */
    public Usuario autenticar(String login, String senha) {
        if (login == null || login.isBlank() || senha == null) {
            throw new RegraNegocioException(MENSAGEM_INVALIDO);
        }
        Usuario usuario = usuarios.buscarPorLogin(login.trim())
                .orElseThrow(() -> new RegraNegocioException(MENSAGEM_INVALIDO));
        if (!codificador.confere(senha, usuario.getSenhaHash())) {
            throw new RegraNegocioException(MENSAGEM_INVALIDO);
        }
        return usuario;
    }

    public Usuario cadastrar(String login, String senha) {
        String loginLimpo = Validacao.obrigatorio(login, "login");
        if (senha == null || senha.length() < TAMANHO_MINIMO_SENHA) {
            throw new RegraNegocioException("A senha deve ter pelo menos " + TAMANHO_MINIMO_SENHA + " caracteres.");
        }
        if (usuarios.buscarPorLogin(loginLimpo).isPresent()) {
            throw new RegraNegocioException("Já existe um usuário com o login " + loginLimpo + ".");
        }
        Usuario usuario = new Usuario(loginLimpo, codificador.codificar(senha));
        usuarios.salvar(usuario);
        return usuario;
    }
}
