/* Cadastro generico (lista + busca + formulario em janela modal) usado por
   alunos, livros e funcionarios. Cada pagina so informa a configuracao e
   a URL da API REST correspondente. */
const Crud = (() => {
  function iniciar(cfg) {
    const corpo = document.getElementById("corpo-tabela");
    const busca = document.getElementById("busca");
    const dialogo = document.getElementById("dialogo-form");
    const form = document.getElementById("form-cadastro");
    const titulo = document.getElementById("titulo-dialogo");
    const contador = document.getElementById("contador");
    let registros = [];
    let editandoId = null;
    let versaoBusca = 0;
    const podeExcluir = cfg.excluir !== false;

    async function carregar() {
      const minha = ++versaoBusca;
      try {
        const termo = busca.value.trim();
        const dados = await Api.get(cfg.url + (termo && cfg.buscaServidor ? "?busca=" + encodeURIComponent(termo) : ""));
        if (minha !== versaoBusca) return; // chegou uma resposta mais nova
        registros = termo && !cfg.buscaServidor
          ? dados.filter((r) => cfg.colunas.some((c) => String(r[c.campo] == null ? "" : r[c.campo]).toLowerCase().includes(termo.toLowerCase())))
          : dados;
        desenhar();
      } catch (e) { if (minha === versaoBusca) { corpo.innerHTML = `<tr><td class="vazio" colspan="${cfg.colunas.length + 1}">Não foi possível carregar a lista.</td></tr>`; App.erro(e); } }
    }

    function desenhar() {
      contador.textContent = `${registros.length} registro(s)`;
      if (registros.length === 0) {
        corpo.innerHTML = `<tr><td class="vazio" colspan="${cfg.colunas.length + 1}">Nenhum registro encontrado.</td></tr>`;
        return;
      }
      corpo.innerHTML = registros.map((r) =>
        `<tr>` + cfg.colunas.map((c) => `<td>${c.formato ? c.formato(r) : App.esc(r[c.campo])}</td>`).join("") +
        `<td class="acoes"><button class="botao secundario pequeno" data-acao="editar" data-id="${r.id}" aria-label="Editar ${App.esc(r[cfg.rotulo])}">Editar</button>` +
        (podeExcluir ? `<button class="botao perigo pequeno" data-acao="excluir" data-id="${r.id}" aria-label="Excluir ${App.esc(r[cfg.rotulo])}">Excluir</button>` : "") +
        `</td></tr>`).join("");
    }

    function abrir(registro) {
      Validacao.limpar(form);
      editandoId = registro ? registro.id : null;
      titulo.textContent = (registro ? "Editar " : "Novo(a) ") + cfg.singular;
      Object.keys(cfg.esquema).forEach((id) => { form.querySelector("#" + id).value = registro && registro[id] != null ? registro[id] : ""; });
      dialogo.showModal();
      form.querySelector("input").focus();
    }

    Validacao.ligar(form, cfg.esquema, async () => {
      const dados = {};
      Object.keys(cfg.esquema).forEach((id) => {
        const v = form.querySelector("#" + id).value.trim();
        dados[id] = cfg.numericos && cfg.numericos.includes(id) ? Number(v) : v;
      });
      const botao = form.querySelector("button[type=submit]");
      botao.disabled = true;
      try {
        if (editandoId) await Api.put(`${cfg.url}/${editandoId}`, dados);
        else await Api.post(cfg.url, dados);
        dialogo.close();
        App.aviso(cfg.singular + (editandoId ? " atualizado(a)." : " cadastrado(a)."));
        await carregar();
      } catch (e) { App.erro(e); } finally { botao.disabled = false; }
    });

    document.getElementById("btn-novo").addEventListener("click", () => abrir(null));
    document.getElementById("btn-cancelar").addEventListener("click", () => dialogo.close());
    let temporizador = null;
    busca.addEventListener("input", () => { clearTimeout(temporizador); temporizador = setTimeout(carregar, 250); });
    corpo.addEventListener("click", async (e) => {
      const b = e.target.closest("button[data-acao]"); if (!b) return;
      const id = Number(b.dataset.id);
      const reg = registros.find((r) => r.id === id);
      if (b.dataset.acao === "editar") abrir(reg);
      else if (await App.confirmar(`Excluir ${cfg.singular.toLowerCase()} "${reg[cfg.rotulo]}"?`)) {
        try { await Api.excluir(`${cfg.url}/${id}`); App.aviso(cfg.singular + " excluído(a)."); await carregar(); }
        catch (err) { App.erro(err); }
      }
    });
    carregar();
  }
  return { iniciar };
})();
