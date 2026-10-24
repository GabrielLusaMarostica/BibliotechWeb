(() => {
  if (Api.logado()) { location.replace("dashboard.html"); return; }
  const form = document.getElementById("form-login");
  const R = Validacao.regras;
  const erroGeral = document.getElementById("erro-login");

  Validacao.ligar(form, { login: [R.obrigatorio], senha: [R.obrigatorio] }, async () => {
    const botao = form.querySelector("button[type=submit]");
    botao.disabled = true;
    try {
      const r = await Api.post("/api/login", { login: form.login.value.trim(), senha: form.senha.value });
      Api.entrar(r.login, r.token);
      location.href = "dashboard.html";
    } catch (e) {
      erroGeral.textContent = e.message;
      form.senha.value = "";
      form.senha.focus();
    } finally { botao.disabled = false; }
  });
  form.addEventListener("input", () => { erroGeral.textContent = ""; });

  const botao = document.getElementById("mostrar-senha");
  botao.addEventListener("click", () => {
    const visivel = form.senha.type === "text";
    form.senha.type = visivel ? "password" : "text";
    botao.textContent = visivel ? "Mostrar" : "Ocultar";
    botao.setAttribute("aria-pressed", String(!visivel));
  });
})();
