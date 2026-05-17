# Tarefas: Persistência Híbrida em Banco de Dados

**Entrada**: Documentos de design em `specs/002-hybrid-db-persistence/`

**Constituição — Princípio II (NÃO NEGOCIÁVEL)**: TDD é a metodologia primária. Testes são escritos ANTES da implementação. Todo teste deve FALHAR antes de ser implementado. O ciclo é: vermelho → verde → refatorar.

**Organização**: Tarefas agrupadas por história de usuário para permitir implementação e teste independentes.

## Formato: `[ID] [P?] [Story?] Descrição com caminho de arquivo`

- **[P]**: Pode ser executada em paralelo (arquivos diferentes, sem dependências incompletas)
- **[Story]**: História de usuário à qual a tarefa pertence (US1, US2, US3)
- Caminhos base: `src/main/java/br/edu/shandragon/pokedex/` e `src/test/java/br/edu/shandragon/pokedex/`

---

## Fase 1: Setup (Infraestrutura Compartilhada)

**Objetivo**: Configurar dependências, utilitários e configurações que todas as histórias precisam

- [x] T001 Atualizar `pom.xml` com as dependências: `spring-boot-starter-data-mongodb`, `spring-boot-starter-security`, `com.github.f4b6a3:uuid-creator:5.3.3` e `de.flapdoodle.embed:de.flapdoodle.embed.mongo.spring3x` (escopo test)
- [x] T002 [P] Criar `compartilhado/UuidUtil.java` com método estático `gerarV7()` retornando `java.util.UUID` via `UuidCreator.getTimeOrderedEpoch()`
- [x] T003 [P] Criar `config/PersistenciaJpaConfig.java` com `@EnableJpaRepositories(basePackages = {"br.edu.shandragon.pokedex.pokemon.repositorio.jpa", "br.edu.shandragon.pokedex.usuario.repositorio.jpa"})`
- [x] T004 [P] Criar `config/PersistenciaMongoConfig.java` com `@EnableMongoRepositories(basePackages = {"br.edu.shandragon.pokedex.pokemon.repositorio.mongo"})`
- [x] T005 Criar `config/SegurancaConfig.java` com `@EnableWebSecurity`: permitir todos os métodos GET sem autenticação; exigir cabeçalho `Authorization: Bearer <token>` para POST, PUT, DELETE; token lido de `app.seguranca.token-admin`
- [x] T006 Criar `src/main/resources/application.yml` com datasource PostgreSQL, URI MongoDB e propriedade `app.seguranca.token-admin`; criar `src/test/resources/application-test.yml` com H2 (JPA) e MongoDB embarcado (Flapdoodle)

**Checkpoint**: Projeto compila com as novas dependências e configurações carregadas

---

## Fase 2: Fundamentos (Pré-requisitos Bloqueantes)

**Objetivo**: Entidades, repositórios e seed de dados que todas as histórias precisam antes de começar

⚠️ **CRÍTICO**: Nenhuma história de usuário pode começar antes desta fase estar completa

