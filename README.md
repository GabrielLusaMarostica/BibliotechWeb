# Bibliotech Web

Sistema web de biblioteca escolar: **Spring Boot (API REST) + JDBC/MySQL + front-end HTML/CSS/JavaScript**.
É a versão final do Projeto Integrador: reúne o núcleo refatorado (etapa 6), os testes JUnit (etapa 7)
e as telas (etapa 8), agora ligados a um banco de dados real.

## Síntese do projeto

| Etapa | Entrega | Onde está |
|---|---|---|
| Problema | Sistema desktop (Swing + MySQL) com regras misturadas nas telas, senha em texto puro, multa errada e estoque que não era gravado | relatório da etapa 6 |
| 6 – Refatoração | Camadas `model`, `servico`, `repositorio` sem Swing; SOLID; Repository, Strategy, DI | `src/main/java/bibliotech/{model,servico,repositorio,config}` |
| 7 – Testes | JUnit 4 sobre regras de negócio (85 testes) e plano de testes com 18 casos manuais | `src/test/java` e plano de testes |
| 8 – Front-end | Wireframes e 7 páginas (HTML, CSS, JS) | `src/main/resources/static` |
| 9 – Back-end | API REST (Spring), repositórios JDBC em MySQL, sessão por token, front-end integrado, testes e bugtracking | `src/main/java/bibliotech/web`, `docs/` |

Resultado: cadastro de alunos, livros e funcionários; empréstimo de vários livros com baixa de estoque na mesma
transação; devolução com cálculo da multa (R$ 2,00 por dia de atraso); painel com indicadores; login com senha
protegida por hash (PBKDF2). **116 testes automatizados** e **27 casos executados no navegador contra MySQL 8**.

## Como executar

Requisitos: JDK 21 e Maven (o NetBeans já traz o Maven). Abra a pasta no NetBeans (*File → Open Project*)
e execute `BibliotechApplication`, ou pelo terminal: `mvn spring-boot:run`. Depois acesse <http://localhost:8080>.

### Modo demonstração (sem banco) – padrão
Sobe com dados de exemplo em memória. Login: **admin / admin123**. Nada é gravado em disco.

### Modo MySQL
1. Execute `banco/schema.sql` no MySQL 8 e crie um usuário com acesso ao banco `bibliotech`:
   ```sql
   CREATE USER 'bibliotech'@'localhost' IDENTIFIED BY 'escolha-uma-senha';
   GRANT ALL ON bibliotech.* TO 'bibliotech'@'localhost';
   ```
2. Defina as variáveis de ambiente (nenhuma senha fica no código) e suba a aplicação:

   | Variável | Para quê | Padrão |
   |---|---|---|
   | `BIBLIOTECH_MODO` | `jdbc` liga o MySQL | `memoria` |
   | `BIBLIOTECH_DB_URL` | URL JDBC | `jdbc:mysql://localhost:3306/bibliotech?useSSL=false&allowPublicKeyRetrieval=true&characterEncoding=UTF-8` |
   | `BIBLIOTECH_DB_USER` | usuário do banco | `bibliotech` |
   | `BIBLIOTECH_DB_PASSWORD` | senha do banco | *(obrigatória)* |
   | `BIBLIOTECH_ADMIN_LOGIN` / `BIBLIOTECH_ADMIN_SENHA` | cria o primeiro usuário do sistema | `admin` / *(obrigatória)* |

   PowerShell: `$env:BIBLIOTECH_MODO="jdbc"; $env:BIBLIOTECH_DB_PASSWORD="..."; $env:BIBLIOTECH_ADMIN_SENHA="..."; mvn spring-boot:run`

No NetBeans, as variáveis podem ser colocadas em *Project Properties → Actions → Run project → Set Properties* ou no próprio sistema operacional.

### Testes
`mvn test` roda os 116 testes (JUnit 4). Não precisam de MySQL: os testes de JDBC usam o H2 em modo MySQL com o mesmo `schema.sql`.

## API REST

Tudo em JSON. Depois do login, envie `Authorization: Bearer <token>` (a sessão dura 8 horas).

| Método e caminho | Função |
|---|---|
| `POST /api/login`, `POST /api/logout`, `GET /api/sessao` | sessão |
| `GET/POST /api/alunos`, `GET/PUT/DELETE /api/alunos/{id}` | alunos (`?busca=` filtra) |
| `GET/POST /api/livros`, `GET/PUT/DELETE /api/livros/{id}` | livros e estoque |
| `GET/POST /api/funcionarios`, `GET/PUT /api/funcionarios/{id}` | funcionários |
| `GET/POST /api/emprestimos`, `GET /api/emprestimos/{id}` | empréstimos (`?pendentes=true`) |
| `POST /api/emprestimos/{id}/devolucao` | devolução e multa |
| `GET /api/painel` | indicadores |

Erros voltam como `{"status": 400, "mensagem": "..."}`: 400 dado inválido, 401 sem login, 404 não encontrado,
409 estoque insuficiente, 500 falha de banco (sem expor SQL).

## Estrutura

```
src/main/java/bibliotech/
├── model/ servico/ repositorio/ config/ excecao/ util/   núcleo da etapa 6
└── web/                                                   controllers REST, DTOs, sessão, tratamento de erros
src/main/resources/static/                                 front-end (páginas, css/, js/)
src/test/java/                                             testes JUnit (etapas 7 e 9)
banco/schema.sql                                           script do MySQL
docs/                                                      evidências de testes, bugtracking e versionamento
```

## Observações
- A sessão fica em memória do servidor: reiniciar o servidor exige novo login.
- Segurança: senha com hash, SQL sempre parametrizado, textos escapados no front-end, token aleatório. Para uso real
  em produção falta HTTPS e proteção contra tentativas repetidas de login.
