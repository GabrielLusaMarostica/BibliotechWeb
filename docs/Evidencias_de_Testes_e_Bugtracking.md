---
title: "Evidências de Testes e Bugtracking – Bibliotech Web"
subtitle: "Projeto Integrador – Etapa 9"
author: "Gabriel Marostica"
date: "Outubro de 2026"
---

**Curso / turma:** [preencher] · **Unidade curricular:** [preencher] · **Professor(a):** [preencher]
**Repositório:** [colar aqui o link do repositório no GitHub]

# 1. Resumo

O sistema web foi testado em três níveis: (1) **116 testes automatizados JUnit** (os 85 da etapa 7 mais 31 novos para a API REST e o acesso ao banco); (2) **27 casos executados no navegador contra um MySQL 8 real**, que cobrem os casos manuais CT-M01 a CT-M18 do plano de testes da etapa 7 e casos novos da versão web; (3) **testes exploratórios**, que encontraram 3 bugs. No total foram registrados e corrigidos **6 bugs** (seção 5). Ao final, a suíte automatizada tem 0 falhas e os 27 casos manuais estão com resultado OK.

# 2. Ambiente de teste

| Item | Detalhe |
|---|---|
| Java / banco | JDK 21; MySQL 8.0.46 (servidor real, não simulado) |
| Aplicação | API REST com Spring MVC + JDBC; front-end HTML/CSS/JS servido pela própria aplicação |
| Testes automatizados | JUnit 4.13.2, MockMvc do Spring, H2 em modo MySQL (com o mesmo `banco/schema.sql`) |
| Testes no navegador | Chromium automatizado (Playwright), 1280×800 e 390×800, contra a aplicação ligada ao MySQL |
| Bugtracking | registros no formato de *issues* do GitHub em `docs/bugtracking/` (seção 5) |

**Limitação deste ambiente de testes.** O ambiente em que os testes foram executados não conseguia baixar as bibliotecas do Spring Boot (repositório Maven bloqueado). Por isso o código foi compilado e testado com o Spring MVC 4.3 e o servidor Jetty disponíveis no ambiente, usando exatamente as mesmas classes do projeto; o `pom.xml` do projeto usa o Spring Boot 3.3, e o código só usa classes do Spring que existem nas duas versões. A única parte que não pôde ser executada ali é a inicialização do Spring Boot (`BibliotechApplication`) e o `mvn test`: ao abrir o projeto no NetBeans pela primeira vez, rode `mvn test` e anexe o print em "Evidências a anexar" (seção 8).

# 3. Testes automatizados (JUnit)

Resultado da execução completa: **OK (116 tests), 0 falhas, 0 erros.**

| Classe de teste | Testes | Origem | Foco |
|---|---|---|---|
| MultaPorDiaFixoTest | 9 | etapa 7 | cálculo da multa |
| CalculoDiasAtrasoTest | 7 | etapa 7 | dias de atraso |
| CodificadorSenhaPbkdf2Test | 6 | etapa 7 | hash de senha |
| EmprestimoServicoTest | 12 | etapa 7 | empréstimo e estoque |
| DevolucaoServicoTest | 7 | etapa 7 | devolução e multa |
| AutenticacaoServicoTest | 7 | etapa 7 | login |
| AlunoServicoTest | 10 | etapa 7 | cadastro de alunos |
| LivroTest | 9 | etapa 7 | regras do livro |
| EmprestimoTest | 9 | etapa 7 | regras do empréstimo |
| EntidadesValidacaoTest | 9 | etapa 7 | validação das entidades |
| ExclusaoComEmprestimoTest | 3 | **etapa 9** | exclusão de aluno/livro com empréstimo (BUG-001) |
| RepositoriosJdbcTest | 10 | **etapa 9** | SQL, transações e chaves estrangeiras (H2 modo MySQL), pedidos simultâneos |
| ApiRestTest | 18 | **etapa 9** | todas as rotas REST, códigos HTTP, sessão, erros |
| **Total** | **116** | | |

Na etapa 7, os testes de acesso a banco foram dispensados pela atividade. Agora que há banco na aplicação, `RepositoriosJdbcTest` protege o SQL e as transações.

# 4. Casos de teste manuais executados no navegador (MySQL real)

Os casos do plano de testes da etapa 7 (CT-M01 a CT-M18) foram executados na aplicação web integrada ao MySQL. Os casos CT-W são novos (próprios da versão web) e os CT-X são exploratórios. O log completo está em `docs/evidencias/testes/execucao-casos-manuais.log` e os dados brutos em `resultados.json`.

## 4.1 Casos do plano de testes (CT-M)

