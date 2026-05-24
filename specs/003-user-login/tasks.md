# Tasks: User Login

**Input**: Artefatos de design de `specs/003-user-login/`

**Pré-requisitos**: plan.md ✅ | spec.md ✅ | research.md ✅ | data-model.md ✅ | contracts/auth-api.md ✅ | quickstart.md ✅

**TDD Obrigatório**: Conforme Princípio II da Constituição (NÃO NEGOCIÁVEL) — escrever o teste antes da implementação; verificar que o teste FALHA antes de implementar.

**Organização**: Tarefas agrupadas por User Story para implementação e teste independentes de cada história.

## Formato: `[ID] [P?] [Story] Descrição`

- **[P]**: Pode rodar em paralelo (arquivos diferentes, sem dependências)
- **[Story]**: A qual User Story esta tarefa pertence (US1, US2, US3, US4)
- Incluir caminho exato do arquivo nas descrições

---

## Fase 1: Configuração (Infraestrutura)

**Objetivo**: Dependências, propriedades e schema de banco — infraestrutura necessária para todas as fases

- [X] T001 Adicionar jjwt-api, jjwt-impl (scope runtime), jjwt-jackson (scope runtime) versão 0.12.6 ao pom.xml
- [X] T002 [P] Adicionar jwt-secret, token-ttl-horas, bloqueio-tentativas, bloqueio-duracao-minutos sob app.seguranca em src/main/resources/application.yml
- [X] T003 Criar migration de banco de dados: adicionar colunas ativo (boolean default true), tentativas_falhas (int default 0), bloqueado_ate (timestamp nullable) à tabela usuarios; criar tabelas tokens_revogados (jti UUID PK, expira_em timestamp) e log_login (id UUID PK, usuario_id UUID nullable FK, ip varchar(45), resultado varchar(10), criado_em timestamp) em src/main/resources/db/migration/

---

## Fase 2: Base (Pré-requisitos Bloqueantes)

**Objetivo**: Entidades JPA, repositórios e JwtService — todas as User Stories dependem destes componentes

**⚠️ CRÍTICO**: Nenhuma User Story pode ser iniciada antes desta fase estar completa

- [X] T004 [P] Modificar src/main/java/br/edu/shandragon/pokedex/usuario/entity/Usuario.java: adicionar campos @Column ativo (boolean, padrão true), tentativasFalhas (int, padrão 0), bloqueadoAte (Instant, nullable)
- [X] T005 [P] Criar src/main/java/br/edu/shandragon/pokedex/auth/entity/TokenRevogado.java: @Entity tabela tokens_revogados; jti (UUID, @Id), expiraEm (Instant, não nulo)
- [X] T006 [P] Criar src/main/java/br/edu/shandragon/pokedex/auth/entity/LogLogin.java: @Entity tabela log_login; id (UUID, @Id, UUID v7), usuarioId (UUID, nullable), ip (String, máx 45), resultado (String, máx 10), criadoEm (Instant)
- [X] T007 [P] Criar src/main/java/br/edu/shandragon/pokedex/auth/repository/jpa/TokenRevogadoRepository.java: extends JpaRepository<TokenRevogado, UUID>; método boolean existsByJti(UUID jti)
- [X] T008 [P] Criar src/main/java/br/edu/shandragon/pokedex/auth/repository/jpa/LogLoginRepository.java: extends JpaRepository<LogLogin, UUID>
- [X] T009 [P] Escrever src/test/java/br/edu/shandragon/pokedex/auth/service/JwtServiceTest.java: @ExtendWith(MockitoExtension) — testar gerar() (claims sub/iat/exp/jti), validar() (token válido, expirado, assinatura inválida, jti em denylist), extrairJti(), extrairSub(), extrairExp(); verificar que testes FALHAM antes de implementar
- [X] T010 [P] Adicionar testes dos campos ativo, tentativasFalhas, bloqueadoAte em src/test/java/br/edu/shandragon/pokedex/usuario/repository/UsuarioRepositoryIntegrationTest.java (@DataJpaTest — salvar e recuperar usuario com novos campos)
- [X] T011 [P] Escrever src/test/java/br/edu/shandragon/pokedex/auth/repository/TokenRevogadoRepositoryIntegrationTest.java: @DataJpaTest — testar existsByJti retorna true após insert e false para jti inexistente; verificar que testes FALHAM antes de implementar
- [X] T012 Implementar src/main/java/br/edu/shandragon/pokedex/auth/service/JwtService.java: gerar(UUID usuarioId), validar(String token), extrairJti(String token), extrairSub(String token), extrairExp(String token) usando jjwt 0.12.x; @Value("${app.seguranca.jwt-secret}"), @Value("${app.seguranca.token-ttl-horas}"); injetar TokenRevogadoRepository para checar denylist em validar(); satisfazer T009 e T011
- [X] T013 [P] Escrever src/test/java/br/edu/shandragon/pokedex/auth/controller/AuthControllerTest.java: criar arquivo base @WebMvcTest(AuthController); adicionar cenários de configuração de segurança: POST /api/auth/login retorna status diferente de 401 sem header Authorization; POST /api/auth/logout retorna 401 sem Bearer; testes devem FALHAR antes de T014
- [X] T014 [P] Modificar src/main/java/br/edu/shandragon/pokedex/config/SegurancaConfig.java: adicionar .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll() antes de anyRequest().authenticated(); satisfaz T013

