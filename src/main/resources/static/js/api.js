/* Cliente da API REST do Bibliotech (substitui o dados.js simulado da etapa 8). */
class ErroApi extends Error {
  constructor(status, mensagem) { super(mensagem); this.status = status; }
}

const Api = (() => {
  const CHAVE_TOKEN = "bibliotech.token";
  const CHAVE_LOGIN = "bibliotech.login";

  const guardar = (k, v) => { try { sessionStorage.setItem(k, v); } catch (e) { /* ignora */ } };
  const ler = (k) => { try { return sessionStorage.getItem(k); } catch (e) { return null; } };
  const apagar = () => { try { sessionStorage.removeItem(CHAVE_TOKEN); sessionStorage.removeItem(CHAVE_LOGIN); } catch (e) { /* ignora */ } };

  async function chamar(metodo, url, corpo) {
    const cabecalhos = { "Accept": "application/json" };
    if (corpo !== undefined) cabecalhos["Content-Type"] = "application/json";
    const token = ler(CHAVE_TOKEN);
    if (token) cabecalhos["Authorization"] = "Bearer " + token;

    let resposta;
    try {
      resposta = await fetch(url, { method: metodo, headers: cabecalhos, body: corpo === undefined ? undefined : JSON.stringify(corpo) });
    } catch (e) {
      throw new ErroApi(0, "Não foi possível falar com o servidor. Verifique se ele está em execução.");
    }
    const texto = await resposta.text();
    let dados = null;
    try { dados = texto ? JSON.parse(texto) : null; } catch (e) { dados = null; }

    if (resposta.status === 401 && url !== "/api/login") {
      apagar();
      location.replace("index.html");
      throw new ErroApi(401, "Sessão expirada. Faça login novamente.");
    }
    if (!resposta.ok) {
      throw new ErroApi(resposta.status, (dados && dados.mensagem) || "Erro inesperado (" + resposta.status + ").");
    }
    return dados;
  }

  return {
    get: (url) => chamar("GET", url),
    post: (url, corpo) => chamar("POST", url, corpo === undefined ? undefined : corpo),
    put: (url, corpo) => chamar("PUT", url, corpo),
    excluir: (url) => chamar("DELETE", url),
    entrar(login, token) { guardar(CHAVE_TOKEN, token); guardar(CHAVE_LOGIN, login); },
    sair: async () => { try { await chamar("POST", "/api/logout"); } catch (e) { /* já saiu */ } apagar(); },
    logado: () => ler(CHAVE_TOKEN) !== null,
    usuario: () => ler(CHAVE_LOGIN)
  };
})();
