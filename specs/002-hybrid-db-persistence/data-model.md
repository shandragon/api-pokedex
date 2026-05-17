# Modelo de Dados: Persistência Híbrida em Banco de Dados

**Branch**: `002-hybrid-db-persistence` | **Data**: 2026-05-17

---

## Visão Geral

O sistema utiliza dois bancos de dados independentes. O domínio Pokémon é uma **entidade dividida** entre os dois:

| Entidade / Documento | Banco | Tipo de Esquema | Conteúdo |
|----------------------|-------|-----------------|----------|
| `Pokemon` (entidade) | PostgreSQL | Fixo | id, numeroPokdex, nome, tipos, evoluções |
| `PokemonAtributos` (documento) | MongoDB | Flexível | ataques, fraquezas, estatísticas e quaisquer outros atributos |
| `Usuario` (entidade) | PostgreSQL | Fixo | id, nome, email, senhaHash, criadoEm |

Ambos os bancos utilizam o mesmo **UUID V7** como chave de identidade. O UUID do registro PostgreSQL é reusado como `_id` no MongoDB — não há tabela de mapeamento.

---

## PostgreSQL — Diagrama de tabelas

```
┌──────────────────────────────┐       ┌───────────────────────────┐       ┌──────────────────────┐
│         pokemons             │       │       pokemon_tipos        │       │        tipos         │
├──────────────────────────────┤       ├───────────────────────────┤       ├──────────────────────┤
│ id          UUID PK          │───┐   │ pokemon_id  UUID  FK      │   ┌──▶│ id    UUID PK        │
│ numero_pokdex INT UNIQUE     │   └──▶│ tipo_id     UUID  FK      │───┘   │ nome  VARCHAR UNIQUE │
│ nome        VARCHAR(255)     │       └───────────────────────────┘       └──────────────────────┘
└──────────────┬───────────────┘
               │ 1
               │
               ▼ N
┌──────────────────────────────┐
│          evolucoes           │
├──────────────────────────────┤
│ id                UUID PK   │
│ pokemon_origem_id  UUID FK  │──▶ pokemons.id
│ pokemon_destino_id UUID FK  │──▶ pokemons.id
└──────────────────────────────┘

┌──────────────────────────────┐
│          usuarios            │
├──────────────────────────────┤
│ id          UUID PK          │
│ nome        VARCHAR(255)     │
│ email       VARCHAR(255)     │
│ senha_hash  VARCHAR(255)     │
│ criado_em   TIMESTAMPTZ      │
└──────────────────────────────┘
```

---

## Entidade: Tipo (PostgreSQL)

**Tabela**: `tipos`

| Campo | Tipo SQL | Restrições | Descrição |
|-------|----------|-----------|-----------|
| `id` | `UUID` | PK, NOT NULL | Identificador único gerado pelo sistema (UUID v7) |
| `nome` | `VARCHAR(50)` | NOT NULL, UNIQUE | Nome canônico do tipo elemental (ex: "Fogo", "Água", "Planta") |

**Regras de validação**:
- `nome`: obrigatório; único na tabela; máximo 50 caracteres
- Tabela pré-populada com os 18 tipos canônicos do universo Pokémon; novas entradas exigem operação administrativa

**Índices**:
- PK: `tipos_pkey` em `id`
- UNIQUE: `tipos_nome_unique` em `nome`

---

## Entidade: Pokemon (PostgreSQL)

**Tabela**: `pokemons`

| Campo | Tipo SQL | Restrições | Descrição |
|-------|----------|-----------|-----------|
| `id` | `UUID` | PK, NOT NULL | Identificador único gerado pelo sistema (UUID v7) |
| `numero_pokdex` | `INTEGER` | NOT NULL, UNIQUE | Número nacional do Pokédex — chave de unicidade do domínio |
| `nome` | `VARCHAR(255)` | NOT NULL | Nome do Pokémon |

**Tabela de junção**: `pokemon_tipos` (gerada via `@ManyToMany`)

| Campo | Tipo SQL | Restrições | Descrição |
|-------|----------|-----------|-----------|
| `pokemon_id` | `UUID` | FK → `pokemons.id`, NOT NULL | Referência ao Pokémon |
| `tipo_id` | `UUID` | FK → `tipos.id`, NOT NULL | Referência ao Tipo elemental |

**Regras de validação**:
- `numero_pokdex`: obrigatório; inteiro positivo (≥ 1); único na tabela
- `nome`: obrigatório; não pode ser vazio; máximo 255 caracteres
- `tipos`: opcional; cada nome de tipo enviado deve corresponder a um `Tipo` existente — nomes inválidos resultam em `400 Bad Request`

**Índices**:
- PK: `pokemons_pkey` em `id`
- UNIQUE: `pokemons_numero_pokdex_unique` em `numero_pokdex`
- FK INDEX: `idx_pokemon_tipos_pokemon_id` em `pokemon_id`
- FK INDEX: `idx_pokemon_tipos_tipo_id` em `tipo_id`