**Marco**: Base pronta — JwtService funcional, entidades criadas, repositórios disponíveis, configuração de segurança testada; fases de User Story podem iniciar

---

## Fase 3: User Story 1 — Login com Credenciais Válidas (Priority: P1) 🎯 MVP

**Objetivo**: Usuário cadastrado faz login com e-mail e senha corretos e recebe JWT; requisições autenticadas têm o token renovado via X-Token-Renovado.

**Teste Independente**: `POST /api/auth/login` com credenciais válidas retorna 200 com token e expiraEm; requisição autenticada subsequente retorna header X-Token-Renovado com novo JWT.

- [X] T015 [P] [US1] Criar src/main/java/br/edu/shandragon/pokedex/auth/controller/AuthController.java: esqueleto mínimo com @RestController @RequestMapping("/api/auth") sem métodos; necessário para compilação de AuthControllerTest.java (T013) e execução dos cenários de segurança antes da implementação completa
- [X] T016 [P] [US1] Criar src/main/java/br/edu/shandragon/pokedex/auth/dto/LoginRequisicaoDTO.java: record ou classe com email (@NotBlank @Email String), senha (@NotBlank String)
- [X] T017 [P] [US1] Criar src/main/java/br/edu/shandragon/pokedex/auth/dto/LoginRespostaDTO.java: record ou classe com token (String), expiraEm (Instant)
- [X] T018 [US1] Escrever cenários US1 em src/test/java/br/edu/shandragon/pokedex/auth/service/AuthServiceTest.java: @ExtendWith(MockitoExtension) — login com credenciais válidas retorna LoginRespostaDTO com token JWT não nulo e expiraEm; verificar que LogLogin com resultado SUCESSO é salvo via LogLoginRepository; testes devem FALHAR antes de implementar
- [X] T019 [US1] Adicionar cenários US1 em src/test/java/br/edu/shandragon/pokedex/auth/controller/AuthControllerTest.java: POST /api/auth/login com body {"email":"joao@example.com","senha":"minhasenha123"} retorna 200 com campos token e expiraEm; testes devem FALHAR antes de implementar
- [X] T020 [US1] Implementar src/main/java/br/edu/shandragon/pokedex/auth/service/AuthService.java: método login(LoginRequisicaoDTO dto, String ip) — normalizar email para minúsculas, buscar usuario por email (não encontrado → 401), verificar ativo (falso → 401), verificar bloqueadoAte > now (→ 401), verificar senha BCrypt, gerar JWT via jwtService.gerar(), salvar LogLogin SUCESSO, retornar LoginRespostaDTO; satisfazer T018
- [X] T021 [US1] Implementar endpoints em src/main/java/br/edu/shandragon/pokedex/auth/controller/AuthController.java: substituir esqueleto — @Valid @RequestBody LoginRequisicaoDTO, extrair IP via HttpServletRequest.getRemoteAddr(), chamar authService.login(), retornar ResponseEntity.ok(LoginRespostaDTO); satisfazer T019
- [X] T022 [US1] Modificar src/main/java/br/edu/shandragon/pokedex/config/FiltroBearerToken.java: (1) verificar token admin primeiro — comportamento atual preservado; (2) caso contrário chamar jwtService.validar(token); (3) se válido: extrair sub via jwtService.extrairSub(), autenticar principal com userId, armazenar token original via request.setAttribute("jwtAtual", token), gerar novo JWT via jwtService.gerar(UUID.fromString(sub)), adicionar response.setHeader("X-Token-Renovado", novoJwt)

