# Contrato de API — Pokedex (Endpoints Legados — Spec 001)

**Branch**: `002-hybrid-db-persistence` | **Base URL**: `/api/pokedex`

Estes endpoints foram definidos na spec 001 e são **mantidos** nesta funcionalidade. A implementação subjacente é migrada de JPA/PostgreSQL para MongoDB, mas o contrato HTTP permanece o mesmo.

**Mudança de compatibilidade**: O tipo do `{id}` nos paths muda de `Long` (inteiro) para `String` (UUID V7). Como não há dados de produção, esta mudança não requer migração de dados.

---

## GET /api/pokedex/por-tipo — Listar Pokémon Agrupados por Tipo

**Autenticação**: Não necessária

**Respostas**:

| Código | Situação | Corpo |
|--------|----------|-------|
| `200 OK` | Listagem realizada | Mapa `{tipo: [Pokemon]}` |
| `200 OK` | Nenhum Pokémon cadastrado | `{}` (mapa vazio) |

**Corpo da resposta 200**:

```json
{
  "Planta": [
    {
      "id": "019640a2-0000-7000-8000-000000000001",
      "nome": "Bulbasaur",
      "tipos": ["Planta", "Veneno"]
    }
  ],
  "Veneno": [
    {
      "id": "019640a2-0000-7000-8000-000000000001",
      "nome": "Bulbasaur",
      "tipos": ["Planta", "Veneno"]
    }
  ]
}
```

> Pokémon com múltiplos tipos aparecem em todas as chaves correspondentes.

---

## GET /api/pokedex/{id}/evolucoes — Buscar Evoluções de um Pokémon

**Autenticação**: Não necessária

**Parâmetro de path**: `id` — UUID V7 do Pokémon (**String**, não mais Long)

**Respostas**:

| Código | Situação | Corpo |
|--------|----------|-------|
| `200 OK` | Pokémon encontrado | Lista de relações evolutivas embutidas |
| `200 OK` | Pokémon sem evoluções | `[]` |
| `404 Not Found` | ID não encontrado | `{"erro": "Pokémon não encontrado: <id>"}` |

**Corpo da resposta 200**:

```json
[
  {
    "idPokemonOrigem": "019640a2-0000-7000-8000-000000000001",
    "nomePokemonOrigem": "Bulbasaur",
    "idPokemonDestino": "019640a2-0000-7000-8000-000000000002",
    "nomePokemonDestino": "Ivysaur"
  }
]
```
