package bibliotech.model;

/**
 * Objeto de domínio com identificador numérico. O valor {@code 0} indica que a
 * entidade ainda não foi persistida; o repositório atribui o id no primeiro
 * {@code salvar}.
 */
public interface Entidade {

    int getId();

    void setId(int id);
}
