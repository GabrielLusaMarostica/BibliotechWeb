---
title: "Evidências de Versionamento – Bibliotech Web"
subtitle: "Projeto Integrador – Etapa 9"
author: "Gabriel Marostica"
date: "Outubro de 2026"
---

**Curso / turma:** [preencher] · **Unidade curricular:** [preencher] · **Professor(a):** [preencher]
**Repositório (novo):** [colar aqui o link do repositório no GitHub]

# 1. Resumo

O projeto Java Web (Spring Boot + JDBC + front-end) foi versionado com **Git** em um **repositório novo**, separado dos repositórios das etapas anteriores. O repositório tem **11 commits** na branch `main` (os 10 da tabela da seção 3 e o último, `docs: evidencias de versionamento`, que inclui este documento), com cerca de **150 arquivos** versionados e um único autor (Gabriel Marostica <gabriellmarostica@gmail.com>). Cada commit reúne uma parte coerente do trabalho (projeto, núcleo, banco, API, testes, front-end, documentação), com mensagens no padrão `tipo: descrição` (`chore`, `feat`, `test`, `docs`).

# 2. Como o repositório foi criado

```
git init -b main
git config user.name  "Gabriel Marostica"
git config user.email "gabriellmarostica@gmail.com"
git add <arquivos de cada parte>
git commit -m "tipo: descrição"
git remote add origin https://github.com/<usuario>/<repositorio>.git
git push -u origin main
```

# 3. Histórico de commits

| # | Commit | Data | Mensagem | Arquivos | + / − |
|---|---|---|---|---|---|
| 1 | `4c0d60f` | 24/10/2026 10:02 | chore: cria projeto Maven com Spring Boot 3 e Java 21 | 4 | +94 / −0 |
| 2 | `646295b` | 24/10/2026 10:02 | feat: traz o nucleo refatorado da etapa 6 (model, servico, repositorios JDBC e em memoria) | 53 | +2186 / −0 |
| 3 | `9957015` | 24/10/2026 10:02 | feat: script do banco MySQL 8 (schema.sql) | 1 | +68 / −0 |
| 4 | `589635b` | 24/10/2026 10:02 | feat(web): API REST com login por token, cadastros, emprestimo, devolucao e painel | 25 | +789 / −0 |
| 5 | `130f1ea` | 24/10/2026 10:02 | test: integra os 85 testes JUnit da etapa 7 ao projeto Maven | 11 | +871 / −0 |
| 6 | `41bf6aa` | 24/10/2026 10:02 | test: testes da API REST (MockMvc), do acesso JDBC e da exclusao com emprestimo | 3 | +575 / −0 |
| 7 | `7b79925` | 24/10/2026 10:02 | feat(web): front-end da etapa 8 integrado a API | 19 | +1023 / −0 |
| 8 | `9a1ca45` | 24/10/2026 10:02 | docs: README com sintese, execucao, API e estrutura | 1 | +84 / −0 |
| 9 | `bbc329f` | 24/10/2026 10:02 | docs: registro de bugs BUG-001 a BUG-006 (bugtracking) | 7 | +163 / −0 |
| 10 | `46bd7d7` | 24/10/2026 10:02 | docs: evidencias de testes (documento, capturas e log de execucao) | 23 | +423 / −0 |

A tabela é a saída de `git log --reverse` (do commit mais antigo ao mais recente) até o commit anterior a este documento. Os commits foram feitos em sequência no mesmo dia, por isso as datas são próximas. O arquivo `docs/evidencias/git_log.txt` do repositório traz a mesma lista em texto.

# 4. Organização do repositório

| Item | Detalhe |
|---|---|
| Branch | `main` |
| `.gitignore` | `target/`, `build/`, `dist/`, `nbproject/private/`, `.idea/`, `out/`, `*.iml`, `*.env`, `*.log`, `application-local.properties` |
| Segredos | nenhuma senha de banco no código: a senha vem da variável de ambiente `BIBLIOTECH_DB_PASSWORD` (conferido com `git grep`, sem ocorrências) |
| Ligação com as etapas anteriores | o núcleo (`model`, `servico`, `repositorio`) vem do projeto da etapa 6, os testes JUnit da etapa 7 e as páginas da etapa 8, todos agora no mesmo projeto Maven |
| Bugtracking | `docs/bugtracking/` com 6 bugs (BUG-001 a BUG-006), no formato de *issues* |

**Observação sobre os bugs e o histórico.** Os 6 bugs foram encontrados e corrigidos durante o desenvolvimento, antes de o histórico ser organizado por partes. Por isso as correções estão dentro dos commits de cada parte (por exemplo, o BUG-006 está no commit do núcleo) e não em commits `fix` separados. O arquivo de cada bug informa o teste que o protege.

# 5. Comandos para conferir

```
git log --oneline --graph
git log --stat -3
git shortlog -sne
git grep -n "senha" -- src/main
```

# 6. Evidências a anexar (feitas por você)

1. Print da página inicial do repositório no GitHub (lista de pastas e README).
2. Print da aba *Commits* com os 11 commits.
3. Print da aba *Issues* com os 6 bugs (se criar as issues).
4. Print do `git log --oneline` no terminal.

[inserir capturas aqui]
