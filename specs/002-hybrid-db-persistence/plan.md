# Plano de Implementação: Persistência Híbrida em Banco de Dados

**Branch**: `002-hybrid-db-persistence` | **Data**: 2026-05-17 | **Spec**: [spec.md](spec.md)

**Entrada**: Especificação de funcionalidade de `specs/002-hybrid-db-persistence/spec.md`

---

## Resumo

Adicionar camada de persistência dual ao projeto Spring Boot existente. O domínio Pokémon é uma **entidade dividida**: atributos fixos (nome, tipos, evoluções, numeroPokdex) ficam no **PostgreSQL** via JPA; atributos flexíveis (ataques, fraquezas, estatísticas, etc.) ficam no **MongoDB** usando o mesmo UUID V7 como chave de correlação. O domínio Usuário usa exclusivamente **PostgreSQL**. Todos os identificadores utilizam **UUID V7**. Endpoints de escrita são protegidos por token estático Bearer; leituras são abertas. A camada de serviço apresenta ao cliente uma visão unificada do Pokémon, mesclando dados dos dois bancos.

---

## Contexto Técnico

**Linguagem/Versão**: Java 17

**Framework**: Spring Boot 3.4.3

**Gerenciador de build**: Maven

**Dependências existentes**:
- `spring-boot-starter-web` — camada HTTP existente
- `spring-boot-starter-data-jpa` — JPA (mantido para Pokemon fixo + Usuario)
- `postgresql` — driver PostgreSQL (já presente)
- `h2` — banco em memória para testes JPA
- `lombok` — redução de boilerplate

**Novas dependências a adicionar**:
- `spring-boot-starter-data-mongodb` — Spring Data MongoDB para PokemonAtributos
- `spring-boot-starter-security` — Spring Security para proteger endpoints de escrita
- `com.github.f4b6a3:uuid-creator:5.3.3` — geração de UUID V7
- `de.flapdoodle.embed:de.flapdoodle.embed.mongo.spring3x` (escopo test) — MongoDB embarcado para testes de integração

**Armazenamento**:
- PostgreSQL: entidades `Pokemon` (fixo), `Evolucao`, `Usuario` via JPA
- MongoDB: documento `PokemonAtributos` (flexível) via Spring Data MongoDB

**Testes**: JUnit 5 + Spring Boot Test (já configurados via `spring-boot-starter-test`)

**Plataforma alvo**: Servidor Linux (JVM)

**Tipo de projeto**: Serviço web (REST API)

**Metas de desempenho**: Operações de escrita e leitura concluídas em < 1 segundo com até 50 requisições simultâneas (CS-001, CS-002)

**Escala/Escopo**: Fase inicial; sem dados de produção existentes (migração de dados não necessária)

---

## Verificação de Constituição

*GATE: Deve passar antes da Fase 0. Re-verificado após o design da Fase 1.*

| Princípio | Status | Observação |
|-----------|--------|-----------|
| **I. Qualidade em Primeiro Lugar** | ✅ | Separação clara por domínio; abstração via interfaces Repository; entidade dividida com lógica de merge isolada no serviço |
| **II. TDD (Não Negociável)** | ✅ | Todas as tarefas seguem ciclo vermelho-verde-refatorar; testes de integração com bancos reais exigidos |
| **III. Verificação Automatizada** | ✅ | Testes unitários para serviços (incluindo merge de fontes); testes de integração para repositórios e controllers |
| **IV. Simplicidade (YAGNI/KISS)** | ✅ | Token estático para auth; entidade `Tipo` mantida — conjunto fechado de 18 tipos exige validação contra tabela mestra (typos rejeitados); merge simples por UUID sem tabela de mapeamento |
| **V. Interfaces Baseadas em Contrato** | ✅ | Contratos REST documentados em `contracts/`; interfaces Repository desacoplam serviço do banco |
| **Governança: PT-BR** | ✅ | Todo texto narrativo em PT-BR; padrões técnicos (Repository, Service, Controller) em inglês conforme convenção existente |

**Resultado**: APROVADO — prosseguir para implementação.

---

## Estrutura do Projeto

### Documentação (esta funcionalidade)

```text
specs/002-hybrid-db-persistence/
├── plan.md              # Este arquivo
├── spec.md              # Especificação
├── research.md          # Decisões de pesquisa (Fase 0)
├── data-model.md        # Modelo de dados com diagrama (Fase 1)
├── quickstart.md        # Guia rápido de desenvolvimento (Fase 1)
├── contracts/
│   ├── pokemon.md       # Contrato REST de Pokemon — visão unificada (Fase 1)
│   ├── usuario.md       # Contrato REST de Usuario (Fase 1)
│   └── pokedex-legado.md # Contratos existentes migrados da spec 001 (Fase 1)
└── tasks.md             # Tarefas (gerado por /speckit-tasks)
```

### Código-fonte (raiz do repositório)

