package bibliotech.web.dto;

import bibliotech.model.Funcionario;

public record FuncionarioDto(Integer id, String nome, String matricula, String funcao) {

    public static FuncionarioDto de(Funcionario f) {
        return new FuncionarioDto(f.getId(), f.getNome(), f.getMatricula(), f.getFuncao());
    }
}