| Caso | Descrição | Resultado | O que foi observado |
|---|---|---|---|
| CT-M01 | Login válido | **OK** | Abriu o Painel após o login (URL: dashboard.html). |
| CT-M02 | Senha errada / login inexistente | **OK** | Mensagens: 'Login ou senha inválidos.' e 'Login ou senha inválidos.'. |
| CT-M03 | Senha oculta e acesso sem login | **OK** | Campo de senha com type=password; abrir alunos.html sem login voltou para index.html; GET /api/alunos sem token = HTTP 401. |
| CT-M04 | Aluno sem nome | **OK** | Mensagem no campo nome: 'Campo obrigatório.'; alunos gravados: 0. |
| CT-M05 | Matrícula duplicada | **OK** | Aviso: 'Já existe um aluno com a matrícula A100.'; alunos gravados: 1. |
| CT-M06 | Busca sem diferenciar maiúsculas | **OK** | Busca por 'SILVA' retornou 1 linha(s): Maria Silva	A100			 Editar Excluir. |
| CT-M07 | Excluir aluno sem e com empréstimo | **OK** | Aluno sem empréstimo removido=True; aluno com empréstimo: aviso 'Não é possível excluir o aluno: ele possui empréstimos registrados.', continua na lista=True; sistema responde=True. |
| CT-M08 | Cadastrar e listar funcionário | **OK** | Aparece na lista=True; linhas no MySQL=1. |
| CT-M09 | Livro com quantidade negativa / ano 0 | **OK** | Tela: 'Informe um número inteiro maior ou igual a zero.' e 'Informe um ano com 4 dígitos (não pode ser futuro).'. API direta: HTTP 400 e 400 ("O campo 'ano' deve ser maior que zero."). Livros gravados: 0. |
| CT-M10 | Empréstimo de 2 de 3 exemplares | **OK** | Aviso: 'Empréstimo registrado com sucesso.'; estoque no banco=1; tela de livros: 'Livro Tres A x 2000 1 un. Editar Excluir'. |
| CT-M11 | Quantidade maior que o estoque | **OK** | Tela: 'Estoque insuficiente: "Livro Tres" tem 3 disponível(is).'. API direta: HTTP 409 "Livro 'Livro Tres' indisponível: pedidos 5, em estoque 1.". Empréstimos antes/depois=2/2; estoque=1. |
| CT-M12 | Data prevista anterior a hoje | **OK** | Tela: 'A data prevista não pode ser anterior a hoje.'. API direta: HTTP 400 'A data prevista de devolução não pode ser anterior à data do empréstimo.'. |
| CT-M13 | Devolução com 3 dias de atraso | **OK** | Diálogo: 'Aluno: Aluno Atrasado  Dias de atraso: 3  Multa: R$\xa06,00'; aviso: 'Devolução registrada. Multa: R$\xa06,00.'; estoque 1→3; devolucao no MySQL: '3\t6.00'. |
| CT-M14 | Devolver duas vezes | **OK** | Segunda tentativa: HTTP 400 'Este empréstimo já foi devolvido.'; estoque continua 3. |
| CT-M15 | Lista de pendentes antes/depois | **OK** | Antes da devolução apareceu=True; depois sumiu=True. |
| CT-M16 | Senha do banco fora do código | **OK** | Sem a variável de ambiente o sistema recusa subir: 'PersistenciaException: Senha do banco não configurada. Defina a variável de ambiente BIBLIOTECH_DB_PASSWORD (ou -Dbibliotech.db.password).'. Ocorrências de senha no código-fonte: 0. |
| CT-M17 | Sistema rodando em MySQL real | **OK** | Servidor MySQL 8.0.46-0ubuntu0.24.04.4; linhas gravadas (alunos, livros, empréstimos, devoluções): 3, 3, 3, 1. |
| CT-M18 | Banco fora do ar durante empréstimo | **OK** | Mensagem na tela: 'Erro ao acessar o banco de dados.'; aplicação continuou respondendo=True; empréstimos 3→3; estoque 4→4 após religar o MySQL. |

## 4.2 Casos da versão web (CT-W)

| Caso | Descrição | Resultado | O que foi observado |
|---|---|---|---|
| CT-W01 | Painel mostra os mesmos números da API | **OK** | Tela: {'ind-livros': '8', 'ind-alunos': '3', 'ind-pendentes': '2', 'ind-atrasados': '0'}; API: {'exemplaresEmEstoque': 8, 'alunos': 3, 'pendentes': 2, 'atrasados': 0}. |
| CT-W02 | Sair encerra a sessão | **OK** | Depois de Sair, abrir dashboard.html voltou para o login. |
| CT-W03 | Layout em tela de celular (390 px) | **OK** | Sem rolagem horizontal da página=True. |
| CT-W04 | Erros de JavaScript no console | **OK** | Erros de página capturados: nenhum. |