- [x] T007 [P] Migrar e refatorar `model/Tipo.java` → `pokemon/entidade/Tipo.java`: trocar `Long id` por `UUID id` (UUID V7 via `UuidUtil.gerarV7()`); manter `@Column(unique = true)` em `nome`; remover arquivo original
- [x] T008 [P] Migrar e refatorar `model/Evolucao.java` → `pokemon/entidade/Evolucao.java`: trocar `Long id` por `UUID id`; manter `@ManyToOne` para `pokemonOrigem` e `pokemonDestino`; remover arquivo original
- [x] T009 Migrar e refatorar `model/Pokemon.java` → `pokemon/entidade/Pokemon.java`: trocar `Long id` por `UUID id`; substituir `@ManyToMany Set<Tipo>` mantendo a relação via tabela `pokemon_tipos` com FKs UUID; depende de T007; remover arquivo original
- [x] T010 [P] Criar `pokemon/documento/PokemonAtributos.java`: classe `@Document(collection = "pokemon_atributos")` com campo `@Id String id` e campo `Map<String, Object> atributos`
- [x] T011 Criar `pokemon/repositorio/jpa/TipoRepositorio.java`: `JpaRepository<Tipo, UUID>` com método `Optional<Tipo> findByNome(String nome)`; remover `repository/TipoRepository.java`
- [x] T012 [P] Criar `pokemon/repositorio/jpa/PokemonRepositorio.java`: `JpaRepository<Pokemon, UUID>` com método `List<Pokemon> findAllByTiposContaining(Tipo tipo)`; remover `repository/PokemonRepository.java`
- [x] T013 [P] Criar `pokemon/repositorio/jpa/EvolucaoRepositorio.java`: `JpaRepository<Evolucao, UUID>` com método `List<Evolucao> findByPokemonOrigemIdOrPokemonDestinoId(UUID origemId, UUID destinoId)`; remover `repository/EvolucaoRepository.java`
- [x] T014 Criar `pokemon/repositorio/mongo/PokemonAtributosRepositorio.java`: `MongoRepository<PokemonAtributos, String>`
- [x] T015 Criar `src/main/resources/data.sql` com INSERT dos 18 tipos canônicos do Pokémon: Normal, Fogo, Água, Planta, Elétrico, Gelo, Lutador, Veneno, Terra, Voador, Psíquico, Inseto, Pedra, Fantasma, Dragão, Sombrio, Aço, Fada (usar `INSERT INTO tipos (id, nome) VALUES (...) ON CONFLICT DO NOTHING`)
- [x] T016 Remover arquivos completamente obsoletos: `dto/TipoDTO.java`, `dto/EvolucaoDTO.java`, `dto/PokemonDTO.java`; ajustar quaisquer imports quebrados no código restante

**Checkpoint**: Fundamentos prontos — entidades mapeadas, repositórios criados, tipos pré-populados. Histórias de usuário podem começar em paralelo.

---

## Fase 3: História de Usuário 1 — Persistência do Cadastro de Usuário (Prioridade: P1) 🎯 MVP

**Objetivo**: Qualquer chamador autenticado pode cadastrar um usuário; qualquer chamador pode listar/buscar usuários; e-mail é único; senha nunca exposta

**Teste Independente**: Enviar `POST /api/usuarios` com token válido → registro criado; tentar com mesmo e-mail → 409; `GET /api/usuarios` sem token → lista com id+nome (sem e-mail)

### Testes para História de Usuário 1

> ⚠️ **TDD**: Escrever estes testes PRIMEIRO. Confirmar que FALHAM antes de implementar.

- [x] T017 [P] [US1] Escrever `usuario/repositorio/UsuarioRepositorioIntegracaoTest.java`: testar persistência de Usuario com UUID V7, unicidade de e-mail (deve lançar exceção ao duplicar) e recuperação por ID via `@DataJpaTest` com H2
- [x] T018 [P] [US1] Escrever `usuario/servico/UsuarioServicoTest.java`: testar `cadastrar()` (retorna DTO sem senha), `cadastrar()` com e-mail duplicado (lança exceção), `listar()` (retorna apenas id+nome), `buscarPorId()` com ID inexistente (lança exceção) — usando mocks do repositório
- [x] T019 [P] [US1] Escrever `usuario/controller/UsuarioControllerTest.java`: testar `POST /api/usuarios` sem token (401), com token inválido (401), com body válido e token (201 sem campo senha na resposta), com e-mail duplicado (409); testar `GET /api/usuarios` sem token (200, campos apenas id+nome); testar `GET /api/usuarios/{id}` inexistente (404)

### Implementação da História de Usuário 1