**Marco**: US1 completa — login funcionando, JWT emitido, X-Token-Renovado retornado em requisições autenticadas

---

## Fase 4: User Story 2 — Login com Credenciais Inválidas (Priority: P1)

**Objetivo**: Sistema nega acesso com mensagem genérica "E-mail ou senha inválidos" para qualquer falha, sem revelar qual dado está incorreto; campos obrigatórios validados.

**Teste Independente**: `POST /api/auth/login` com senha errada, e-mail inexistente ou conta inativa retorna 401 com mensagem genérica; campos em branco retornam 400.

- [X] T023 [US2] Adicionar cenários US2 em src/test/java/br/edu/shandragon/pokedex/auth/service/AuthServiceTest.java: senha incorreta → lança exceção 401 genérica; e-mail inexistente → lança exceção 401 genérica; conta inativa (ativo=false) → lança exceção 401 genérica; LogLogin FALHA é salvo em cada cenário; testes devem FALHAR antes de ajuste
- [X] T024 [US2] Adicionar cenários US2 em src/test/java/br/edu/shandragon/pokedex/auth/controller/AuthControllerTest.java: campos obrigatórios ausentes retorna 400 com "Dados de requisição inválidos"; credenciais incorretas retorna 401 com "E-mail ou senha inválidos"; testes devem FALHAR antes de ajuste
- [X] T025 [US2] Implementar tratamento de credenciais inválidas em src/main/java/br/edu/shandragon/pokedex/auth/service/AuthService.java: e-mail não encontrado → lançar 401 com mensagem genérica; ativo=false → lançar 401 com mensagem genérica; senha incorreta → lançar 401 com mensagem genérica; salvar LogLogin FALHA (usuarioId null se e-mail inexistente) em todos os casos; satisfazer T023 e T024

**Marco**: US2 completa — todas as falhas de autenticação retornam 401 genérico e são registradas em log_login

---

## Fase 5: User Story 3 — Bloqueio por Tentativas Excessivas (Priority: P2)

**Objetivo**: Após 5 falhas consecutivas para o mesmo e-mail, a conta é bloqueada por 15 minutos; após expiração do bloqueio o login volta ao normal.

**Teste Independente**: 5 chamadas inválidas consecutivas incrementam tentativasFalhas e na 5ª definem bloqueadoAte; chamada dentro do período de bloqueio retorna 401; login bem-sucedido zera tentativasFalhas e bloqueadoAte.

