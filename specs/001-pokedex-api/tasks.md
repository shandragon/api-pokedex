# Tarefas de Implementação: Cadastro de Pokémon

**Funcionalidade**: Cadastro de Pokémon
**Branch**: `feature/001-pokedex-api`

## Visão Geral da Estratégia de Implementação
- **MVP**: Focar na listagem agrupada por tipo (US1).
- **Incremental**: Adicionar visualização de evoluções (US2) após a listagem estar estável.
- **TDD**: Todos os componentes seguirão o ciclo TDD — tarefas `[TDD]` devem ser executadas **antes** da tarefa de implementação correspondente.

## Fase 1: Setup do Projeto
- [x] T001 Inicializar projeto Spring Boot (Java 17, Web, JPA, Lombok)
- [x] T002 Configurar profiles de banco: H2 para dev (`application.yml`) e PostgreSQL para prod (`application-prod.yml`)
- [x] T003 Configurar CORS em `WebMvcConfigurer` em `src/main/java/br/edu/shandragon/pokedex/config/WebConfig.java`

## Fase 2: Fundamentos
- [x] T004 [P] Criar modelo de dados `Pokemon` em `src/main/java/br/edu/shandragon/pokedex/model/Pokemon.java`
- [x] T005 [P] Criar modelo de dados `Tipo` em `src/main/java/br/edu/shandragon/pokedex/model/Tipo.java`
- [x] T006 [P] Criar modelo de dados `Evolucao` em `src/main/java/br/edu/shandragon/pokedex/model/Evolucao.java`

## Fase 3: História de Usuário 1 - Listagem de Pokémon Agrupada por Tipo
- [x] T007 [US1] Criar `PokemonRepository` em `src/main/java/br/edu/shandragon/pokedex/repository/PokemonRepository.java`
- [x] T007a [US1][TDD] Escrever testes unitários para `PokemonService` (agrupamento por tipo, lista vazia, múltiplos tipos) em `src/test/java/br/edu/shandragon/pokedex/service/PokemonServiceTest.java`
- [x] T008 [US1] Implementar `PokemonService` com lógica de agrupamento por tipo e `@Transactional(readOnly = true)` em `src/main/java/br/edu/shandragon/pokedex/service/PokemonService.java`
- [x] T008a [US1][TDD] Escrever testes de integração (MockMvc) para endpoint `GET /api/pokedex/por-tipo` em `src/test/java/br/edu/shandragon/pokedex/controller/PokemonControllerTest.java`
- [x] T009 [US1] Criar `PokemonController` com endpoint GET `/api/pokedex/por-tipo` em `src/main/java/br/edu/shandragon/pokedex/controller/PokemonController.java`
- [x] T009b [US1] Criar DTOs `TipoDTO` e `PokemonDTO` em `src/main/java/br/edu/shandragon/pokedex/dto/`

## Fase 4: História de Usuário 2 - Visualização de Evoluções
- [x] T010a [US2] Criar DTO `EvolucaoDTO` em `src/main/java/br/edu/shandragon/pokedex/dto/EvolucaoDTO.java`
- [x] T010 [US2] Adicionar método `buscarEvolucoes` no `PokemonService` em `src/main/java/br/edu/shandragon/pokedex/service/PokemonService.java`
- [x] T011 [US2] Adicionar endpoint GET `/api/pokedex/{id}/evolucoes` no `PokemonController` em `src/main/java/br/edu/shandragon/pokedex/controller/PokemonController.java`

## Fase 5: Polimento e Finalização
- [x] T012 [CS-001] Implementar teste de desempenho para `GET /api/pokedex/por-tipo` com 1.000 registros: tempo de resposta deve ser inferior a 500ms (usar JMeter, k6 ou `@SpringBootTest` com seed de dados)
- [x] T013 [US2] Implementar tratamento de ID inexistente em `GET /api/pokedex/{id}/evolucoes`: retornar HTTP 404 com corpo `{"erro": "Pokémon não encontrado"}` e adicionar teste correspondente
- [x] T014 Revisar cobertura de testes com JaCoCo (meta: ≥ 80% de cobertura de linhas)
- [x] T015 Validar documentação em PT-BR

## Dependências e Ordem de Execução
1. **Fase 1** (Setup) → **Fase 2** (Modelos) → **Fase 3** (US1) → **Fase 4** (US2) → **Fase 5** (Polimento).
- Tarefas `[TDD]` devem ser criadas antes da tarefa de implementação correspondente (ciclo vermelho-verde-refatorar).
- US1 e US2 são independentes após a conclusão dos modelos básicos (Fase 2).

## Oportunidades de Paralelização
- [P] T004, T005, T006: Podem ser criados simultaneamente após o setup.
- [P] Testes de unidade/integração podem ser desenvolvidos paralelamente à implementação de cada serviço/controlador.