- [x] T020 [US1] Criar `usuario/entidade/Usuario.java`: `@Entity @Table(name = "usuarios")` com campos `UUID id` (pré-gerado com `UuidUtil.gerarV7()`), `String nome`, `String email` (`@Column(unique = true)`), `String senhaHash`, `Instant criadoEm` (`@Column(updatable = false)`)
- [x] T021 [US1] Criar `usuario/repositorio/jpa/UsuarioRepositorio.java`: `JpaRepository<Usuario, UUID>` com método `boolean existsByEmail(String email)`
- [x] T022 [US1] Criar `usuario/dto/UsuarioRequisicaoDTO.java` (campos: `nome`, `email`, `senha`) e `usuario/dto/UsuarioRespostaDTO.java` (campos: `id`, `nome`, `email`, `criadoEm`) — sem campo `senha` no DTO de resposta
- [x] T023 [US1] Criar `usuario/servico/UsuarioServico.java`: método `cadastrar(UsuarioRequisicaoDTO)` gera UUID V7, hasheia senha com `BCryptPasswordEncoder`, verifica unicidade de e-mail (lança `ResponseStatusException(CONFLICT)` se duplicado), persiste e retorna `UsuarioRespostaDTO`; método `listar()` retorna `List<UsuarioRespostaPublicaDTO>` (apenas id+nome); método `buscarPorId(UUID)` retorna `UsuarioRespostaPublicaDTO` ou lança `ResponseStatusException(NOT_FOUND)`
- [x] T024 [US1] Criar `usuario/dto/UsuarioRespostaPublicaDTO.java` (apenas `id` e `nome`, sem e-mail) para uso nos endpoints públicos de listagem e busca por ID
- [x] T025 [US1] Criar `usuario/controller/UsuarioController.java` com `@RequestMapping("/api/usuarios")`: `POST /` chama `usuarioServico.cadastrar()` retornando 201; `GET /` chama `usuarioServico.listar()` retornando 200; `GET /{id}` chama `usuarioServico.buscarPorId()` retornando 200 ou 404

**Checkpoint**: `POST /api/usuarios`, `GET /api/usuarios` e `GET /api/usuarios/{id}` funcionais e testados. Todos os testes de US1 passando.

---

## Fase 4: História de Usuário 2 — Persistência de Dados de Pokémon (Prioridade: P2)

**Objetivo**: Chamador autenticado pode criar um Pokémon com atributos fixos (PostgreSQL) e flexíveis (MongoDB); resposta é visão unificada

**Teste Independente**: `POST /api/pokemon` com `numeroPokdex`, `nome`, `tipos` válidos, atributos extras → 201 com todos os campos; `POST` com numeroPokdex duplicado → 409; `POST` com tipo inválido → 400; buscar por ID retorna visão unificada

### Testes para História de Usuário 2

> ⚠️ **TDD**: Escrever estes testes PRIMEIRO. Confirmar que FALHAM antes de implementar.

- [x] T026 [P] [US2] Escrever `pokemon/repositorio/PokemonRepositorioIntegracaoTest.java`: testar persistência de Pokemon com UUID V7, unicidade de `numeroPokdex`, relação `@ManyToMany` com Tipo via `@DataJpaTest` com H2
- [x] T027 [P] [US2] Escrever `pokemon/repositorio/PokemonAtributosRepositorioIntegracaoTest.java`: testar persistência de `PokemonAtributos` com ID String (UUID V7), armazenamento e recuperação de `Map<String, Object>` com campos arbitrários — usando `@DataMongoTest` com Flapdoodle
- [x] T028 [P] [US2] Escrever `pokemon/servico/PokemonServicoTest.java` para operações de escrita: testar `criar()` com atributos fixos e flexíveis (verifica save em ambos os repositórios), `criar()` com tipo inválido (lança exceção), `criar()` com numeroPokdex duplicado (lança exceção) — usando mocks dos repositórios
- [x] T029 [P] [US2] Escrever `pokemon/controller/PokemonControllerTest.java` para `POST /api/pokemon`: sem token (401), tipo inválido no body (400), numeroPokdex duplicado (409), body válido com token (201 com campos fixos e flexíveis mesclados na resposta)

### Implementação da História de Usuário 2