- [X] T026 [US3] Adicionar cenários US3 em src/test/java/br/edu/shandragon/pokedex/auth/service/AuthServiceTest.java: tentativasFalhas incrementa a cada falha; na 5ª falha bloqueadoAte é definido como now + 15min; durante bloqueio qualquer tentativa retorna 401 genérico sem validar senha; login bem-sucedido zera tentativasFalhas = 0 e bloqueadoAte = null; testes devem FALHAR antes de ajuste
- [X] T027 [US3] Implementar proteção contra força bruta em src/main/java/br/edu/shandragon/pokedex/auth/service/AuthService.java: verificar bloqueadoAte != null && bloqueadoAte.isAfter(Instant.now()) → lançar 401 genérico; em falha de senha → tentativasFalhas++; se tentativasFalhas >= @Value bloqueio-tentativas → bloqueadoAte = Instant.now().plus(bloqueio-duracao-minutos, MINUTES); salvar usuario; em sucesso → tentativasFalhas = 0, bloqueadoAte = null, salvar usuario; satisfazer T026

**Marco**: US3 completa — proteção contra força bruta ativa, campos tentativasFalhas/bloqueadoAte gerenciados corretamente

---

## Fase 6: User Story 4 — Encerramento de Sessão (Priority: P2)

**Objetivo**: Usuário autenticado revoga o token atual via POST /api/auth/logout; token revogado é rejeitado em requisições subsequentes mesmo dentro do prazo de expiração original.

**Teste Independente**: `POST /api/auth/logout` com JWT válido retorna 204 e insere jti em tokens_revogados; requisição subsequente com o mesmo JWT retorna 401; logout de token A não afeta token B do mesmo usuário.

- [X] T028 [P] [US4] Adicionar cenários US4 em src/test/java/br/edu/shandragon/pokedex/auth/service/AuthServiceTest.java: logout(token) extrai jti e expiraEm, cria TokenRevogado e salva via TokenRevogadoRepository; **cenário multi-dispositivo**: gerar dois tokens distintos (jti A e jti B) para o mesmo usuário, chamar logout(tokenA), verificar que existsByJti(jtiA) retorna true e existsByJti(jtiB) retorna false; testes devem FALHAR antes de ajuste
- [X] T029 [P] [US4] Adicionar cenários US4 em src/test/java/br/edu/shandragon/pokedex/auth/controller/AuthControllerTest.java: POST /api/auth/logout com Bearer JWT válido retorna 204 No Content; POST /api/auth/logout sem header Authorization retorna 401; testes devem FALHAR antes de ajuste
- [X] T030 [US4] Implementar método logout(String token) em src/main/java/br/edu/shandragon/pokedex/auth/service/AuthService.java: extrair jti via jwtService.extrairJti(token), extrair expiraEm via jwtService.extrairExp(token), criar TokenRevogado(UUID.fromString(jti), expiraEm), salvar via tokenRevogadoRepository.save(); satisfazer T028
- [X] T031 [US4] Adicionar POST /api/auth/logout em src/main/java/br/edu/shandragon/pokedex/auth/controller/AuthController.java: @PostMapping("/logout"), requer autenticação Bearer JWT válido já processado por FiltroBearerToken; extrair token via (String) request.getAttribute("jwtAtual") (atributo definido em T022); chamar authService.logout(token); retornar ResponseEntity.noContent().build(); satisfazer T029

**Marco**: US4 completa — logout funcional, token revogado rejeitado, logout seletivo por dispositivo verificado; todas as 4 User Stories implementadas

---

## Fase 7: Refinamento e Preocupações Transversais

**Objetivo**: Validação final, retrocompatibilidade com token admin e qualidade de cobertura

- [X] T032 [P] Validar retrocompatibilidade do token admin em src/main/java/br/edu/shandragon/pokedex/config/FiltroBearerToken.java: garantir que o token admin estático (app.seguranca.token-admin) continua autenticando sem passar pelo JwtService; nenhum endpoint existente afetado
- [X] T033 [P] Executar validação completa do quickstart.md: sequência login → requisição autenticada (verificar header X-Token-Renovado) → logout (verificar 204) → requisição com token revogado (verificar 401); medir tempo de resposta do login para referência ao SC-001
- [X] T034 Verificar cobertura de testes: confirmar que JwtServiceTest, AuthServiceTest, AuthControllerTest, TokenRevogadoRepositoryIntegrationTest e UsuarioRepositoryIntegrationTest cobrem FR-001 a FR-012 e SC-001 a SC-006

