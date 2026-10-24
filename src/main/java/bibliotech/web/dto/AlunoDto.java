package bibliotech.web.dto;

import bibliotech.model.Aluno;

public record AlunoDto(Integer id, String nome, String matricula, String serie, String contato) {

    public static AlunoDto de(Aluno a) {
        return new AlunoDto(a.getId(), a.getNome(), a.getMatricula(), a.getSerie(), a.getContato());
    }
}
