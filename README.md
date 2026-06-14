# Pokedex API

API REST para cadastro e gerenciamento de Pokémon, desenvolvida com Spring Boot. Utiliza **persistência híbrida**: PostgreSQL para dados estruturados e estáveis (usuários e atributos fixos dos Pokémon) e MongoDB para atributos flexíveis dos Pokémon (ataques, fraquezas, estatísticas, etc.).

## Tecnologias

- Java 17
- Spring Boot 3.4.3
- Spring Data JPA + PostgreSQL (dados estruturados)
- Spring Data MongoDB (dados flexíveis)
- Spring Security (autenticação por token Bearer)
- Spring Validation (validação de dados de entrada)
- UUID V7 via `uuid-creator` (identificadores ordenados por tempo)
- Lombok
- H2 + Flapdoodle (ambientes de teste)

## Arquitetura de Persistência Híbrida

O projeto adota uma abordagem híbrida para otimizar o armazenamento de diferentes tipos de dados:

- **PostgreSQL**: Armazena entidades com esquemas rígidos e relacionais, como `Usuario`, `Pokemon` (dados básicos), `Tipo` e `Evolucao`. Garante a integridade referencial e consistência para dados críticos.
- **MongoDB**: Armazena `PokemonAtributos`, permitindo que cada Pokémon possua um conjunto flexível de características (ataques, fraquezas, estatísticas) que podem evoluir sem necessidade de migrações de banco de dados.

Os registros são correlacionados através do mesmo **UUID V7**. O sistema é projetado para ser tolerante a falhas parciais: se o MongoDB estiver temporariamente indisponível, o registro básico do Pokémon ainda é persistido no PostgreSQL.

## Estrutura do Projeto

```text
src/main/java/br/edu/shandragon/pokedex/
├── compartilhado/          # Utilitários compartilhados (UuidUtil)
├── config/                 # Configurações (JPA, MongoDB, Segurança)
├── exception/              # Tratamento global de exceções
├── pokemon/
│   ├── document/           # Documento MongoDB (PokemonAtributos)
│   ├── dto/                # DTOs de entrada e saída
│   ├── controller/         # Endpoints REST
│   ├── entity/             # Entidades JPA (Pokemon, Tipo, Evolucao)
│   ├── repository/
│   │   ├── jpa/            # Repositórios PostgreSQL
│   │   └── mongo/          # Repositórios MongoDB
│   └── service/            # Lógica de negócio + merge dos dois bancos
└── usuario/
    ├── dto/
    ├── controller/
    ├── entity/
    ├── repository/jpa/
    └── service/
```

## Pré-requisitos

- JDK 17+
- Maven 3.9+
- Docker (para PostgreSQL e MongoDB locais)

## Configuração e execução

### 1. Subir os bancos de dados

Utilize o Docker Compose para iniciar as instâncias do PostgreSQL e MongoDB:

```bash
docker-compose up -d
```

Isso criará os containers com as credenciais e bancos de dados necessários (`app_pokedex`).

### 2. Configurar `application.yml`

O arquivo `src/main/resources/application.yml` contém as configurações padrão para desenvolvimento local:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/app_pokedex
    username: user_pokedex
    password: 123456
  data:
    mongodb:
      host: localhost
      port: 27017
      database: app_pokedex
      username: admin_pokedex
      password: 123456
app:
  seguranca:
    token-admin: token-dev-alterar-em-producao
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
| `POST` | `/api/pokemon` | Token Bearer | Cadastra um Pokémon (obrigatório: `nome`, `numeroPokedex`) |
| `GET` | `/api/pokemon` | Nenhuma | Lista Pokémon com paginação opcional (`?page=0&size=10`) |
| `GET` | `/api/pokemon/{id}` | Nenhuma | Busca Pokémon por UUID |
| `GET` | `/api/pokedex/por-tipo` | Nenhuma | Lista Pokémon agrupados por tipo |
| `GET` | `/api/pokedex/{id}/evolucoes` | Nenhuma | Lista evoluções de um Pokémon |

### Usuários

| Método | Rota | Autenticação | Descrição |
|--------|------|-------------|-----------|
| `POST` | `/api/usuarios` | Token Bearer | Cadastra um usuário (obrigatório: `nome`, `email`, `senha`) |
| `GET` | `/api/usuarios` | Nenhuma | Lista usuários (apenas id e nome por privacidade) |
| `GET` | `/api/usuarios/{id}` | Nenhuma | Busca usuário por UUID |

### Autenticação

Endpoints de escrita exigem o cabeçalho:

```
Authorization: Bearer <token-configurado>
```

O token padrão de desenvolvimento é `token-dev-alterar-em-producao`.

## Exemplos de uso

### Cadastrar um Pokémon

```bash
curl -X POST http://localhost:8080/api/pokemon \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer token-dev-alterar-em-producao" \
  -d '{
    "numeroPokedex": 1,
    "nome": "Bulbasaur",
    "tipos": ["Planta", "Veneno"],
    "ataques": ["Investida", "Absorver", "Chicote de Vinha"],
    "fraquezas": ["Fogo", "Gelo", "Voador", "Psíquico"],
    "estatisticas": { "hp": 45, "ataque": 49, "defesa": 49 }
  }'
```

### Listar Pokémon

Retorna uma lista paginada com metadados. Se os parâmetros `page` e `size` não forem informados, retorna todos os itens.

```bash
# Listar com paginação
curl "http://localhost:8080/api/pokemon?page=0&size=5"
```

**Exemplo de Resposta**:
```json
{
  "totalItens": 151,
  "itensPorPagina": 5,
  "paginaAtual": 0,
  "itens": [
    { "id": "...", "nome": "Bulbasaur", ... },
    { "id": "...", "nome": "Ivysaur", ... }
  ]
}
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
