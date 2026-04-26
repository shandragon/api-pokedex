# Pokedex API

API para cadastro e gerenciamento de Pokémon, desenvolvida com Spring Boot.

## Tecnologias
- Java 17
- Spring Boot 3.4.3
- Spring Data JPA
- H2 Database (em memória)
- Lombok

## Estrutura do Projeto
```text
src/main/java/br/edu/shandragon/pokedex/
├── config/      # Configurações do projeto
├── controller/  # Endpoints da API
├── dto/         # Objetos de transferência de dados
├── model/       # Entidades de domínio
├── repository/  # Camada de acesso a dados
└── service/     # Lógica de negócio
```

## Como configurar e executar
1. Certifique-se de ter o **JDK 17** instalado.
2. Clone o repositório.
3. No terminal, navegue até a pasta raiz do projeto.
4. Execute o comando para rodar a aplicação:
   ```bash
   ./mvnw spring-boot:run
   ```
5. A API estará disponível em `http://localhost:8080`.