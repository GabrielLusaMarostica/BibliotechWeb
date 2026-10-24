if (App.iniciar()) {
  const R = Validacao.regras;
  Crud.iniciar({
    url: "/api/alunos", singular: "Aluno", rotulo: "nome", buscaServidor: true,
    colunas: [{ campo: "nome" }, { campo: "matricula" }, { campo: "serie" }, { campo: "contato" }],
    esquema: { nome: [R.obrigatorio, R.minimo(3), R.maximo(120)], matricula: [R.obrigatorio, R.matricula], serie: [R.maximo(30)], contato: [R.maximo(120)] }
  });
}
