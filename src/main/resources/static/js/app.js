/* Funcoes comuns: menu lateral, avisos e utilitarios. */
const App = (() => {
  const PAGINAS = [
    ["dashboard.html", "Painel", "▦"],
    ["alunos.html", "Alunos", "☺"],
    ["livros.html", "Livros", "▤"],
    ["funcionarios.html", "Funcionários", "☰"],
    ["emprestimo.html", "Novo empréstimo", "＋"],
    ["pendentes.html", "Pendentes / devolução", "↺"]
  ];

  const esc = (t) => String(t == null ? "" : t).replace(/[&<>"']/g, (c) => ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" }[c]));
  const moeda = (n) => Number(n).toLocaleString("pt-BR", { style: "currency", currency: "BRL" });
  const dataBR = (iso) => iso.split("-").reverse().join("/");
  const hojeISO = () => { const d = new Date(); return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, "0")}-${String(d.getDate()).padStart(2, "0")}`; };
  const somarDiasISO = (dias) => { const d = new Date(); d.setDate(d.getDate() + dias); return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, "0")}-${String(d.getDate()).padStart(2, "0")}`; };

  function aviso(texto, tipo) {
    let area = document.querySelector(".avisos");
    if (!area) {
      area = document.createElement("div");
      area.className = "avisos"; area.setAttribute("role", "status"); area.setAttribute("aria-live", "polite");
      document.body.appendChild(area);
    }
    const el = document.createElement("div");
    el.className = "aviso" + (tipo === "erro" ? " erro" : "");
    el.textContent = texto;
    area.appendChild(el);
    setTimeout(() => el.remove(), 4500);
  }

  /** Mostra o erro de uma chamada da API como aviso vermelho. */
  function erro(e) { aviso(e && e.message ? e.message : "Erro inesperado.", "erro"); }

  async function sair() { await Api.sair(); location.href = "index.html"; }

  function montarMenu() {
    const alvo = document.getElementById("menu");
    if (!alvo) return;
    const atual = location.pathname.split("/").pop() || "index.html";
    alvo.className = "menu";
    alvo.innerHTML =
      `<a class="marca" href="dashboard.html"><span aria-hidden="true">▣</span><span>Biblio<em style="font-style:normal;color:var(--cor-destaque)">tech</em></span></a>
       <button class="botao secundario pequeno botao-menu" type="button" aria-expanded="false" aria-controls="menu-nav">Menu</button>
       <nav id="menu-nav" aria-label="Navegação principal">` +
      PAGINAS.map(([href, nome, icone]) =>
        `<a href="${href}"${href === atual ? ' aria-current="page"' : ""}><span aria-hidden="true">${icone}</span> ${nome}</a>`).join("") +
      `</nav>
       <div class="usuario">Logado como <strong>${esc(Api.usuario())}</strong><br>
       <button class="botao secundario pequeno" type="button" id="btn-sair">Sair</button></div>`;
    alvo.querySelector("#btn-sair").addEventListener("click", sair);
    const botao = alvo.querySelector(".botao-menu");
    botao.addEventListener("click", () => {
      const aberto = alvo.classList.toggle("aberto");
      botao.setAttribute("aria-expanded", String(aberto));
    });
  }

  function iniciar() {
    if (!Api.logado()) { location.replace("index.html"); return false; }
    montarMenu();
    return true;
  }

  /** Confirmacao usando <dialog> (sem alert/confirm do navegador). */
  function confirmar(mensagem) {
    return new Promise((resolve) => {
      const d = document.createElement("dialog");
      d.innerHTML = `<h2>Confirmar</h2><p>${esc(mensagem)}</p>
        <div class="acoes-form"><button class="botao secundario" value="n">Cancelar</button><button class="botao perigo" value="s">Confirmar</button></div>`;
      d.addEventListener("click", (e) => {
        const v = e.target.value; if (!v) return;
        d.close(); d.remove(); resolve(v === "s");
      });
      d.addEventListener("cancel", () => { d.remove(); resolve(false); });
      document.body.appendChild(d); d.showModal();
    });
  }

  return { iniciar, aviso, erro, esc, moeda, dataBR, hojeISO, somarDiasISO, confirmar };
})();
