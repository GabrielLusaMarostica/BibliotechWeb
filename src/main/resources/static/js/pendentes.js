if (App.iniciar()) {
  const corpo = document.getElementById("corpo-tabela");
  const busca = document.getElementById("busca");
  const soAtrasados = document.getElementById("so-atrasados");
  const dialogo = document.getElementById("dialogo-devolucao");
  let pendentes = [];
  let alvo = null;

  async function carregar() {
    try { pendentes = await Api.get("/api/emprestimos?situacao=pendentes"); desenhar(); }
    catch (e) { corpo.innerHTML = '<tr><td class="vazio" colspan="5">Não foi possível carregar os pendentes.</td></tr>'; App.erro(e); }
  }

  function desenhar() {
    const termo = busca.value.trim().toLowerCase();
    const lista = pendentes.filter((e) => (!termo || e.aluno.toLowerCase().includes(termo)) && (!soAtrasados.checked || e.diasAtraso > 0));
    document.getElementById("contador").textContent = `${lista.length} pendente(s)`;
    corpo.innerHTML = lista.length === 0
      ? '<tr><td class="vazio" colspan="5">Nenhum empréstimo pendente.</td></tr>'
      : lista.map((e) => {
          const livros = e.itens.map((i) => `${App.esc(i.titulo)} (×${i.quantidade})`).join(", ");
          const selo = e.diasAtraso > 0 ? `<span class="selo atraso">${e.diasAtraso} dia(s) de atraso</span>` : '<span class="selo ok">No prazo</span>';
          return `<tr><td>${App.esc(e.aluno)}</td><td>${livros}</td><td>${App.dataBR(e.dataPrevista)}</td><td>${selo}</td>
            <td><button class="botao pequeno" data-devolver="${e.id}">Devolver</button></td></tr>`;
        }).join("");
  }

  corpo.addEventListener("click", (ev) => {
    const b = ev.target.closest("[data-devolver]"); if (!b) return;
    alvo = pendentes.find((e) => e.id === Number(b.dataset.devolver));
    document.getElementById("detalhe-devolucao").innerHTML =
      `<p>Aluno: <strong>${App.esc(alvo.aluno)}</strong></p>
       <p>Dias de atraso: <strong>${alvo.diasAtraso}</strong></p>
       <p>Multa: <span class="resumo-valor">${App.moeda(alvo.multaPrevista)}</span></p>`;
    dialogo.showModal();
  });
  document.getElementById("cancelar-devolucao").addEventListener("click", () => dialogo.close());
  document.getElementById("confirmar-devolucao").addEventListener("click", async () => {
    const botao = document.getElementById("confirmar-devolucao");
    botao.disabled = true;
    try {
      const d = await Api.post(`/api/emprestimos/${alvo.id}/devolucao`);
      dialogo.close();
      App.aviso(d.multa > 0 ? `Devolução registrada. Multa: ${App.moeda(d.multa)}.` : "Devolução registrada. Estoque atualizado.");
    } catch (e) { dialogo.close(); App.erro(e); }
    finally { botao.disabled = false; }
    await carregar();
  });
  busca.addEventListener("input", desenhar);
  soAtrasados.addEventListener("change", desenhar);
  carregar();
}