- [x] T030 [US2] Criar `pokemon/dto/PokemonRequisicaoDTO.java` com campos: `Integer numeroPokdex`, `String nome`, `List<String> tipos`, `List<Map<String, Object>> evolucoes` e `Map<String, Object> atributosExtras` (recebe qualquer campo adicional)
- [x] T031 [US2] Criar `pokemon/dto/PokemonRespostaDTO.java` com campos: `String id`, `Integer numeroPokdex`, `String nome`, `List<String> tipos`, campo de evoluções e `Map<String, Object> atributosExtras` para visão unificada
- [x] T032 [US2] Criar `pokemon/servico/PokemonServico.java` com método `criar(PokemonRequisicaoDTO)`: (1) validar tipos contra `TipoRepositorio.findByNome()` (lança 400 se inválido); (2) verificar unicidade de `numeroPokdex` (lança 409 se duplicado); (3) gerar UUID V7; (4) salvar entidade `Pokemon` em PostgreSQL; (5) salvar `PokemonAtributos` em MongoDB com mesmo UUID; (6) retornar `PokemonRespostaDTO` unificado
- [x] T033 [US2] Criar `pokemon/controller/PokemonController.java` com `@RequestMapping`: `POST /api/pokemon` protegido por token chama `pokemonServico.criar()` retornando 201; manter rotas legadas `GET /api/pokedex/por-tipo` e `GET /api/pokedex/{id}/evolucoes` (migradas, id agora `String` UUID); remover `controller/PokemonController.java` original e `service/PokemonService.java` original após migração
- [x] T034 [US2] Criar handler de exceção em `exception/GlobalExceptionHandler.java` (ou atualizar o existente) para: `409 Conflict` (numeroPokdex duplicado, e-mail duplicado) e `400 Bad Request` (tipo inválido, campos obrigatórios ausentes) com corpo `{"erro": "mensagem"}`

**Checkpoint**: `POST /api/pokemon` funcional e testado. Pokémon persiste atributos fixos no PostgreSQL e flexíveis no MongoDB. Todos os testes de US2 passando.

---

## Fase 5: História de Usuário 3 — Listagem e Consulta de Dados Persistidos (Prioridade: P3)

**Objetivo**: Qualquer chamador pode listar e buscar Pokémon (visão unificada) e usuários (apenas id+nome); coleções vazias retornam array vazio

**Teste Independente**: Criar N registros de cada tipo, listar e confirmar que a coleção retornada corresponde exatamente aos dados criados; buscar por ID inexistente retorna 404; sem registros retorna `[]`

### Testes para História de Usuário 3

> ⚠️ **TDD**: Escrever estes testes PRIMEIRO. Confirmar que FALHAM antes de implementar.

- [x] T035 [P] [US3] Escrever testes no `pokemon/servico/PokemonServicoTest.java` para: `listarTodos()` retorna lista mesclada (fixos + flexíveis), `listarTodos()` com MongoDB indisponível retorna apenas campos fixos, `buscarPorId()` com ID existente retorna visão unificada, `buscarPorId()` com ID inexistente lança 404
- [x] T036 [P] [US3] Escrever testes no `pokemon/controller/PokemonControllerTest.java` para: `GET /api/pokemon` sem autenticação (200, array com todos os campos), `GET /api/pokemon` sem registros (200, `[]`), `GET /api/pokemon/{id}` com ID válido (200), `GET /api/pokemon/{id}` inválido (404)

### Implementação da História de Usuário 3

- [x] T037 [US3] Implementar `listarTodos()` em `pokemon/servico/PokemonServico.java`: busca todos os `Pokemon` no PostgreSQL; para cada um, busca `PokemonAtributos` no MongoDB pelo mesmo UUID; mescla em `PokemonRespostaDTO`; se MongoDB não retornar documento para algum Pokemon, retornar apenas atributos fixos (sem erro)
- [x] T038 [US3] Implementar `buscarPorId(String id)` em `pokemon/servico/PokemonServico.java`: busca `Pokemon` no PostgreSQL por UUID; lança `ResponseStatusException(NOT_FOUND)` se ausente; mescla com `PokemonAtributos` do MongoDB (opcional); retorna `PokemonRespostaDTO`
- [x] T039 [US3] Adicionar endpoints `GET /api/pokemon` e `GET /api/pokemon/{id}` em `pokemon/controller/PokemonController.java` chamando `pokemonServico.listarTodos()` e `pokemonServico.buscarPorId()`

**Checkpoint**: Todos os endpoints de leitura (`GET /api/pokemon`, `GET /api/pokemon/{id}`, `GET /api/usuarios`, `GET /api/usuarios/{id}`) funcionais e testados. Todos os testes de US3 passando.

---

## Fase Final: Polimento e Aspectos Transversais

**Objetivo**: Garantia de qualidade, validação de contrato e verificação do quickstart

