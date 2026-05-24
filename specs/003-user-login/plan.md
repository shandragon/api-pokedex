# Implementation Plan: User Login

**Branch**: `feature/003-user-login` | **Data**: 2026-05-23 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `specs/003-user-login/spec.md`

## Summary

Implementar autenticação de usuários cadastrados via e-mail e senha usando **JWT (HS256)** com **sliding window** — a cada requisição autenticada, um novo JWT com TTL renovado é emitido no header `X-Token-Renovado`. Logout revoga o token atual via denylist (`tokens_revogados` no PostgreSQL). Proteção contra força bruta (5 tentativas → bloqueio de 15 min) e registro de auditoria com IP completam a feature. Nova dependência: `io.jsonwebtoken:jjwt` 0.12.x.

## Technical Context

**Language/Version**: Java 17 (Spring Boot 3.4.3)

**Primary Dependencies**: Spring Web, Spring Security 6, Spring Data JPA, Lombok, Bean Validation (jakarta.validation), uuid-creator 5.3.3, **jjwt-api/jjwt-impl/jjwt-jackson 0.12.6 (nova)**

**Storage**: PostgreSQL — tabelas novas: `tokens_revogados`, `log_login`; modificação de `usuarios`

**Testing**: JUnit 5, MockMvc, Mockito, Spring Boot Test, spring-security-test

**Target Platform**: Servidor Linux (REST API)

**Project Type**: Web service (REST API)

**Performance Goals**: Login em menos de 3 segundos sob carga normal (SC-001); validação JWT sem consulta ao banco (stateless por assinatura) + 1 consulta à denylist por requisição autenticada

**Constraints**: STATELESS já configurado no Spring Security; retrocompatibilidade com token admin estático; chave JWT externalizada via application.yml (variável de ambiente em produção)

**Scale/Scope**: Compatível com o volume atual da API Pokédex

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Princípio | Status | Observação |
|-----------|--------|------------|
| I. Qualidade em Primeiro Lugar | ✅ | Responsabilidade única em cada componente; `JwtService` isolado |
| II. TDD (NÃO NEGOCIÁVEL) | ✅ | Testes escritos antes da implementação em cada tarefa |
| III. Verificação Automatizada | ✅ | Cobertura unitária (Service/JwtService) e integração (Controller + Repository) |
| IV. YAGNI/KISS | ✅ | JWT simétrico HS256 sem OAuth2 Resource Server; denylist simples em PostgreSQL |
| V. Interfaces Baseadas em Contrato | ✅ | Contrato documentado em `contracts/auth-api.md`; header `X-Token-Renovado` especificado |
| Gitflow | ✅ | Branch `feature/003-user-login` a partir de `main` |
| Commits Semânticos | ✅ | `feat:`, `test:`, `refactor:`, `docs:` conforme Conventional Commits |
| Idioma PT-BR | ✅ | Nomes de entidades e variáveis em PT-BR; termos canônicos em inglês (JWT, Bearer, HS256, Controller, Service, Repository, DTO, jti, sub, exp, iat) |

**Resultado: APROVADO — nenhuma violação detectada.**

**Justificativa da nova dependência** (Princípio IV): `jjwt` é a única adição; é o padrão de facto para JWT em Spring Boot e foi definida como requisito pelo usuário. Sem alternativa mais simples que atenda ao requisito de JWT.

## Project Structure

### Documentação (esta feature)

```text
specs/003-user-login/
├── plan.md              # Este arquivo
├── research.md          # Decisões técnicas (Phase 0)
├── data-model.md        # Modelo de dados (Phase 1)
├── quickstart.md        # Guia de uso (Phase 1)
├── contracts/
│   └── auth-api.md      # Contrato REST JWT (Phase 1)
└── tasks.md             # Gerado por /speckit-tasks
```

### Código-fonte (raiz do repositório)

