# Quickstart: User Login (JWT)

**Feature**: 003-user-login | **Data**: 2026-05-23

## Pré-requisitos

- PostgreSQL rodando com banco `app_pokedex` configurado
- Usuário previamente cadastrado via `POST /api/usuarios`

## Fluxo Básico

### 1. Fazer login — obter JWT

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"joao@example.com","senha":"minhasenha123"}'
```

Resposta esperada (`200 OK`):
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiI...",
  "expira_em": "2026-05-23T15:30:00Z"
}
```

### 2. Usar o JWT em requisições autenticadas

```bash
curl -X POST http://localhost:8080/api/pokemon \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiI..." \
  -H "Content-Type: application/json" \
  -d '{"nome":"Pikachu","tipo":"Elétrico"}'
```

Resposta incluirá o header de renovação:
```
HTTP/1.1 201 Created
X-Token-Renovado: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.<novo-payload>.<nova-assinatura>
```

**Importante**: substituir o JWT armazenado pelo valor de `X-Token-Renovado` a cada resposta para manter a sessão ativa (sliding window).

### 3. Fazer logout — revogar JWT atual

```bash
curl -X POST http://localhost:8080/api/auth/logout \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiI..."
```

Resposta: `204 No Content`

### 4. Verificar token revogado

```bash
curl -X GET http://localhost:8080/api/pokemon \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiI..."
```

Resposta: `401 Unauthorized` (mesmo que o JWT ainda não tenha expirado pelo `exp`)

## Comportamentos de Erro

| Situação                        | Status | Mensagem                      |
|---------------------------------|--------|-------------------------------|
| Senha incorreta                 | 401    | E-mail ou senha inválidos     |
| E-mail não cadastrado           | 401    | E-mail ou senha inválidos     |
| Conta inativa                   | 401    | E-mail ou senha inválidos     |
| 5+ falhas consecutivas          | 401    | E-mail ou senha inválidos     |
| JWT expirado                    | 401    | Autenticação necessária       |
| JWT revogado via logout         | 401    | Autenticação necessária       |
| JWT com assinatura inválida     | 401    | Autenticação necessária       |
| E-mail com formato inválido     | 400    | Dados de requisição inválidos |

## Configuração (application.yml)

```yaml
app:
  seguranca:
    token-admin: token-dev-alterar-em-producao   # token admin estático (mantido)
    jwt-secret: base64-encoded-256bit-secret-key  # chave HMAC-SHA256 (novo — externalizar em prod)
    token-ttl-horas: 1                            # TTL dos JWTs (novo)
    bloqueio-tentativas: 5                        # falhas até bloquear (novo)
    bloqueio-duracao-minutos: 15                  # duração do bloqueio (novo)
```

## Decodificar um JWT (debug)

```bash
# Decodificar payload sem verificar assinatura
echo "eyJzdWIiOiIwMTk0ZjNhMiIsImlhdCI6MTcxNjQ4MDAwMH0" | base64 -d
# {"sub":"0194f3a2-...","iat":1716480000,"exp":1716483600,"jti":"0194f3a2-..."}
```
