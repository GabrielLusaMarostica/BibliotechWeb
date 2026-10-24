if (App.iniciar()) {
  const R = Validacao.regras;
  Crud.iniciar({
    url: "/api/livros", singular: "Livro", rotulo: "titulo", buscaServidor: true, numericos: ["ano", "quantidade"],
    colunas: [
      { campo: "titulo" }, { campo: "autor" }, { campo: "genero" }, { campo: "ano" },
      { campo: "quantidade", formato: (l) => {
        const classe = l.quantidade === 0 ? "atraso" : l.quantidade <= 1 ? "baixo" : "ok";
        const texto = l.quantidade === 0 ? "Indisponível" : l.quantidade + " un.";
        return `<span class="selo ${classe}">${texto}</span>`;
      } }
    ],
    esquema: { titulo: [R.obrigatorio, R.maximo(200)], autor: [R.obrigatorio, R.maximo(120)], genero: [R.maximo(60)], ano: [R.obrigatorio, R.ano], quantidade: [R.obrigatorio, R.inteiroNaoNegativo] }
  });
}
