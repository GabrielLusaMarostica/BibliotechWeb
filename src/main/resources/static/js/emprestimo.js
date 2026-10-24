if (App.iniciar()) {
  const form = document.getElementById("form-emprestimo");
  const selAluno = document.getElementById("aluno");
  const selFunc = document.getElementById("funcionario");
  const selLivro = document.getElementById("livro");
  const qtd = document.getElementById("qtd");
  const prevista = document.getElementById("prevista");
  const lista = document.getElementById("lista-itens");
  const erroItem = document.getElementById("erro-item");
  const erroLista = document.getElementById("erro-lista");
  const R = Validacao.regras;
  let livros = [];
  let itens = []; // { livroId, quantidade }

  const opcoes = (arr, texto) => '<option value="">Selecione...</option>' +
    arr.map((x) => `<option value="${x.id}">${App.esc(texto(x))}</option>`).join("");

  async function carregarLivros() {
    livros = await Api.get("/api/livros");
    selLivro.innerHTML = opcoes(livros, (l) => `${l.titulo} – ${l.quantidade === 0 ? "indisponível" : l.quantidade + " disp."}`);
  }
  async function iniciarDados() {
    try {
      const [alunos, funcionarios] = await Promise.all([Api.get("/api/alunos"), Api.get("/api/funcionarios")]);
      selAluno.innerHTML = opcoes(alunos, (a) => `${a.nome} (${a.matricula})`);
      selFunc.innerHTML = opcoes(funcionarios, (f) => f.nome);
      await carregarLivros();
    } catch (e) { App.erro(e); }
  }
  function padraoPrevista() { prevista.value = App.somarDiasISO(7); }
  padraoPrevista(); iniciarDados();

  function desenharItens() {
    lista.innerHTML = itens.length === 0
      ? '<p class="vazio">Nenhum livro adicionado.</p>'
      : itens.map((it, i) => {
          const l = livros.find((x) => x.id === it.livroId);
          return `<div class="item-lista"><span>${App.esc(l.titulo)} <strong>× ${it.quantidade}</strong></span>
            <button type="button" class="botao perigo pequeno" data-remover="${i}" aria-label="Remover ${App.esc(l.titulo)}">Remover</button></div>`;
        }).join("");
    document.getElementById("total-exemplares").textContent = itens.reduce((s, i) => s + i.quantidade, 0);
  }

  document.getElementById("btn-adicionar").addEventListener("click", () => {
    erroItem.textContent = ""; erroLista.textContent = "";
    const livro = livros.find((l) => l.id === Number(selLivro.value));
    const q = Number(qtd.value);
    if (!livro) { erroItem.textContent = "Selecione um livro."; return; }
    if (!Number.isInteger(q) || q <= 0) { erroItem.textContent = "A quantidade deve ser um inteiro maior que zero."; return; }
    if (itens.some((i) => i.livroId === livro.id)) { erroItem.textContent = "Este livro já está na lista."; return; }
    if (q > livro.quantidade) {
      erroItem.textContent = `Estoque insuficiente: "${livro.titulo}" tem ${livro.quantidade} disponível(is).`; return;
    }
    itens.push({ livroId: livro.id, quantidade: q });
    selLivro.value = ""; qtd.value = 1;
    desenharItens();
  });
  lista.addEventListener("click", (e) => {
    const b = e.target.closest("[data-remover]"); if (!b) return;
    itens.splice(Number(b.dataset.remover), 1); desenharItens();
  });

  Validacao.ligar(form, { aluno: [R.obrigatorio], funcionario: [R.obrigatorio], prevista: [R.dataFutura] }, async () => {
    if (itens.length === 0) { erroLista.textContent = "Adicione pelo menos um livro."; return; }
    const botao = form.querySelector("button[type=submit]");
    botao.disabled = true;
    try {
      // o servidor confere tudo de novo: o pedido é gravado por inteiro ou recusado por inteiro
      await Api.post("/api/emprestimos", {
        idAluno: Number(selAluno.value), idFuncionario: Number(selFunc.value), dataPrevista: prevista.value,
        itens: itens.map((i) => ({ idLivro: i.livroId, quantidade: i.quantidade }))
      });
      App.aviso("Empréstimo registrado com sucesso.");
      itens = []; Validacao.limpar(form); padraoPrevista(); desenharItens();
    } catch (e) {
      erroLista.textContent = e.message;
    } finally {
      botao.disabled = false;
      try { await carregarLivros(); } catch (e) { /* mantém a lista antiga */ }
    }
  });
  form.addEventListener("reset", () => { setTimeout(() => { itens = []; Validacao.limpar(form); padraoPrevista(); desenharItens(); erroItem.textContent = ""; erroLista.textContent = ""; }); });
  desenharItens();
}