- [x] T040 [P] Verificar que `GET /api/pokedex/por-tipo` agrupa corretamente por tipo com dados vindos do PostgreSQL e retorna o mesmo formato da spec 001
- [x] T041 [P] Verificar que `GET /api/pokedex/{id}/evolucoes` funciona com UUID String (não mais Long) e retorna formato conforme `contracts/pokedex-legado.md`
- [x] T042 [P] Adicionar validação de entrada com `@Valid` e `@NotBlank`, `@NotNull`, `@Min(1)` nos DTOs de requisição de Usuario e Pokemon; garantir que `400 Bad Request` é retornado com mensagem clara para campos obrigatórios ausentes
- [ ] T043 Executar todos os cenários do `quickstart.md` manualmente com containers Docker reais (PostgreSQL + MongoDB) e confirmar que todos os `curl` retornam os códigos e corpos esperados

---

## Dependências e Ordem de Execução

### Dependências entre Fases

- **Fase 1 (Setup)**: Sem dependências — iniciar imediatamente
- **Fase 2 (Fundamentos)**: Depende da Fase 1 concluída — **BLOQUEIA** todas as histórias
- **Fases 3, 4, 5 (Histórias)**: Todas dependem da Fase 2; podem ser executadas em sequência de prioridade (P1 → P2 → P3)
- **Fase Final (Polimento)**: Depende de todas as histórias concluídas

### Dependências entre Histórias de Usuário

- **US1 (P1)**: Inicia após Fase 2 — sem dependência de outras histórias
- **US2 (P2)**: Inicia após Fase 2 — sem dependência de US1 (domínios independentes)
- **US3 (P3)**: Depende de US1 e US2 estar concluídas (endpoints de listagem precisam de dados criáveis)

### Dependências dentro de cada História

```
Testes (todos em paralelo)
        ↓
Entidade → Repositório → DTOs
                          ↓
                       Serviço → Controller → Handler de Exceção
```

### Oportunidades de Paralelismo

- T002, T003, T004 (Fase 1): paralelos entre si
- T007, T008, T010 (Fase 2): paralelos entre si; T009 depende de T007
- T011, T012, T013 (Fase 2): paralelos entre si; dependem de T007, T008, T009
- T017, T018, T019 (US1 — testes): paralelos entre si
- T026, T027, T028, T029 (US2 — testes): paralelos entre si
- T035, T036 (US3 — testes): paralelos entre si
- T040, T041, T042 (Fase Final): paralelos entre si

---

## Exemplo de Execução Paralela — Fase 2

```bash
# Executar em paralelo (arquivos diferentes, sem dependências entre si):
Tarefa: "Migrar Tipo.java → pokemon/entidade/Tipo.java"           (T007)
Tarefa: "Migrar Evolucao.java → pokemon/entidade/Evolucao.java"   (T008)
Tarefa: "Criar PokemonAtributos.java"                             (T010)

# Após T007 concluído, iniciar em paralelo:
Tarefa: "Migrar Pokemon.java → pokemon/entidade/Pokemon.java"     (T009)
Tarefa: "Criar TipoRepositorio.java"                              (T011)
```

---

## Estratégia de Implementação

### MVP Imediato (apenas US1)

1. Concluir Fase 1: Setup
2. Concluir Fase 2: Fundamentos (**CRÍTICO**)
3. Concluir Fase 3: US1 — Cadastro de Usuário
4. **PARAR e VALIDAR**: `POST /api/usuarios`, `GET /api/usuarios`, `GET /api/usuarios/{id}` funcionando
5. Demo/entrega parcial possível

### Entrega Incremental

1. Setup + Fundamentos → base pronta
2. + US1 → cadastro e leitura de usuários (demo)
3. + US2 → criação e busca de Pokémon com persistência híbrida (demo)
4. + US3 → listagem completa (demo)
5. + Polimento → versão final

---

## Notas

- `[P]` = arquivos diferentes, sem dependências incompletas — podem ser executadas em paralelo
- `[Story]` = rastreabilidade da tarefa para a história de usuário correspondente
- **TDD é obrigatório** (Constituição, Princípio II): todo teste deve ser escrito e **falhar** antes da implementação
- Confirmar que cada teste falha antes de implementar
- Realizar commit após cada tarefa concluída ou grupo lógico (commits semânticos: `feat:`, `test:`, `refactor:`, `chore:`)
- Parar em qualquer checkpoint para validar a história independentemente
- A Fase 2 é o pré-requisito mais crítico — nenhuma história funciona sem ela completa
