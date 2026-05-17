# Contrato de API — Usuário

**Branch**: `002-hybrid-db-persistence` | **Base URL**: `/api/usuarios`

---

## POST /api/usuarios — Cadastrar Usuário

**Autenticação**: Obrigatória (`Authorization: Bearer <token>`)

**Corpo da requisição** (JSON):

```json
{
  "nome": "Ash Ketchum",
  "email": "ash@pokemon.com",
  "senha": "senhaSegura123"
}
```

**Campos obrigatórios**: `nome`, `email`, `senha`

**Respostas**:

| Código | Situação | Corpo |
|--------|----------|-------|
| `201 Created` | Usuário cadastrado com sucesso | Objeto Usuário (sem senha, ver abaixo) |
| `400 Bad Request` | Campo obrigatório ausente ou inválido | `{"erro": "Campo obrigatório ausente: email"}` |
| `401 Unauthorized` | Token ausente ou inválido | `{"erro": "Autenticação necessária"}` |
| `409 Conflict` | E-mail já cadastrado | `{"erro": "E-mail já cadastrado: ash@pokemon.com"}` |

**Corpo da resposta 201**:

```json
{
  "id": "019640a2-0000-7000-8000-000000000010",
  "nome": "Ash Ketchum",
  "email": "ash@pokemon.com",
  "criadoEm": "2026-05-17T10:30:00Z"
}
```

> A senha **nunca** é retornada na resposta.

---

## GET /api/usuarios — Listar Usuários

**Autenticação**: Não necessária

**Parâmetros de query**: nenhum

**Respostas**:

| Código | Situação | Corpo |
|--------|----------|-------|
| `200 OK` | Listagem realizada | Array de objetos resumo de Usuário |
| `200 OK` | Nenhum usuário cadastrado | `[]` (array vazio) |

**Corpo da resposta 200** — somente `id` e `nome` são expostos (RF-012):

```json
[
  {
    "id": "019640a2-0000-7000-8000-000000000010",
    "nome": "Ash Ketchum"
  }
]
```

> O e-mail **nunca** é incluído na listagem pública (dado pessoal sensível — RF-012).

---

## GET /api/usuarios/{id} — Buscar Usuário por ID

**Autenticação**: Não necessária

**Parâmetro de path**: `id` — UUID V7 do usuário (string)

**Respostas**:

| Código | Situação | Corpo |
|--------|----------|-------|
| `200 OK` | Usuário encontrado | Objeto resumo de Usuário (apenas `id` e `nome`) |
| `404 Not Found` | ID não encontrado | `{"erro": "Usuário não encontrado: <id>"}` |

**Corpo da resposta 200**:

```json
{
  "id": "019640a2-0000-7000-8000-000000000010",
  "nome": "Ash Ketchum"
}
```

---

## Regras de negócio

1. O campo `id` é sempre gerado pelo sistema (UUID V7); valores enviados pelo cliente são ignorados.
2. `email` deve ser único no sistema. Tentativas de duplicação retornam `409`.
3. A senha recebida na requisição é imediatamente processada com BCrypt; a senha bruta nunca é armazenada.
4. O e-mail é persistido mas nunca exposto em respostas públicas (GET sem autenticação).
5. `criadoEm` é gerado automaticamente no momento da inserção e é imutável.
