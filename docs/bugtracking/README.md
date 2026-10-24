# Registro de bugs (bugtracking)

Os bugs abaixo foram encontrados durante a etapa 9 e corrigidos. Cada arquivo está no formato de uma *issue* do GitHub (título, rótulos, passos, esperado × obtido, correção e teste). Para registrar no GitHub: *Issues → New issue*, colar o conteúdo, rótulo `bug`, e fechar com o commit de correção (`Fixes #n`).

| ID | Título | Severidade | Origem | Status |
|---|---|---|---|---|
| BUG-001 | Excluir aluno ou livro que já tem empréstimo | Alta | Teste automatizado (JUnit) | Corrigido |
| BUG-002 | Front-end da etapa 8 pedia campos que o modelo não tem | Média | Integração front-end × back-end | Corrigido |
| BUG-003 | Erro 500 em todas as rotas com `{id}` fora do Maven | Baixa | Teste automatizado (JUnit) | Corrigido |
| BUG-004 | Texto maior que a coluna do banco vira erro 500 | Alta | Teste exploratório | Corrigido |
| BUG-005 | Id não numérico na URL devolve página HTML | Média | Teste exploratório | Corrigido |
| BUG-006 | Pedidos simultâneos pelo mesmo livro causam deadlock (500) | Alta | Teste exploratório (concorrência) | Corrigido |

## Falhas do ambiente de teste (não são bugs do sistema)

Estas falhas apareceram durante a execução dos testes e foram corrigidas nos próprios scripts de teste:

- Um teste lia o aviso na tela antes de o aviso certo aparecer (corrida entre dois avisos). O script passou a esperar o aviso de erro.
- O caso CT-M18 (banco fora do ar) falhou duas vezes porque havia dois servidores MySQL ativos ao mesmo tempo, então o desligamento não derrubava o banco. Com um único servidor, o caso passou.
- O teste de expiração de sessão avançava 10 dias no relógio sem fazer login de novo (a sessão dura 8 horas).
