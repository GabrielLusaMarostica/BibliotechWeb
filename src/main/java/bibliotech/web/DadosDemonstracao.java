package bibliotech.web;

import bibliotech.config.ContextoAplicacao;
import bibliotech.model.Aluno;
import bibliotech.model.Funcionario;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Map;

/** Dados de exemplo usados só no modo memória, para a demonstração funcionar sem banco. */
final class DadosDemonstracao {

    private DadosDemonstracao() {
    }

    static void popular(ContextoAplicacao c, Clock relogio) {
        Aluno ana = c.alunos().cadastrar("Ana Beatriz Souza", "2024001", "3º ano A", "ana.souza@escola.edu.br");
        Aluno bruno = c.alunos().cadastrar("Bruno Lima", "2024002", "2º ano B", "(11) 90000-0002");
        c.alunos().cadastrar("Carla Mendes", "2024003", "1º ano C", "carla.mendes@escola.edu.br");
        Funcionario marta = c.funcionarios().cadastrar("Marta Oliveira", "F001", "Bibliotecária");
        c.funcionarios().cadastrar("Paulo Henrique", "F002", "Auxiliar");
        int dom = c.livros().cadastrar("Dom Casmurro", "Machado de Assis", "Romance", 1899, 4).getId();
        int cortico = c.livros().cadastrar("O Cortiço", "Aluísio Azevedo", "Romance", 1890, 1).getId();
        int capitaes = c.livros().cadastrar("Capitães da Areia", "Jorge Amado", "Romance", 1937, 3).getId();
        c.livros().cadastrar("Vidas Secas", "Graciliano Ramos", "Romance", 1938, 0);
        LocalDate hoje = LocalDate.now(relogio);
        c.emprestimos().registrar(ana.getId(), marta.getId(), hoje.plusDays(7), Map.of(cortico, 1));
        c.emprestimos().registrar(bruno.getId(), marta.getId(), hoje.plusDays(7), Map.of(dom, 1, capitaes, 1));
    }
}
