/* Validacoes reutilizaveis dos formularios (espelham as regras do model Java). */
const Validacao = (() => {
  const regras = {
    obrigatorio: (v) => v.trim() !== "" || "Campo obrigatório.",
    maximo: (n) => (v) => v.trim().length <= n || `Use no máximo ${n} caracteres.`,
    minimo: (n) => (v) => v.trim().length >= n || `Informe pelo menos ${n} caracteres.`,
    email: (v) => v === "" || /^[^\s@]+@[^\s@]+\.[^\s@]{2,}$/.test(v) || "E-mail inválido.",
    matricula: (v) => /^[A-Za-z0-9]{2,20}$/.test(v) || "A matrícula deve ter de 2 a 20 letras ou números, sem espaços.",
    ano: (v) => (/^\d{4}$/.test(v) && Number(v) >= 1000 && Number(v) <= new Date().getFullYear()) || "Informe um ano com 4 dígitos (não pode ser futuro).",
    inteiroNaoNegativo: (v) => (/^\d+$/.test(v) && Number(v) >= 0) || "Informe um número inteiro maior ou igual a zero.",
    inteiroPositivo: (v) => (/^\d+$/.test(v) && Number(v) > 0) || "Informe um número inteiro maior que zero.",
    dataFutura: (v) => {
      if (!v) return "Informe a data.";
      const d = new Date(); const hoje = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, "0")}-${String(d.getDate()).padStart(2, "0")}`;
      return v >= hoje || "A data prevista não pode ser anterior a hoje.";
    }
  };

  /** Valida um campo e mostra/remove a mensagem de erro. Retorna true se valido. */
  function validarCampo(input, lista) {
    const caixa = input.closest(".campo");
    const alvoErro = caixa.querySelector(".erro");
    let mensagem = "";
    for (const regra of lista) {
      const r = regra(input.value);
      if (r !== true) { mensagem = r; break; }
    }
    caixa.classList.toggle("invalido", mensagem !== "");
    caixa.classList.toggle("valido", mensagem === "" && input.value !== "");
    input.setAttribute("aria-invalid", mensagem !== "" ? "true" : "false");
    if (alvoErro) alvoErro.textContent = mensagem;
    return mensagem === "";
  }

  /** esquema: { idDoCampo: [regras...] }. Valida em tempo real e no envio. */
  function ligar(form, esquema, aoEnviar) {
    form.noValidate = true;
    const campos = Object.keys(esquema).map((id) => form.querySelector("#" + id));
    campos.forEach((input) => {
      input.addEventListener("blur", () => validarCampo(input, esquema[input.id]));
      input.addEventListener("input", () => {
        if (input.closest(".campo").classList.contains("invalido")) validarCampo(input, esquema[input.id]);
      });
    });
    form.addEventListener("submit", (e) => {
      e.preventDefault();
      const resultados = campos.map((input) => validarCampo(input, esquema[input.id]));
      const primeiroInvalido = campos.find((_, i) => !resultados[i]);
      if (primeiroInvalido) { primeiroInvalido.focus(); return; }
      aoEnviar();
    });
  }

  function limpar(form) {
    form.reset();
    form.querySelectorAll(".campo").forEach((c) => {
      c.classList.remove("invalido", "valido");
      const e = c.querySelector(".erro"); if (e) e.textContent = "";
    });
  }

  return { regras, validarCampo, ligar, limpar };
})();