## 4.3 Casos exploratórios (CT-X)

| Caso | Descrição | Resultado | O que foi observado |
|---|---|---|---|
| CT-X01 | Texto maior que a coluna do banco (BUG-004) | **OK** | Nome com 400 caracteres: HTTP 400 "O campo 'nome' deve ter no máximo 120 caracteres.". |
| CT-X02 | Id não numérico na URL (BUG-005) | **OK** | GET /api/alunos/abc: HTTP 400 "Valor inválido para o parâmetro 'id'.". |
| CT-X03 | 8 pedidos simultâneos pelo último exemplar (BUG-006) | **OK** | Códigos HTTP: [201, 409, 409, 409, 409, 409, 409, 409]; estoque final=0. |
| CT-X04 | Texto com HTML/JavaScript no nome (XSS) | **OK** | O texto aparece literalmente na tabela e o script não executou. |
| CT-X05 | Token inválido e ausente | **OK** | Token falso: HTTP 401; sem token: HTTP 401. |

## 4.4 Capturas de tela

**CT-M01 – Login válido**

![CT-M01](evidencias/testes/CT-M01.png){width=14cm}

**CT-M02 – Senha errada / login inexistente**

![CT-M02](evidencias/testes/CT-M02.png){width=14cm}

**CT-M03 – Senha oculta e acesso sem login**

![CT-M03](evidencias/testes/CT-M03.png){width=14cm}

**CT-M04 – Aluno sem nome**

![CT-M04](evidencias/testes/CT-M04.png){width=14cm}

**CT-M05 – Matrícula duplicada**

![CT-M05](evidencias/testes/CT-M05.png){width=14cm}

**CT-M06 – Busca sem diferenciar maiúsculas**

![CT-M06](evidencias/testes/CT-M06.png){width=14cm}

**CT-M07 – Excluir aluno sem e com empréstimo**

![CT-M07](evidencias/testes/CT-M07.png){width=14cm}

**CT-M08 – Cadastrar e listar funcionário**

![CT-M08](evidencias/testes/CT-M08.png){width=14cm}

**CT-M09 – Livro com quantidade negativa / ano 0**

![CT-M09](evidencias/testes/CT-M09.png){width=14cm}

**CT-M10 – Empréstimo de 2 de 3 exemplares**

![CT-M10](evidencias/testes/CT-M10.png){width=14cm}

**CT-M11 – Quantidade maior que o estoque**

![CT-M11](evidencias/testes/CT-M11.png){width=14cm}

**CT-M12 – Data prevista anterior a hoje**

![CT-M12](evidencias/testes/CT-M12.png){width=14cm}

**CT-M13 – Devolução com 3 dias de atraso**

![CT-M13](evidencias/testes/CT-M13.png){width=14cm}

**CT-M15 – Lista de pendentes antes/depois**

![CT-M15](evidencias/testes/CT-M15.png){width=14cm}

**CT-M18 – Banco fora do ar durante empréstimo**

![CT-M18](evidencias/testes/CT-M18.png){width=14cm}

**CT-W01 – Painel mostra os mesmos números da API**

![CT-W01](evidencias/testes/CT-W01.png){width=14cm}

**CT-W02 – Sair encerra a sessão**

![CT-W02](evidencias/testes/CT-W02.png){width=14cm}

**CT-W03 – Layout em tela de celular (390 px)**

![CT-W03](evidencias/testes/CT-W03.png){width=14cm}

**CT-X04 – Texto com HTML/JavaScript no nome (XSS)**

![CT-X04](evidencias/testes/CT-X04.png){width=14cm}


# 5. Bugtracking

Os bugs foram registrados em `docs/bugtracking/BUG-00n.md`, no formato de *issue*. A tabela resume cada um; os detalhes (passos, esperado × obtido, correção) estão nos arquivos.