```text
src/main/java/br/edu/shandragon/pokedex/
│
├── config/
│   ├── WebConfig.java                        # Existente — mantido
│   ├── PersistenciaJpaConfig.java            # NOVO — @EnableJpaRepositories nos pacotes jpa/
│   ├── PersistenciaMongoConfig.java          # NOVO — @EnableMongoRepositories nos pacotes mongo/
│   └── SegurancaConfig.java                  # NOVO — Spring Security, token Bearer estático
│
├── compartilhado/
│   └── UuidUtil.java                         # NOVO — gerador UUID V7 centralizado
│
├── pokemon/
│   ├── entidade/
│   │   ├── Pokemon.java                      # REFATORADO — @Entity JPA, @ManyToMany com Tipo, UUID v7
│   │   ├── Tipo.java                         # REFATORADO — @Entity JPA, UUID v7 (era Long)
│   │   └── Evolucao.java                     # REFATORADO — @Entity JPA, UUID v7 (era Long)
│   ├── documento/
│   │   └── PokemonAtributos.java             # NOVO — @Document MongoDB, atributos flexíveis
│   ├── repositorio/
│   │   ├── jpa/
│   │   │   ├── PokemonRepositorio.java       # REFATORADO — JpaRepository<Pokemon, UUID>
│   │   │   ├── TipoRepositorio.java          # REFATORADO — JpaRepository<Tipo, UUID>
│   │   │   └── EvolucaoRepositorio.java      # REFATORADO — JpaRepository<Evolucao, UUID>
│   │   └── mongo/
│   │       └── PokemonAtributosRepositorio.java  # NOVO — MongoRepository<PokemonAtributos, String>
│   ├── servico/
│   │   └── PokemonServico.java               # REFATORADO — merge PostgreSQL + MongoDB na leitura
│   ├── controller/
│   │   └── PokemonController.java            # REFATORADO — adiciona POST /api/pokemon; migra GET existentes
│   └── dto/
│       ├── PokemonRequisicaoDTO.java          # NOVO — corpo da requisição (fixos + flexíveis)
│       └── PokemonRespostaDTO.java            # NOVO — resposta unificada (fixos + flexíveis)
│
└── usuario/
    ├── entidade/
    │   └── Usuario.java                      # NOVO — @Entity JPA, UUID v7
    ├── repositorio/
    │   └── jpa/
    │       └── UsuarioRepositorio.java       # NOVO — JpaRepository<Usuario, UUID>
    ├── servico/
    │   └── UsuarioServico.java               # NOVO
    ├── controller/
    │   └── UsuarioController.java            # NOVO
    └── dto/
        ├── UsuarioRequisicaoDTO.java          # NOVO
        └── UsuarioRespostaDTO.java            # NOVO

# Classes REMOVIDAS ou SUBSTITUÍDAS:
# model/Pokemon.java              → pokemon/entidade/Pokemon.java        (refatorado: UUID v7, @ManyToMany Tipo)
# model/Tipo.java                 → pokemon/entidade/Tipo.java           (refatorado: UUID v7, era Long)
# model/Evolucao.java             → pokemon/entidade/Evolucao.java       (refatorado: UUID v7, era Long)
# repository/PokemonRepository.java   → pokemon/repositorio/jpa/PokemonRepositorio.java
# repository/TipoRepository.java       → pokemon/repositorio/jpa/TipoRepositorio.java
# repository/EvolucaoRepository.java   → pokemon/repositorio/jpa/EvolucaoRepositorio.java
# dto/PokemonDTO.java             → pokemon/dto/PokemonRespostaDTO.java
# dto/TipoDTO.java                → eliminado (tipos retornados como List<String> de nomes)
# dto/EvolucaoDTO.java            → embutido em PokemonRespostaDTO
```

```text
src/test/java/br/edu/shandragon/pokedex/
├── pokemon/
│   ├── repositorio/
│   │   ├── PokemonRepositorioIntegracaoTest.java       # NOVO — testa JPA com H2
│   │   └── PokemonAtributosRepositorioIntegracaoTest.java  # NOVO — testa MongoDB embarcado
│   ├── servico/
│   │   └── PokemonServicoTest.java                     # REFATORADO — inclui mock do merge
│   └── controller/
│       └── PokemonControllerTest.java                  # REFATORADO
└── usuario/
    ├── repositorio/
    │   └── UsuarioRepositorioIntegracaoTest.java        # NOVO — testa JPA com H2
    ├── servico/
    │   └── UsuarioServicoTest.java                      # NOVO
    └── controller/
        └── UsuarioControllerTest.java                   # NOVO
```

**Decisão de estrutura**: Sub-pacotes `jpa/` e `mongo/` dentro de `repositorio/` permitem que o domínio `pokemon` use simultaneamente os dois bancos sem ambiguidade no scanning de componentes Spring (`@EnableJpaRepositories` e `@EnableMongoRepositories` apontam para sub-pacotes distintos).

---

## Rastreamento de Complexidade

| Exceção | Necessidade | Alternativa Mais Simples Descartada |
|---------|------------|-------------------------------------|
| Dois bancos de dados (PostgreSQL + MongoDB) | Requisito explícito: esquema fixo para atributos estruturados, esquema flexível para atributos em evolução | Apenas PostgreSQL com JSONB — não atende ao requisito explícito de usar MongoDB para o esquema flexível |
| Entidade dividida (split entity) para Pokemon | Requisito explícito: nome/tipo/evolução no Postgres, ataques/fraquezas no MongoDB | Documento completo no MongoDB — perde integridade relacional (unicidade de numeroPokdex, FK de evolução) |
| Sub-pacotes `jpa/` e `mongo/` | Necessário quando um domínio usa dois bancos — Spring Boot não consegue auto-detectar | Separação por domínio de pacote — inviável quando pokemon usa os dois bancos |
| Entidade `Tipo` separada (vs. @ElementCollection) | Tipos Pokémon são conjunto fechado — integridade de domínio exige validação contra tabela mestra; typos são rejeitados na criação | @ElementCollection com strings livres — aceita qualquer string sem validação |
| Spring Security (token estático) | RF-010: endpoints de escrita DEVEM rejeitar requisições não autenticadas | Sem segurança — viola requisito funcional |
