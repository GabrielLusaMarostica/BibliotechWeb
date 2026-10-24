package bibliotech.servico;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Hash de senha com PBKDF2-HMAC-SHA256 e sal aleatório (somente biblioteca padrão do Java).
 * Formato guardado: {@code iteracoes:salBase64:hashBase64}.
 */
public class CodificadorSenhaPbkdf2 implements CodificadorSenha {

    public static final int ITERACOES_PADRAO = 600_000;
    private static final int TAMANHO_SAL = 16;
    private static final int TAMANHO_HASH_BITS = 256;

    private final int iteracoes;
    private final SecureRandom aleatorio = new SecureRandom();

    public CodificadorSenhaPbkdf2() {
        this(ITERACOES_PADRAO);
    }

    public CodificadorSenhaPbkdf2(int iteracoes) {
        this.iteracoes = iteracoes;
    }

    @Override
    public String codificar(String senhaPlana) {
        byte[] sal = new byte[TAMANHO_SAL];
        aleatorio.nextBytes(sal);
        byte[] hash = derivar(senhaPlana, sal, iteracoes);
        return iteracoes + ":" + Base64.getEncoder().encodeToString(sal)
                + ":" + Base64.getEncoder().encodeToString(hash);
    }

    @Override
    public boolean confere(String senhaPlana, String hashGuardado) {
        try {
            String[] partes = hashGuardado.split(":");
            if (partes.length != 3) {
                return false;
            }
            int it = Integer.parseInt(partes[0]);
            byte[] sal = Base64.getDecoder().decode(partes[1]);
            byte[] esperado = Base64.getDecoder().decode(partes[2]);
            byte[] calculado = derivar(senhaPlana, sal, it);
            return MessageDigest.isEqual(esperado, calculado);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static byte[] derivar(String senha, byte[] sal, int iteracoes) {
        PBEKeySpec spec = new PBEKeySpec(senha.toCharArray(), sal, iteracoes, TAMANHO_HASH_BITS);
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("PBKDF2 indisponível nesta JVM.", e);
        } finally {
            spec.clearPassword();
        }
    }
}
