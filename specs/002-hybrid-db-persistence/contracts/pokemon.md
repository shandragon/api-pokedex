# Contrato de API — Pokemon

**Branch**: `002-hybrid-db-persistence` | **Base URL**: `/api/pokemon`

> Endpoints existentes da spec 001 (`/api/pokedex/por-tipo`, `/api/pokedex/{id}/evolucoes`) são mantidos e migrados — seus contratos estão em `pokedex-legado.md`.

---

## Estrutura de armazenamento (transparente ao cliente)

O cliente envia e recebe uma **visão unificada** do Pokémon. Internamente, o servidor divide os dados:
- **PostgreSQL**: `numeroPokdex`, `nome`, `tipos`, `evolucoes`
- **MongoDB**: todos os demais atributos (`ataques`, `fraquezas`, `estatisticas`, etc.)

Esta divisão é completamente transparente para o cliente da API.

---

## POST /api/pokemon — Criar Pokémon

**Autenticação**: Obrigatória (`Authorization: Bearer <token>`)

**Corpo da requisição** (JSON):

```json
{
  "numeroPokdex": 1,
  "nome": "Bulbasaur",
  "tipos": ["Planta", "Veneno"],
  "evolucoes": [
    {
      "idPokemonOrigem": "019640a2-0000-7000-8000-000000000001",
      "idPokemonDestino": "019640a2-0000-7000-8000-000000000002"
    }
  ],
  "ataques": ["Investida", "Absorver", "Chicote de Vinha"],
  "fraquezas": ["Fogo", "Gelo", "Voador", "Psíquico"],
  "estatisticas": {
    "hp": 45,
    "ataque": 49,
    "defesa": 49,
    "velocidade": 45
  }
}
```

**Campos obrigatórios** (persistidos no PostgreSQL): `numeroPokdex`, `nome`

**Campos opcionais estruturados** (persistidos no PostgreSQL): `tipos`, `evolucoes`

**Atributos flexíveis** (persistidos no MongoDB): qualquer outro campo (`ataques`, `fraquezas`, `estatisticas`, e quaisquer campos futuros)

**Respostas**:

| Código | Situação | Corpo |
|--------|----------|-------|
| `201 Created` | Pokémon criado com sucesso | Visão unificada do Pokémon (ver abaixo) |
| `400 Bad Request` | Campo obrigatório ausente ou inválido | `{"erro": "Campo obrigatório ausente: nome"}` |
| `401 Unauthorized` | Token ausente ou inválido | `{"erro": "Autenticação necessária"}` |
| `409 Conflict` | Número do Pokédex já cadastrado | `{"erro": "Número do Pokédex 1 já existe"}` |

**Corpo da resposta 201** (visão unificada):

```json
{
  "id": "019640a2-0000-7000-8000-000000000001",
  "numeroPokdex": 1,
  "nome": "Bulbasaur",
  "tipos": ["Planta", "Veneno"],
  "evolucoes": [
    {
      "idPokemonOrigem": "019640a2-0000-7000-8000-000000000001",
      "idPokemonDestino": "019640a2-0000-7000-8000-000000000002"
    }
  ],
  "ataques": ["Investida", "Absorver", "Chicote de Vinha"],
  "fraquezas": ["Fogo", "Gelo", "Voador", "Psíquico"],
  "estatisticas": {
    "hp": 45,
    "ataque": 49,
    "defesa": 49,
    "velocidade": 45
  }
}
```

---

## GET /api/pokemon — Listar Pokémon

**Autenticação**: Não necessária

**Respostas**:

| Código | Situação | Corpo |
|--------|----------|-------|
| `200 OK` | Listagem realizada | Array de visões unificadas |
| `200 OK` | Nenhum Pokémon cadastrado | `[]` |

---

## GET /api/pokemon/{id} — Buscar Pokémon por ID

**Autenticação**: Não necessária

**Parâmetro de path**: `id` — UUID V7 do Pokémon (string)

**Respostas**:

| Código | Situação | Corpo |
|--------|----------|-------|
| `200 OK` | Pokémon encontrado | Visão unificada (atributos fixos + flexíveis) |
| `200 OK` | Pokémon sem atributos flexíveis | Visão com apenas atributos fixos — sem erro |
| `404 Not Found` | ID não encontrado no PostgreSQL | `{"erro": "Pokémon não encontrado: <id>"}` |

---

## Regras de negócio

1. O campo `id` é sempre gerado pelo sistema (UUID V7); valores enviados pelo cliente são ignorados.
2. `numeroPokdex` deve ser positivo (≥ 1) e único. Duplicatas retornam `409`.
3. Atributos flexíveis ausentes no MongoDB não resultam em erro — a resposta inclui apenas os atributos fixos do PostgreSQL.
4. Todos os atributos flexíveis são retornados exatamente como foram enviados (sem transformação).
5. As chaves `id`, `numeroPokdex`, `nome`, `tipos`, `evolucoes` são sempre gerenciadas pelo PostgreSQL e não podem ser sobrescritas via atributos flexíveis.
