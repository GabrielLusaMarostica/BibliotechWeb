if (App.iniciar()) {
  const R = Validacao.regras;
  Crud.iniciar({
    url: "/api/funcionarios", singular: "Funcionário", rotulo: "nome", excluir: false,
    colunas: [{ campo: "nome" }, { campo: "matricula" }, { campo: "funcao" }],
    esquema: { nome: [R.obrigatorio, R.minimo(3), R.maximo(120)], matricula: [R.obrigatorio, R.matricula], funcao: [R.maximo(60)] }
  });
}
