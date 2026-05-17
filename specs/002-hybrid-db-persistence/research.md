# Pesquisa: Persistência Híbrida em Banco de Dados

**Branch**: `002-hybrid-db-persistence` | **Data**: 2026-05-17

---

## Decisão 1 — Biblioteca UUID V7 para Java

**Decisão**: `com.github.f4b6a3:uuid-creator` versão 5.x

**Justificativa**: Biblioteca leve, sem dependências externas, bem mantida (última release ativa), suporte nativo a UUID v1–v7. A alternativa `com.fasterxml.uuid:java-uuid-generator` também suporta v7 mas tem mais dependências transitivas. A JVM padrão (Java 17) oferece apenas UUID v4 via `UUID.randomUUID()`.

**UUID V7** é monotonicamente crescente (baseado em timestamp), o que melhora a performance de índice em bancos relacionais (Postgres) e de documentos (MongoDB) ao inserir novos registros.

**Alternativas descartadas**:
- `java.util.UUID.randomUUID()` — gera apenas v4 (sem ordenação temporal)
- `io.hypersistence:hypersistence-utils` — pesado, focado em Hibernate

---

## Decisão 2 — Spring Data MongoDB

**Decisão**: `spring-boot-starter-data-mongodb`

**Justificativa**: Integração nativa ao ecossistema Spring Boot já presente no projeto. A anotação `@Document` é usada para o documento de atributos flexíveis do Pokémon (`PokemonAtributos`); `MongoRepository` estende `Repository` da mesma forma que `JpaRepository`. Padrões de uso (serviços, controllers) permanecem iguais, minimizando a curva de aprendizado e o impacto na base de código existente.

**Alternativas descartadas**:
- MongoDB Driver puro (`mongodb-driver-sync`) — verboso, sem integração com Spring Data
- Morphia — framework externo, duplicação de funcionalidade já oferecida pelo Spring

---

## Decisão 3 — Configuração Multi-Banco (JPA + MongoDB no mesmo domínio)

**Decisão**: Sub-pacotes por tecnologia dentro do pacote `repositorio` de cada domínio, com configuração explícita via `@EnableJpaRepositories` e `@EnableMongoRepositories`

**Contexto**: O domínio `pokemon` utiliza **ambos** os bancos simultaneamente: PostgreSQL para atributos fixos e MongoDB para atributos flexíveis. Isso impede a separação por domínio de pacote (o que funcionaria se cada banco pertencesse a um domínio diferente). A solução é separar por sub-pacote de tecnologia:

```
pokemon/repositorio/jpa/    → escaneado por @EnableJpaRepositories
pokemon/repositorio/mongo/  → escaneado por @EnableMongoRepositories
usuario/repositorio/jpa/    → escaneado por @EnableJpaRepositories
```

Configuração nas classes de configuração:
- `PersistenciaJpaConfig`: `@EnableJpaRepositories(basePackages = {"...pokemon.repositorio.jpa", "...usuario.repositorio.jpa"})`
- `PersistenciaMongoConfig`: `@EnableMongoRepositories(basePackages = {"...pokemon.repositorio.mongo"})`

**Alternativas descartadas**:
- Separação por domínio de pacote — inviável quando um domínio usa dois bancos
- `AbstractRoutingDataSource` — só suporta múltiplos bancos relacionais, não MongoDB
- Módulos Maven separados — complexidade desnecessária (YAGNI)

---

## Decisão 4 — Arquitetura de Entidade Dividida (Split Entity) para Pokemon

**Decisão**: Pokémon é uma entidade lógica única dividida em dois armazenamentos:
- **PostgreSQL** (`pokemons`, `pokemon_tipos`, `evolucoes`): atributos fixos e estruturados — id, numeroPokdex, nome, lista de tipos, relações de evolução
- **MongoDB** (`pokemon_atributos`): atributos flexíveis — ataques, fraquezas, estatísticas, e qualquer outro campo futuro

**Justificativa**: O usuário definiu explicitamente que campos com esquema estável (nome, tipo, evolução) pertencem ao PostgreSQL, enquanto campos que evoluem com frequência (ataques, fraquezas, etc.) pertencem ao MongoDB. Esta divisão:
1. Preserva a integridade relacional dos dados fixos (unicidade de numeroPokdex, integridade de evoluções via FK)
2. Permite extensão ilimitada dos atributos flexíveis sem migrations no banco relacional
3. Mantém os endpoints existentes (`/por-tipo`, `/evolucoes`) eficientes, pois dependem apenas do PostgreSQL