---

## Dependências e Ordem de Execução

### Dependências por Fase

- **Fase 1 (Configuração)**: Sem dependências — pode iniciar imediatamente; T002 paralelo a T001 e T003
- **Fase 2 (Base)**: Requer Fase 1 (T001 para classpath jjwt, T003 para schema de banco)
  - T004–T011: todos paralelos dentro da Fase 2 (arquivos diferentes, sem dependências cruzadas)
  - T012 (implementação JwtService): requer T007 (TokenRevogadoRepository) + T009 (JwtServiceTest) concluídos
  - T013 (AuthControllerTest base + segurança): paralelo a T012
  - T014 (SegurancaConfig): requer T013 (teste de segurança escrito e falhando)
- **Fase 3 (US1)**: Requer Fase 2 completa
  - T015 (stub AuthController), T016 (LoginRequisicaoDTO), T017 (LoginRespostaDTO): paralelos entre si
  - T018 (AuthServiceTest US1): requer T016, T017
  - T019 (AuthControllerTest US1): requer T015, T016, T017 (T013 já criou o arquivo)
  - T020 (AuthService impl): requer T018 (teste escrito antes)
  - T021 (AuthController impl): requer T019 (teste escrito antes) + T016, T017
  - T022 (FiltroBearerToken): requer T012 (JwtService implementado)
- **Fase 4 (US2)**: Requer Fase 3 (AuthService e AuthController já existem)
- **Fase 5 (US3)**: Requer Fase 4 (tratamento de credenciais inválidas já em AuthService)
- **Fase 6 (US4)**: Requer Fase 2 (TokenRevogadoRepository) + Fase 3 (FiltroBearerToken com request.setAttribute em T022)
  - T028–T029: paralelos (alvos de teste diferentes)
  - T030: requer T028
  - T031: requer T029 + T030
- **Fase 7 (Refinamento)**: Requer todas as fases anteriores; T032–T033 paralelos

### Dependências por User Story

- **US1 (P1)**: Inicia após Base — sem dependência de outras histórias
- **US2 (P1)**: Estende componentes de US1 (AuthService/AuthController já existem)
- **US3 (P2)**: Estende US2 (adiciona lógica de força bruta sobre tratamento de credenciais inválidas)
- **US4 (P2)**: Paralela a US3 — usa repositórios da Fase 2 e FiltroBearerToken da Fase 3 (T022)

### Dentro de Cada Story (Ciclo TDD)

1. **Escrever testes** (verificar que FALHAM antes de implementar)
2. **Implementar** apenas o necessário para os testes passarem
3. **Refatorar** se necessário, mantendo testes verdes
4. **Confirmar cobertura** no marco antes de avançar

### Oportunidades de Paralelismo

- T001–T003 (Fase 1): T002 paralelo aos demais
- T004–T013 (Fase 2): todos paralelos — arquivos diferentes, sem dependências cruzadas
- T015–T017 (início Fase 3): paralelos — stub e DTOs em arquivos distintos
- T028–T029 (testes Fase 6): paralelos — métodos/classes distintos
- T032–T033 (Fase 7): paralelos

---

## Exemplo de Paralelismo: Fase 2 (Base)

```bash
# Executar criação de entidades + repositórios + testes em paralelo (T004–T013):
Tarefa T004: Modificar Usuario.java (adicionar ativo, tentativasFalhas, bloqueadoAte)
Tarefa T005: Criar TokenRevogado.java
Tarefa T006: Criar LogLogin.java
Tarefa T007: Criar TokenRevogadoRepository.java (existsByJti)
Tarefa T008: Criar LogLoginRepository.java
Tarefa T009: Escrever JwtServiceTest.java (TDD — deve FALHAR primeiro)
Tarefa T010: Adicionar testes de novos campos em UsuarioRepositoryIntegrationTest.java
Tarefa T011: Escrever TokenRevogadoRepositoryIntegrationTest.java (TDD — deve FALHAR primeiro)
Tarefa T013: Escrever AuthControllerTest.java (base + cenários de segurança — deve FALHAR primeiro)

# Em sequência após T007 + T009 + T013:
Tarefa T012: Implementar JwtService.java
Tarefa T014: Modificar SegurancaConfig.java (satisfaz T013)
```

