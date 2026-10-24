package bibliotech.servico;

/** Abstração do algoritmo de hash de senha (DIP): o serviço de login não conhece o algoritmo. */
public interface CodificadorSenha {

    String codificar(String senhaPlana);

    boolean confere(String senhaPlana, String hashGuardado);
}
