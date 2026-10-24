package bibliotech.repositorio;

import bibliotech.model.Emprestimo;

/**
 * Empréstimos só são lidos e criados.
 *
 * <p>{@link #salvar(Emprestimo)} grava o empréstimo, seus itens e a baixa de
 * estoque dos livros como uma única operação atômica (transação). Se algo
 * falhar, nada é gravado.</p>
 */
public interface EmprestimoRepositorio
        extends LeituraRepositorio<Emprestimo>, EscritaRepositorio<Emprestimo> {
}
