# Guia Rápido de Desenvolvimento — Persistência Híbrida

**Branch**: `002-hybrid-db-persistence`

---

## Pré-requisitos

- Java 17+
- Maven 3.9+
- Docker (para PostgreSQL e MongoDB locais)
- Token de administrador (configurado em `application.properties`)

---

## Subindo os bancos de dados localmente

```bash
# PostgreSQL
docker run -d \
  --name pokedex-postgres \
  -e POSTGRES_DB=pokedex \
  -e POSTGRES_USER=pokedex \
  -e POSTGRES_PASSWORD=pokedex \
  -p 5432:5432 \
  postgres:16-alpine

# MongoDB
docker run -d \
  --name pokedex-mongo \
  -p 27017:27017 \
  mongo:7
```

---

## Configuração (`application.properties`)

```properties
# PostgreSQL
spring.datasource.url=jdbc:postgresql://localhost:5432/pokedex
spring.datasource.username=pokedex
spring.datasource.password=pokedex
spring.jpa.hibernate.ddl-auto=update

# MongoDB
spring.data.mongodb.uri=mongodb://localhost:27017/pokedex

# Token de administrador (substituir por valor seguro)
app.seguranca.token-admin=token-dev-alterar-em-producao
```

---

## Compilar e executar

```bash
mvn spring-boot:run
```

---

## Exemplos de uso

### Criar um Pokémon (requer token)

```bash
curl -X POST http://localhost:8080/api/pokemon \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer token-dev-alterar-em-producao" \
  -d '{
    "numeroPokdex": 1,
    "nome": "Bulbasaur",
    "tipos": ["Planta", "Veneno"],
    "habilidades": ["Overgrow", "Chlorophyll"]
  }'
```

### Listar Pokémon (aberto)

```bash
curl http://localhost:8080/api/pokemon
```

### Cadastrar um Usuário (requer token)

```bash
curl -X POST http://localhost:8080/api/usuarios \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer token-dev-alterar-em-producao" \
  -d '{
    "nome": "Ash Ketchum",
    "email": "ash@pokemon.com",
    "senha": "senhaSegura123"
  }'
```

### Listar Usuários (aberto)

```bash
curl http://localhost:8080/api/usuarios
```

---

## Executar testes

```bash
mvn test
```

Os testes de integração requerem os bancos em execução. Utilize os containers acima ou configure perfil `test` com banco em memória (H2 para JPA, Flapdoodle para MongoDB).