---

## Entidade: Evolucao (PostgreSQL)

**Tabela**: `evolucoes`

| Campo | Tipo SQL | Restrições | Descrição |
|-------|----------|-----------|-----------|
| `id` | `UUID` | PK, NOT NULL | Identificador único da relação evolutiva (UUID v7) |
| `pokemon_origem_id` | `UUID` | FK → `pokemons.id`, NOT NULL | Pokémon que evolui (pré-evolução) |
| `pokemon_destino_id` | `UUID` | FK → `pokemons.id`, NOT NULL | Pokémon resultante da evolução |

**Regras de validação**:
- `pokemon_origem_id` e `pokemon_destino_id` devem referenciar Pokémon existentes
- Um par origem-destino pode ser registrado apenas uma vez (unicidade de relação)

**Índices**:
- PK: `evolucoes_pkey` em `id`
- FK INDEX: `idx_evolucoes_origem` em `pokemon_origem_id`
- FK INDEX: `idx_evolucoes_destino` em `pokemon_destino_id`

---

## Entidade: Usuario (PostgreSQL)

**Tabela**: `usuarios`

| Campo | Tipo SQL | Restrições | Descrição |
|-------|----------|-----------|-----------|
| `id` | `UUID` | PK, NOT NULL | Identificador único gerado pelo sistema (UUID v7) |
| `nome` | `VARCHAR(255)` | NOT NULL | Nome completo do usuário |
| `email` | `VARCHAR(255)` | NOT NULL, UNIQUE | Endereço de e-mail — chave de unicidade |
| `senha_hash` | `VARCHAR(255)` | NOT NULL | Senha processada com BCrypt |
| `criado_em` | `TIMESTAMP WITH TIME ZONE` | NOT NULL, DEFAULT NOW() | Data/hora de criação (imutável) |

**Índices**:
- PK: `usuarios_pkey` em `id`
- UNIQUE: `usuarios_email_unique` em `email`

---

## Documento: PokemonAtributos (MongoDB)

**Coleção**: `pokemon_atributos`

| Campo | Tipo BSON | Restrições | Descrição |
|-------|-----------|-----------|-----------|
| `_id` | `String` | NOT NULL, UNIQUE | UUID V7 idêntico ao `pokemons.id` do PostgreSQL — chave de correlação entre os dois bancos |
| `*` | `any` | — | Quaisquer campos adicionais fornecidos pelo cliente na criação: `ataques`, `fraquezas`, `estatisticas`, `formaAlternativa`, etc. |

**Regras de validação** (camada de aplicação):
- `_id` é sempre o UUID V7 do Pokémon já persistido no PostgreSQL; nunca gerado independentemente
- Campos reservados (`_id`) não podem ser sobrescritos por atributos do cliente
- Um documento pode estar ausente (Pokémon sem atributos flexíveis) — isso é válido

**Índices**:
- PK: `_id` (padrão MongoDB)

---

## Fluxo de Persistência do Pokémon

### Criação (POST /api/pokemon)

```
Cliente
  │
  ▼
PokemonServico
  ├─── 1. Gera UUID V7
  ├─── 2. Salva atributos fixos → PostgreSQL (pokemons, pokemon_tipos)
  │         Se falhar: retorna erro 400/409; aborta
  ├─── 3. Salva atributos flexíveis → MongoDB (pokemon_atributos)
  │         Se falhar: Pokémon existe no Postgres com campos fixos apenas
  │         Atributos flexíveis podem ser adicionados futuramente
  └─── 4. Retorna resposta unificada ao cliente
```

### Leitura (GET /api/pokemon/{id})

```
PokemonServico
  ├─── 1. Busca atributos fixos → PostgreSQL por UUID
  │         Se não encontrado: retorna 404
  ├─── 2. Busca atributos flexíveis → MongoDB por mesmo UUID
  │         Se não encontrado: retorna objeto com atributos fixos apenas (não é erro)
  └─── 3. Mescla e retorna resposta unificada
```

---

## Identificação Única — UUID V7

| Entidade | Tipo Java | Tipo no banco | Geração |
|----------|-----------|---------------|---------|
| `Pokemon.id` | `java.util.UUID` | `UUID` (Postgres) | `UuidUtil.gerarV7()` antes da inserção |
| `Evolucao.id` | `java.util.UUID` | `UUID` (Postgres) | `UuidUtil.gerarV7()` antes da inserção |
| `PokemonAtributos._id` | `String` | `String` (MongoDB) | Reutiliza UUID do `Pokemon.id` (formato `xxxxxxxx-xxxx-7xxx-xxxx-xxxxxxxxxxxx`) |
| `Usuario.id` | `java.util.UUID` | `UUID` (Postgres) | `UuidUtil.gerarV7()` antes da inserção |