## Exemplo de Paralelismo: User Story 1

```bash
# Paralelo: criar stub + DTOs (T015–T017):
Tarefa T015: Criar AuthController.java esqueleto (viabiliza compilação de T013)
Tarefa T016: Criar LoginRequisicaoDTO.java
Tarefa T017: Criar LoginRespostaDTO.java

# Após T015–T017: escrever testes
Tarefa T018: Escrever AuthServiceTest.java cenários US1 (deve FALHAR primeiro)
Tarefa T019: Adicionar cenários US1 em AuthControllerTest.java (deve FALHAR primeiro)

# Em sequência: implementar após testes escritos:
Tarefa T020: Implementar AuthService.java login() (satisfaz T018)
Tarefa T021: Implementar AuthController.java POST /api/auth/login (satisfaz T019)
Tarefa T022: Modificar FiltroBearerToken.java (validação JWT + X-Token-Renovado + request.setAttribute)
```

---

## Estratégia de Implementação

### MVP Primeiro (User Stories 1 + 2 — ambas P1)

1. Concluir Fase 1: Configuração (T001–T003)
2. Concluir Fase 2: Base (T004–T014) — **CRÍTICO, bloqueia tudo**
3. Concluir Fase 3: US1 — Login com Credenciais Válidas (T015–T022)
4. Concluir Fase 4: US2 — Login com Credenciais Inválidas (T023–T025)
5. **PARAR e VALIDAR**: Testar fluxo completo via quickstart.md
6. Entregar/demonstrar se pronto

### Entrega Incremental

1. Configuração + Base → infraestrutura pronta
2. US1 → login básico funcionando com JWT (MVP mínimo)
3. US2 → segurança das credenciais inválidas
4. US3 → proteção força bruta
5. US4 → logout com revogação de token por dispositivo
6. Refinamento → validação final e cobertura confirmada

---

## Notas

- **TDD é OBRIGATÓRIO** (Constituição Princípio II, NÃO NEGOCIÁVEL): todo teste DEVE ser escrito antes da implementação e FALHAR antes de implementar
- **Esqueleto AuthController**: T015 cria o arquivo mínimo (sem métodos) para que AuthControllerTest.java (T013) compile na Fase 2; T021 substitui o esqueleto pela implementação real
- **Extração do token para logout**: FiltroBearerToken (T022) armazena o JWT via `request.setAttribute("jwtAtual", token)`; AuthController (T031) lê via `(String) request.getAttribute("jwtAtual")` — mecanismo determinístico, sem ambiguidade
- **Retrocompatibilidade**: FiltroBearerToken DEVE verificar token admin estático antes de tentar parsear JWT (research.md, Decisão 9)
- **Mensagem genérica**: "E-mail ou senha inválidos" para TODAS as falhas de autenticação — nunca revelar a condição específica (FR-004, FR-005)
- **Sliding window**: X-Token-Renovado emitido apenas para JWT de usuário — ausente quando autenticação for via token admin (contracts/auth-api.md)
- **Denylist**: JwtService.validar() consulta tokens_revogados a cada validação — 1 consulta por requisição autenticada
- **Idioma**: entidades e variáveis em PT-BR; termos canônicos em inglês (JWT, Bearer, Controller, Service, Repository, DTO, jti, sub, exp — Constituição v1.1.3)
- Commits seguindo Conventional Commits: `feat:`, `test:`, `refactor:`, `docs:` (Gitflow ativo na branch feature/003-user-login)
