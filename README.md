# Pokedex API

API REST para cadastro e gerenciamento de Pokémon, desenvolvida com Spring Boot. Utiliza **persistência híbrida**: PostgreSQL para dados estruturados e estáveis (usuários e atributos fixos dos Pokémon) e MongoDB para atributos flexíveis dos Pokémon (ataques, fraquezas, estatísticas, etc.).

## Tecnologias

- Java 17
- Spring Boot 3.4.3
- Spring Data JPA + PostgreSQL (dados estruturados)
- Spring Data MongoDB (dados flexíveis)
- Spring Security (autenticação por token Bearer)
- UUID V7 via `uuid-creator` (identificadores ordenados por tempo)
- Lombok
- H2 + Flapdoodle (ambientes de teste)

## Estrutura do Projeto

```text
src/main/java/br/edu/shandragon/pokedex/
├── compartilhado/          # Utilitários compartilhados (UuidUtil)
├── config/                 # Configurações (JPA, MongoDB, Segurança)
├── exception/              # Tratamento global de exceções
├── pokemon/
│   ├── documento/          # Documento MongoDB (PokemonAtributos)
│   ├── dto/                # DTOs de entrada e saída
│   ├── controller/         # Endpoints REST
│   ├── entidade/           # Entidades JPA (Pokemon, Tipo, Evolucao)
│   ├── repositorio/
│   │   ├── jpa/            # Repositórios PostgreSQL
│   │   └── mongo/          # Repositórios MongoDB
│   └── servico/            # Lógica de negócio + merge dos dois bancos
└── usuario/
    ├── dto/
    ├── controller/
    ├── entidade/
    ├── repositorio/jpa/
    └── servico/
```

## Pré-requisitos

- JDK 17+
- Maven 3.9+
- Docker (para PostgreSQL e MongoDB locais)

## Configuração e execução

### 1. Subir os bancos de dados

```bash
docker run -d \
  --name pokedex-postgres \
  -e POSTGRES_DB=pokedex \
  -e POSTGRES_USER=pokedex \
  -e POSTGRES_PASSWORD=pokedex \
  -p 5432:5432 \
  postgres:16-alpine

docker run -d \
  --name pokedex-mongo \
  -p 27017:27017 \
  mongo:7
```

### 2. Configurar `application.properties`

O arquivo `src/main/resources/application.properties` já vem com as configurações padrão para desenvolvimento local. Ajuste se necessário:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/pokedex
spring.datasource.username=pokedex
spring.datasource.password=pokedex
spring.data.mongodb.uri=mongodb://localhost:27017/pokedex
app.seguranca.token-admin=token-dev-alterar-em-producao
```

### 3. Executar

```bash
mvn spring-boot:run
```

A API estará disponível em `http://localhost:8080`.

> Na primeira execução, a tabela `tipos` é populada automaticamente com os 18 tipos canônicos do Pokémon via `data.sql`.

## Endpoints

### Pokémon

| Método | Rota | Autenticação | Descrição |
|--------|------|-------------|-----------|
| `POST` | `/api/pokemon` | Token Bearer | Cadastra um Pokémon |
| `GET` | `/api/pokemon` | Nenhuma | Lista todos os Pokémon |
| `GET` | `/api/pokemon/{id}` | Nenhuma | Busca Pokémon por UUID |
| `GET` | `/api/pokedex/por-tipo` | Nenhuma | Lista Pokémon agrupados por tipo |
| `GET` | `/api/pokedex/{id}/evolucoes` | Nenhuma | Lista evoluções de um Pokémon |

### Usuários

| Método | Rota | Autenticação | Descrição |
|--------|------|-------------|-----------|
| `POST` | `/api/usuarios` | Token Bearer | Cadastra um usuário |
| `GET` | `/api/usuarios` | Nenhuma | Lista usuários (apenas id e nome) |
| `GET` | `/api/usuarios/{id}` | Nenhuma | Busca usuário por UUID |

### Autenticação

Endpoints de escrita exigem o cabeçalho:

```
Authorization: Bearer <token-configurado>
```

O token é configurado em `app.seguranca.token-admin` no `application.properties`.

## Exemplos de uso

### Cadastrar um Pokémon

```bash
curl -X POST http://localhost:8080/api/pokemon \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer token-dev-alterar-em-producao" \
  -d '{
    "numeroPokdex": 1,
    "nome": "Bulbasaur",
    "tipos": ["Planta", "Veneno"],
    "ataques": ["Investida", "Absorver", "Chicote de Vinha"],
    "fraquezas": ["Fogo", "Gelo", "Voador", "Psíquico"],
    "estatisticas": { "hp": 45, "ataque": 49, "defesa": 49 }
  }'
```

### Listar Pokémon

```bash
curl http://localhost:8080/api/pokemon
```

### Cadastrar um Usuário

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

### Listar Usuários

```bash
curl http://localhost:8080/api/usuarios
```

## Executar testes

```bash
mvn test
```

Os testes utilizam H2 (PostgreSQL em memória) e Flapdoodle (MongoDB embarcado) — nenhum banco externo necessário para testar.

## Tipos disponíveis

Os seguintes tipos são pré-cadastrados e válidos para uso na criação de Pokémon:

`Normal` · `Fogo` · `Água` · `Planta` · `Elétrico` · `Gelo` · `Lutador` · `Veneno` · `Terra` · `Voador` · `Psíquico` · `Inseto` · `Pedra` · `Fantasma` · `Dragão` · `Sombrio` · `Aço` · `Fada`
