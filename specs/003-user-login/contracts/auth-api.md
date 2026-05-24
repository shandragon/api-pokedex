# Contratos da API: Autenticação

**Feature**: 003-user-login | **Data**: 2026-05-23
**Mecanismo**: JWT (HS256) com sliding window via header `X-Token-Renovado`
**Base URL**: `/api/auth`

---

## POST /api/auth/login

Autentica um usuário cadastrado e emite um JWT de acesso.

**Autenticação**: Não requerida

### Requisição

```
POST /api/auth/login
Content-Type: application/json
```

```json
{
  "email": "joao@example.com",
  "senha": "minhasenha123"
}
```

| Campo  | Tipo   | Obrigatório | Validação                         |
|--------|--------|-------------|-----------------------------------|
| email  | string | sim         | formato de e-mail válido          |
| senha  | string | sim         | não vazio                         |

### Respostas

#### 200 OK — Login bem-sucedido

```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIwMTk0ZjNhMiIsImlhdCI6MTcxNjQ4MDAwMCwiZXhwIjoxNzE2NDgzNjAwLCJqdGkiOiIwMTk0ZjNhMi1lNzdiLTdjMDAtYTFiMi0zYzRkNWU2ZjM4OTAifQ.signature",
  "expira_em": "2026-05-23T15:30:00Z"
}
```

#### 400 Bad Request — Corpo inválido

```json
{
  "status": 400,
  "mensagem": "Dados de requisição inválidos"
}
```

*Cenários: campo ausente, e-mail com formato inválido.*

#### 401 Unauthorized — Credenciais inválidas ou conta bloqueada/inativa

```json
{
  "status": 401,
  "mensagem": "E-mail ou senha inválidos"
}
```

*Cenários: senha incorreta, e-mail inexistente, conta inativa, conta bloqueada por força bruta.*
*A mensagem é idêntica em todos os casos para impedir enumeração de contas.*

---

## POST /api/auth/logout

Revoga o JWT atual adicionando seu `jti` à denylist.

**Autenticação**: Bearer JWT válido no header `Authorization`

### Requisição

```
POST /api/auth/logout
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

*Corpo vazio.*

### Respostas

#### 204 No Content — Logout realizado com sucesso

*Sem corpo. O `jti` do JWT é inserido na denylist. Outros tokens do mesmo usuário permanecem ativos.*

#### 401 Unauthorized — JWT ausente, inválido, expirado ou já revogado

```json
{
  "status": 401,
  "mensagem": "Autenticação necessária"
}
```

---

## Sliding Window: Header de Renovação

Em **todas as respostas de requisições autenticadas com JWT de usuário** (não apenas no logout), o servidor emite um novo JWT com TTL renovado:

```
HTTP/1.1 200 OK
X-Token-Renovado: eyJhbGciOiJIUzI1NiJ9.<novo-payload>.<nova-assinatura>
```

| Comportamento | Detalhe |
|---------------|---------|
| Header presente | Sempre que a autenticação for via JWT de usuário válido |
| Header ausente | Autenticação via token admin estático |
| Novo token | Mesmo `sub`, novo `iat`/`exp`/`jti` |
| Token anterior | Permanece válido até expiração natural (`exp`) |
| Responsabilidade do cliente | Substituir o token armazenado pelo valor de `X-Token-Renovado` a cada resposta |

---

## Notas de Contrato

- Todos os timestamps seguem ISO 8601 em UTC (sufixo `Z`).
- O campo `token` no body do login é o JWT completo; enviá-lo como `Bearer <token>` no header `Authorization`.
- Após logout, qualquer requisição com o JWT revogado (mesmo dentro do prazo de `exp`) recebe `401`.
- O logout revoga apenas o JWT presente na requisição; JWTs de outros dispositivos do mesmo usuário permanecem ativos.
