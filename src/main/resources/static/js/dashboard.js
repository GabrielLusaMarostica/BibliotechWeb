if (App.iniciar()) {
  (async () => {
    try {
      const p = await Api.get("/api/painel");
      document.getElementById("ind-livros").textContent = p.exemplaresEmEstoque;
      document.getElementById("ind-alunos").textContent = p.alunos;
      document.getElementById("ind-pendentes").textContent = p.pendentes;
      document.getElementById("ind-atrasados").textContent = p.atrasados;

      document.getElementById("urgentes").innerHTML = p.urgentes.length === 0
        ? '<tr><td class="vazio" colspan="3">Nenhum empréstimo pendente.</td></tr>'
        : p.urgentes.map((e) => {
            const selo = e.diasAtraso > 0 ? `<span class="selo atraso">${e.diasAtraso} dia(s) de atraso</span>` : '<span class="selo ok">No prazo</span>';
            return `<tr><td>${App.esc(e.aluno)}</td><td>${App.dataBR(e.dataPrevista)}</td><td>${selo}</td></tr>`;
          }).join("");

      document.getElementById("estoque-baixo").innerHTML = p.estoqueBaixo.length === 0
        ? "<li>Todos os livros com estoque confortável.</li>"
        : p.estoqueBaixo.map((l) => `<li><strong>${App.esc(l.titulo)}</strong> – ${l.quantidade === 0 ? "indisponível" : l.quantidade + " exemplar"}</li>`).join("");
    } catch (e) { App.erro(e); }
  })();
}
