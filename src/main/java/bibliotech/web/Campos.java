package bibliotech.web;

import bibliotech.excecao.RegraNegocioException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/** Conversões de campos opcionais do JSON para tipos primitivos, com mensagem clara. */
final class Campos {

    private Campos() {
    }

    static int inteiro(Integer valor, String campo) {
        if (valor == null) {
            throw new RegraNegocioException("O campo " + campo + " é obrigatório.");
        }
        return valor;
    }

    static LocalDate data(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new RegraNegocioException("O campo " + campo + " é obrigatório.");
        }
        try {
            return LocalDate.parse(valor.trim());
        } catch (DateTimeParseException e) {
            throw new RegraNegocioException("Data inválida em " + campo + " (use aaaa-mm-dd).");
        }
    }
}