```text
src/main/java/br/edu/shandragon/pokedex/
├── auth/
│   ├── controller/
│   │   └── AuthController.java                   (novo — POST /api/auth/login, POST /api/auth/logout)
│   ├── dto/
│   │   ├── LoginRequisicaoDTO.java                (novo — email, senha)
│   │   └── LoginRespostaDTO.java                  (novo — token JWT, expira_em)
│   ├── entity/
        └── LogLogin.java                          (novo — id UUID, usuarioId UUID nullable, ip, resultado, criadoEm)
│   │   └── TokenRevogado.java                     (novo — jti UUID PK, expira_em)
│   ├── repository/jpa/
│   │   ├── TokenRevogadoRepository.java           (novo — existsByJti)
│   │   └── LogLoginRepository.java                (novo — save)
│   └── service/
│       ├── AuthService.java                       (novo — login(), logout())
│       └── JwtService.java                        (novo — gerar(), validar(), extrairJti(), extrairSub())
├── config/
│   ├── FiltroBearerToken.java                     (modificar — validar JWT + renovar via X-Token-Renovado)
│   └── SegurancaConfig.java                       (modificar — liberar POST /api/auth/login)
└── usuario/
    └── entity/
        └── Usuario.java                           (modificar — adicionar: ativo, tentativasFalhas, bloqueadoAte)

src/test/java/br/edu/shandragon/pokedex/
├── auth/
│   ├── controller/
│   │   └── AuthControllerTest.java                (novo — @WebMvcTest)
│   ├── repository/
│   │   └── TokenRevogadoRepositoryIntegrationTest.java  (novo — @DataJpaTest)
│   └── service/
│       ├── AuthServiceTest.java                   (novo — @ExtendWith(MockitoExtension))
│       └── JwtServiceTest.java                    (novo — @ExtendWith(MockitoExtension))
└── usuario/
    └── repository/
        └── UsuarioRepositoryIntegrationTest.java  (existente — adicionar testes dos novos campos)
```

**Structure Decision**: Pacote `auth` novo, paralelo a `usuario` e `pokemon`, seguindo o padrão modular existente. `JwtService` isolado do `AuthService` para facilitar testes unitários da lógica JWT. Modificações mínimas em `config` e `usuario`.

## Complexity Tracking

> Nenhuma violação de constituição detectada — tabela não aplicável.

---

## Detalhes de Implementação por Componente

### JwtService

```
gerar(usuarioId: UUID) → String (JWT)
  - Claims: sub=usuarioId.toString(), iat=now, exp=now+TTL, jti=UuidUtil.gerarV7().toString()
  - Assinar com HMAC-SHA256 usando chave de app.seguranca.jwt-secret

validar(token: String) → boolean
  - Parsear JWT; verificar assinatura; verificar exp; verificar jti não está em tokens_revogados

extrairSub(token: String) → String (userId)
extrairJti(token: String) → String (jti)
extrairExp(token: String) → Instant
```

### AuthService — Fluxo de login

```
1. Normalizar email para minúsculas
2. Buscar Usuario por email → 401 genérico se não encontrado
3. Verificar usuario.ativo → 401 genérico se inativo
4. Verificar bloqueio: bloqueadoAte != null && bloqueadoAte > now → 401 genérico
5. Verificar senha: BCrypt.matches(senhaInformada, usuario.senhaHash)
   - Falha: tentativasFalhas++; se >= 5 → bloqueadoAte = now + 15min; salvar; 401 genérico
   - Sucesso: tentativasFalhas = 0; bloqueadoAte = null; salvar
6. Gerar JWT via JwtService.gerar(usuario.id)
7. Registrar LogLogin(SUCESSO, ip, usuario.id)
8. Retornar LoginRespostaDTO(token, expiraEm)
```

### AuthService — Fluxo de logout

```
1. Extrair jti do JWT presente no SecurityContext (já validado pelo filtro)
2. Criar TokenRevogado(jti=UUID.fromString(jti), expiraEm=jwtService.extrairExp(token))
3. Salvar em tokens_revogados
4. Retornar 204
```

### FiltroBearerToken — Modificação (sliding window)

```
Para cada requisição com "Authorization: Bearer <valor>":
1. Se valor == tokenAdmin → autenticar como admin (comportamento atual preservado)
2. Senão: jwtService.validar(valor)
   - Inválido/expirado/revogado → ignorar (Spring Security retorna 401)
   - Válido:
     a. Extrair sub (userId); autenticar com userId como principal
     b. Gerar novo JWT via jwtService.gerar(UUID.fromString(sub))
     c. Adicionar header na resposta: response.setHeader("X-Token-Renovado", novoJwt)
```

### SegurancaConfig — Modificação

```
Adicionar: .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
(POST /api/auth/logout requer autenticação — já coberto por anyRequest().authenticated())
```

### pom.xml — Nova dependência

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
