# Data Model: User Login

**Feature**: 003-user-login | **Data**: 2026-05-23
**Abordagem**: JWT com sliding window e denylist por logout

## Entidade: Usuario (modificação)

**Tabela**: `usuarios` (existente — adicionar colunas)

| Campo              | Tipo      | Nulável | Padrão | Descrição                                           |
|--------------------|-----------|---------|--------|-----------------------------------------------------|
| id                 | UUID (PK) | não     | —      | UUID v7 (existente)                                 |
| nome               | VARCHAR   | não     | —      | Nome do usuário (existente)                         |
| email              | VARCHAR   | não     | —      | E-mail único, normalizado para minúsculas (existente) |
| senha_hash         | VARCHAR   | não     | —      | Hash BCrypt da senha (existente)                    |
| criado_em          | TIMESTAMP | não     | —      | Timestamp de cadastro (existente)                   |
| ativo              | BOOLEAN   | não     | true   | **novo** — status ativo/inativo da conta            |
| tentativas_falhas  | INT       | não     | 0      | **novo** — contador de falhas consecutivas de login |
| bloqueado_ate      | TIMESTAMP | sim     | null   | **novo** — expiração do bloqueio por força bruta    |

**Regras de validação**:
- `email` normalizado para minúsculas antes da comparação (case-insensitive — FR-003)
- `tentativas_falhas` zerado após login bem-sucedido
- `bloqueado_ate` definido como `Instant.now() + 15min` após a 5ª falha consecutiva
- Login negado se `bloqueado_ate != null && bloqueado_ate > Instant.now()`
- Login negado (com mensagem genérica) se `ativo == false`

---

## Entidade: TokenRevogado (nova)

**Tabela**: `tokens_revogados` (denylist para logout)

| Campo     | Tipo       | Nulável | Padrão | Descrição                                              |
|-----------|------------|---------|--------|--------------------------------------------------------|
| jti       | UUID (PK)  | não     | —      | `jti` claim do JWT revogado (UUID v7)                  |
| expira_em | TIMESTAMP  | não     | —      | Cópia de `exp` do JWT original; permite limpeza futura |

**Regras**:
- Inserido ao fazer logout; nunca atualizado (append-only)
- Na validação de cada requisição: verificar se `jti` do JWT está nesta tabela
- Entradas com `expira_em < now` são logicamente inativas (limpeza periódica fora do escopo desta feature)
- Um `jti` nesta tabela invalida o token mesmo que `exp` ainda não tenha chegado

---

## Entidade: LogLogin (nova)

**Tabela**: `log_login`

| Campo      | Tipo           | Nulável | Padrão | Descrição                                              |
|------------|----------------|---------|--------|--------------------------------------------------------|
| id         | UUID (PK)      | não     | —      | UUID v7                                                |
| usuario_id | UUID (FK)      | sim     | null   | FK para `usuarios.id`; null se e-mail não existir      |
| ip         | VARCHAR(45)    | não     | —      | Endereço IP de origem (suporta IPv4 e IPv6)            |
| resultado  | VARCHAR(10)    | não     | —      | `SUCESSO` ou `FALHA`                                   |
| criado_em  | TIMESTAMP      | não     | —      | Timestamp do evento                                    |

**Regras**:
- Sempre inserido — nunca atualizado (append-only)
- `usuario_id` é null quando o e-mail informado não corresponde a nenhum usuário

---

## Estrutura JWT

```
Header:  { "alg": "HS256", "typ": "JWT" }
Payload: {
  "sub": "0194f3a2-e77b-7000-0000-000000000001",  // UUID do usuário
  "iat": 1716480000,                               // issued at (Unix epoch)
  "exp": 1716483600,                               // expiry (iat + 3600s por padrão)
  "jti": "0194f3a2-e77b-7c00-a1b2-3c4d5e6f7890"  // UUID v7 único deste token
}
Signature: HMAC-SHA256(base64url(header) + "." + base64url(payload), secret)
```

---

## Transições de Estado

### Usuario.ativo
```
ativo=true  →  desativado externamente  →  ativo=false
(login negado com mensagem genérica "E-mail ou senha inválidos")
```

### Usuario.tentativas_falhas / bloqueado_ate
```
Falha de login:
  tentativas_falhas < 5  →  tentativas_falhas + 1
  tentativas_falhas == 5  →  bloqueado_ate = now + 15min

Sucesso de login:
  qualquer estado  →  tentativas_falhas = 0, bloqueado_ate = null

Bloqueio expira automaticamente:
  bloqueado_ate <= now  →  próxima tentativa avalia credenciais normalmente
```

### TokenRevogado (denylist)
```
logout  →  INSERT tokens_revogados(jti, expira_em)
Validação: jti EXISTS em tokens_revogados  →  token inválido (401)
```

### Sliding Window
```
Requisição autenticada  →  novo JWT emitido (sub=mesmo, novo iat/exp/jti)
                        →  retornado em X-Token-Renovado header
Token anterior:         →  permanece válido até exp natural (não revogado automaticamente)
```

---

## Relacionamentos

```
usuarios (1) ──< log_login (N)       [usuario_id FK, nullable]
tokens_revogados                      [standalone — sem FK para manter denylist simples]
```
