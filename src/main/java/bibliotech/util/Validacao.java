package bibliotech.util;

import bibliotech.excecao.RegraNegocioException;

/** Validações reutilizáveis pelas classes de modelo e de serviço. */
public final class Validacao {

    private Validacao() {
    }

    /** Exige texto não vazio e devolve o valor sem espaços nas pontas. */
    public static String obrigatorio(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new RegraNegocioException("O campo '" + campo + "' é obrigatório.");
        }
        return valor.trim();
    }

    /** Como {@link #obrigatorio(String, String)}, mas também limita o tamanho (o mesmo do VARCHAR no banco). */
    public static String obrigatorio(String valor, String campo, int maximo) {
        return limitar(obrigatorio(valor, campo), campo, maximo);
    }

    /** Texto opcional com tamanho máximo. */
    public static String opcional(String valor, String campo, int maximo) {
        return limitar(opcional(valor), campo, maximo);
    }

    private static String limitar(String valor, String campo, int maximo) {
        if (valor.length() > maximo) {
            throw new RegraNegocioException("O campo '" + campo + "' deve ter no máximo " + maximo + " caracteres.");
        }
        return valor;
    }

    /** Texto opcional: nulo vira vazio e as pontas são aparadas. */
    public static String opcional(String valor) {
        return valor == null ? "" : valor.trim();
    }

    public static int positivo(int valor, String campo) {
        if (valor <= 0) {
            throw new RegraNegocioException("O campo '" + campo + "' deve ser maior que zero.");
        }
        return valor;
    }

    public static int naoNegativo(int valor, String campo) {
        if (valor < 0) {
            throw new RegraNegocioException("O campo '" + campo + "' não pode ser negativo.");
        }
        return valor;
    }

    public static <T> T naoNulo(T valor, String campo) {
        if (valor == null) {
            throw new RegraNegocioException("O campo '" + campo + "' é obrigatório.");
        }
        return valor;
    }
}