**Fluxo de criação** (dois passos independentes com mesmo UUID):
1. Persistir atributos fixos no PostgreSQL → obtém UUID V7 gerado
2. Persistir atributos flexíveis no MongoDB com o mesmo UUID V7 como `_id`
3. Se o passo 2 falhar: o Pokémon existe no PostgreSQL com atributos fixos apenas; atributos flexíveis podem ser adicionados posteriormente (os dois armazenamentos são tolerantes a falha parcial)

**Fluxo de leitura** (merge na camada de serviço):
1. Buscar atributos fixos no PostgreSQL
2. Buscar atributos flexíveis no MongoDB pelo mesmo UUID
3. Mesclar na resposta; se não há documento MongoDB, retornar apenas atributos fixos

**Identificador compartilhado**: O UUID V7 gerado para o registro PostgreSQL é reutilizado como `_id` no MongoDB. Isso elimina a necessidade de uma tabela de mapeamento ou referência externa.

**Alternativas descartadas**:
- Documento completo no MongoDB (atributos fixos + flexíveis) — perde integridade relacional; requer duplicação de validações de unicidade (numeroPokdex) fora do banco
- JSONB no PostgreSQL para atributos flexíveis — funciona, mas foge do requisito explícito de usar MongoDB para o esquema flexível
- Referência via `@DBRef` entre PostgreSQL e MongoDB — não existe mecanismo nativo; exige lógica manual de mesma forma

---

## Decisão 5 — Tipos de Pokémon como entidade JPA separada (PostgreSQL)

**Decisão**: `Tipo` é uma entidade JPA com tabela própria (`tipos`), relacionada a `Pokemon` via `@ManyToMany` através da tabela de junção `pokemon_tipos`.

**Justificativa**: Os tipos do universo Pokémon formam um **conjunto fechado e controlado** (18 tipos canônicos: Fogo, Água, Planta, etc.). Manter `Tipo` como entidade garante:
1. **Integridade de domínio** — só tipos pré-cadastrados são aceitos; typos como `"Fgo"` são rejeitados na criação do Pokémon
2. **Consistência de nomenclatura** — o mesmo tipo nunca aparece grafado de formas diferentes entre Pokémon distintos
3. **Extensibilidade** — um endpoint de listagem de tipos pode ser adicionado futuramente sem migração
4. **Alinhamento com a spec 001** — `Tipo` já existia como entidade no modelo original e estava funcionando

O cliente continua enviando tipos como strings (nomes), mas o serviço valida que cada nome corresponde a um `Tipo` existente na tabela antes de persistir o Pokémon.

**Alternativas descartadas**:
- `@ElementCollection` com strings livres — sem validação central; permite tipos inválidos por typo; descartada porque tipos Pokémon são conjunto fixo de domínio, não texto livre
- Campo `tipos` como string delimitada — dificulta queries e viola normalização mínima

---

## Decisão 6 — Autenticação para Endpoints de Escrita

**Decisão**: Spring Security com token estático configurável via `application.properties`

**Justificativa**: A funcionalidade de autenticação completa (login, emissão de tokens, sessões) é explicitamente adiada para uma funcionalidade futura (Suposições da spec). Porém, RF-010 exige que endpoints de escrita rejeitem requisições não autenticadas. A solução mínima é: um token `Bearer` estático definido em propriedade (`app.seguranca.token-admin`), verificado por filtro Spring Security. Simples, testável, substituível quando a feature de auth for implementada.

**Cabeçalho esperado**: `Authorization: Bearer <token-configurado>`

**Alternativas descartadas**:
- Sem autenticação — viola RF-010
- JWT completo — complexidade não justificada neste escopo (YAGNI)
- HTTP Basic Auth — expõe credenciais de forma menos segura que Bearer token

---

## Decisão 7 — Hashing de Senha

**Decisão**: `BCryptPasswordEncoder` do Spring Security

**Justificativa**: Já é dependência transitiva do `spring-boot-starter-security`. BCrypt é o padrão da indústria para hashing de senhas em Java Spring. Nenhuma dependência adicional necessária.