| ID | Bug | Severidade | Como foi achado | Correção | Teste de regressão |
|---|---|---|---|---|---|
| BUG-001 | Excluir aluno/livro com empréstimo: erro genérico (MySQL) ou registro órfão (memória) | Alta | JUnit | regra nos serviços + tradução de erro de integridade | ExclusaoComEmprestimoTest, RepositoriosJdbcTest, CT-M07 |
| BUG-002 | Telas da etapa 8 com campos que o modelo não tem (ISBN, e-mail, turma) | Média | integração | formulários e validações ajustados ao modelo | CT-M04 a CT-M09 |
| BUG-003 | Rotas com `{id}` falham sem a opção `-parameters` | Baixa | JUnit | nomes explícitos em `@PathVariable` | ApiRestTest |
| BUG-004 | Texto maior que a coluna do banco vira HTTP 500 | Alta | exploratório | limites de tamanho no modelo e no front-end | ApiRestTest, CT-X01 |
| BUG-005 | Id não numérico na URL devolve HTML | Média | exploratório | tratamento de `TypeMismatchException` | ApiRestTest, CT-X02 |
| BUG-006 | Pedidos simultâneos pelo mesmo livro: deadlock e HTTP 500 | Alta | exploratório | baixa de estoque antes dos INSERTs + repetição em deadlock | CT-X03 (MySQL), RepositoriosJdbcTest |

**Antes e depois do BUG-006 (8 pedidos simultâneos pelo último exemplar, MySQL real):**

| | Códigos HTTP | Estoque final |
|---|---|---|
| Antes da correção | 201, 409, 500, 500, 500, 500, 500, 500 | 0 |
| Depois da correção (3 repetições) | 201, 409, 409, 409, 409, 409, 409, 409 | 0 |

**Antes e depois dos BUG-004 e BUG-005:**

| Requisição | Antes | Depois |
|---|---|---|
| `POST /api/alunos` com nome de 400 letras | HTTP 500 "Erro ao acessar o banco de dados." | HTTP 400 "O campo 'nome' deve ter no máximo 120 caracteres." |
| `GET /api/alunos/abc` | HTTP 400 com página HTML | HTTP 400 JSON "Valor inválido para o parâmetro 'id'." |

**Falhas do ambiente de teste (não são bugs do sistema).** Três falhas vieram dos scripts de teste ou do ambiente: um aviso lido cedo demais na tela, dois servidores MySQL ligados ao mesmo tempo (que fez o CT-M18 falhar até haver um só) e um teste de expiração de sessão que não fazia login de novo depois de avançar o relógio. Foram corrigidas nos scripts e estão listadas em `docs/bugtracking/README.md`.

# 6. Rastreabilidade: requisito × teste

| Requisito | Testes automatizados | Casos no navegador |
|---|---|---|
| RF01 Login | AutenticacaoServicoTest, ApiRestTest | CT-M01, CT-M02, CT-M03, CT-W02, CT-X05 |
| RF02 Alunos | AlunoServicoTest, ExclusaoComEmprestimoTest, ApiRestTest | CT-M04 a CT-M07, CT-X01, CT-X04 |
| RF03 Funcionários | EntidadesValidacaoTest, ApiRestTest | CT-M08 |
| RF04 Livros | LivroTest, ApiRestTest | CT-M09 |
| RF05 Empréstimo | EmprestimoServicoTest, RepositoriosJdbcTest | CT-M10, CT-M11, CT-M12, CT-X03 |
| RF06 Estoque | LivroTest, EmprestimoServicoTest | CT-M10, CT-M11, CT-M13 |
| RF07 Devolução | DevolucaoServicoTest, ApiRestTest | CT-M13, CT-M14 |
| RF08 Multa | MultaPorDiaFixoTest, CalculoDiasAtrasoTest | CT-M13 |
| RF09 Pendentes | EmprestimoServicoTest | CT-M15, CT-W01 |
| RNF01 Hash de senha | CodificadorSenhaPbkdf2Test | CT-M03 |
| RNF02 Credenciais fora do código | – | CT-M16 |
| RNF03 Integridade | RepositoriosJdbcTest | CT-M17, CT-M18, CT-X03 |
| RNF04 Mensagens claras | ApiRestTest | CT-M04, CT-M09, CT-M11 |

# 7. Conclusão

Todos os casos do plano de testes foram executados na versão web, contra o MySQL, e passaram depois das correções. O que os testes mais mostraram foi onde o sistema falhava sem aviso claro: exclusões que quebravam regras do banco, textos longos demais, ids inválidos e, principalmente, pedidos simultâneos. Fica de fora desta etapa: HTTPS, limite de tentativas de login e teste de carga.

# 8. Evidências a anexar (feitas por você)

1. Print da lista de *Issues* no GitHub com os 6 bugs (copie o texto de `docs/bugtracking/BUG-00n.md`).
2. Print do NetBeans com o resultado do `mvn test` (116 testes) ou da janela *Test Results*.
3. Print do sistema rodando em `http://localhost:8080` com o MySQL (variável `BIBLIOTECH_MODO=jdbc`).

[inserir capturas aqui]
