# Research: User Login

**Feature**: 003-user-login | **Data**: 2026-05-23
**Revisão**: Abordagem atualizada para JWT com sliding window (input do usuário)

## Decisão 1: Formato do Token

- **Decisão**: JWT (JSON Web Token) assinado com HMAC-SHA256 (HS256), biblioteca `io.jsonwebtoken:jjwt` versão 0.12.x.
- **Racional**: Definido pelo usuário como requisito. JWT é stateless por natureza — não requer consulta ao banco para validação da assinatura; amplamente suportado por clientes REST. A versão 0.12.x é a mais recente da série jjwt, compatível com Spring Boot 3 / Java 21.
- **Alternativas consideradas**:
  - UUID Bearer (plano anterior): mais simples, sem dependência nova, mas requer consulta ao banco em cada requisição; descartado pelo requisito de JWT.
  - `com.auth0:java-jwt`: API mais simples, mas menor adoção no ecossistema Spring; jjwt é o padrão de facto.
  - Spring Security OAuth2 Resource Server: adequado para cenários com múltiplos servidores de autorização; superdimensionado para este caso (YAGNI).

## Decisão 2: Claims do JWT

- **Claims obrigatórias**:
  - `sub`: UUID do usuário (string)
  - `iat`: timestamp de emissão
  - `exp`: timestamp de expiração (iat + TTL configurado)
  - `jti`: UUID v7 — identificador único do token; usado para denylist no logout
- **Racional**: `jti` permite revogação individual sem invalidar todos os tokens do usuário; UUID v7 é consistente com o padrão de IDs do projeto.

## Decisão 3: Sliding Window (Renovação por Requisição)

- **Decisão**: A cada requisição autenticada bem-sucedida, o servidor emite um novo JWT com TTL renovado a partir do momento da requisição e o retorna no header de resposta `X-Token-Renovado`.
- **Racional**: Implementa efetivamente uma sessão de expiração deslizante — o token expira somente se o usuário ficar inativo por mais de 1 TTL. O cliente deve substituir seu token atual pelo token recebido no header.
- **Comportamento do cliente**: ler `X-Token-Renovado` em cada resposta; se presente, armazenar como novo token para próximas requisições. O token anterior permanece válido até sua própria expiração natural (sem revogação automática na renovação — YAGNI).
- **Alternativas consideradas**:
  - Refresh token separado: mais seguro para aplicações públicas; complexidade desnecessária para o escopo atual.
  - Revogar token anterior na renovação: mais seguro mas requer escrita no denylist em cada requisição — impacto de performance injustificado.

## Decisão 4: Armazenamento para Revogação (Denylist)

- **Decisão**: Tabela `tokens_revogados` no PostgreSQL com campos `jti (UUID PK)` e `expira_em (Instant)`. O `jti` do token é inserido ao fazer logout. Limpeza periódica de entradas expiradas é responsabilidade de um job de manutenção (fora do escopo desta feature).
- **Racional**: Revogação por logout requer persistência server-side mesmo com JWT stateless; PostgreSQL já está no projeto; sem nova dependência de infraestrutura. A tabela é pequena — apenas tokens revogados e ainda dentro do TTL.
- **Alternativas consideradas**:
  - Redis com TTL automático: ideal para denylist de alta escala; introduz nova infraestrutura sem justificativa para o volume atual.
  - In-memory (Set): sem persistência; tokens revogados voltariam a ser válidos após reinicialização.

## Decisão 5: Chave de Assinatura

- **Decisão**: Chave simétrica HMAC-SHA256, configurada via `app.seguranca.jwt-secret` em `application.yml` (Base64, mínimo 256 bits). Em produção, valor externalizado via variável de ambiente.
- **Racional**: Simples, sem gestão de par de chaves pública/privada; suficiente para um serviço sem múltiplos servidores de autorização.
- **Alternativas consideradas**:
  - RS256 (assimétrico): necessário quando terceiros precisam validar o token sem compartilhar o segredo; superdimensionado para este cenário.

## Decisão 6: Proteção contra Força Bruta

- **Decisão**: Campos `tentativas_falhas (int, default 0)` e `bloqueado_ate (Instant nullable)` adicionados à entidade `Usuario`. Após 5 falhas consecutivas, `bloqueado_ate` recebe `Instant.now() + 15 min`; login bem-sucedido zera o contador.
- **Racional**: Sem tabela separada; dados de bloqueio pertencem ao `Usuario`; atomicamente atualizável via `@Transactional`.

## Decisão 7: Registro de Auditoria

- **Decisão**: Tabela `log_login` no PostgreSQL (id UUID, usuario_id UUID nullable, ip VARCHAR(45), resultado VARCHAR(10), criado_em Instant).
- **Racional**: Estruturado para consulta; `usuario_id` nullable cobre tentativas com e-mails inexistentes; armazenamento de IP requer divulgação na política de privacidade (LGPD).

## Decisão 8: Endpoints

- **Login**: `POST /api/auth/login` — público
- **Logout**: `POST /api/auth/logout` — requer JWT válido no header `Authorization: Bearer <jwt>`
- **Racional**: Prefixo `/api/auth/` segrega o recurso de autenticação dos recursos de domínio.

## Decisão 9: Retrocompatibilidade com Token Admin

- **Decisão**: O `FiltroBearerToken` verifica primeiro se o valor do header é o token admin estático (`app.seguranca.token-admin`); se não, tenta parsear como JWT. Desta forma, os endpoints existentes que usam o token admin continuam funcionando sem alteração.
- **Racional**: YAGNI — não migrar o token admin para JWT nesta feature; compatibilidade preservada.

## Nova Dependência

Adicionar ao `pom.xml`:
```xml
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.12.6</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.12.6</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.12.6</version>
    <scope>runtime</scope>
</dependency>
```
