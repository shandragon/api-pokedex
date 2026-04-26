# Tarefas de Implementação: Cadastro de Pokémon

**Funcionalidade**: Cadastro de Pokémon
**Branch**: `001-pokedex-api`

## Visão Geral da Estratégia de Implementação
- **MVP**: Focar na listagem agrupada por tipo (US1).
- **Incremental**: Adicionar visualização de evoluções (US2) após a listagem estar estável.
- **TDD**: Todos os componentes seguirão o ciclo TDD.

## Fase 1: Setup do Projeto
- [x] T001 Inicializar projeto Spring Boot (Java 17, Web, JPA, Lombok)
- [x] T002 Configurar `application.yml` para PostgreSQL e JPA
- [x] T003 Configurar CORS em `WebMvcConfigurer` em `src/main/java/br/edu/shandragon/pokedex/config/WebConfig.java`

## Fase 2: Fundamentos
- [x] T004 [P] Criar modelo de dados `pokedex` em `src/main/java/br/edu/shandragon/pokedex/model/pokedex.java`
- [x] T005 [P] Criar modelo de dados `Tipo` em `src/main/java/br/edu/shandragon/pokedex/model/Tipo.java`
- [x] T006 [P] Criar modelo de dados `Evolucao` em `src/main/java/br/edu/shandragon/pokedex/model/Evolucao.java`

## Fase 3: História de Usuário 1 - Listagem de Pokémon Agrupada por Tipo
- [x] T007 [US1] Criar `pokedexRepository` em `src/main/java/br/edu/shandragon/pokedex/repository/pokedexRepository.java`
- [x] T008 [US1] Implementar `pokedexService` com lógica de agrupamento por tipo em `src/main/java/br/edu/shandragon/pokedex/service/pokedexService.java`
- [x] T009 [US1] Criar `pokedexController` com endpoint GET `/api/pokedex/por-tipo` em `src/main/java/br/edu/shandragon/pokedex/controller/pokedexController.java`

## Fase 4: História de Usuário 2 - Visualização de Evoluções
- [x] T010 [US2] Adicionar método no `pokedexService` para buscar evoluções em `src/main/java/br/edu/shandragon/pokedex/service/pokedexService.java`
- [x] T011 [US2] Adicionar endpoint GET `/api/pokedex/{id}/evolucoes` no `pokedexController` em `src/main/java/br/edu/shandragon/pokedex/controller/pokedexController.java`

## Fase 5: Polimento e Finalização
- [x] T012 Revisar cobertura de testes e conformidade com padrões RESTful
- [x] T013 Validar documentação em PT-BR

## Dependências e Ordem de Execução
1. **Fase 1** (Setup) → **Fase 2** (Modelos) → **Fase 3** (US1) → **Fase 4** (US2) → **Fase 5** (Polimento).
- US1 e US2 são independentes após a conclusão dos modelos básicos (Fase 2).

## Oportunidades de Paralelização
- [P] T004, T005, T006: Podem ser criados simultaneamente após o setup.
- [P] Testes de unidade/integração podem ser desenvolvidos paralelamente à implementação de cada serviço/controlador.
