# Especificação de Funcionalidade: Persistência Híbrida em Banco de Dados

**Branch da Funcionalidade**: `002-hybrid-db-persistence`

**Criada em**: 2026-05-17

**Status**: Implementada

**Entrada**: Descrição do usuário: "Quero que as informações sejam persistidas em banco de dados de forma que possa ter estrutura mais bem definidas e estaveis, como cadastro de usuário. Contudo, também possua esquemas mais flexíveis, como os pokemons que são criados com frequência."

## Cenários de Usuário e Testes *(obrigatório)*

### História de Usuário 1 - Persistência do Cadastro de Usuário (Prioridade: P1)

Como um novo usuário, quero me cadastrar no sistema para que meus dados pessoais sejam armazenados de forma segura e eu possa acessar o sistema em visitas futuras.

**Por que esta prioridade**: O cadastro de usuário é a entidade de esquema fixo mais fundamental. Sem usuários, não há contexto de autoria ou propriedade de nenhum outro recurso no sistema. Esta história valida que dados estruturados e bem definidos podem ser criados e recuperados de forma confiável.

**Teste Independente**: Pode ser testada completamente enviando uma requisição de cadastro com nome, e-mail e senha, e verificando que o usuário pode ser encontrado pelo seu identificador — entregando um sistema de contas funcionando de forma independente das funcionalidades de Pokémon.

**Cenários de Aceitação**:

1. **Dado** que um visitante fornece dados válidos de cadastro (nome, e-mail, senha), **Quando** ele envia a requisição de cadastro, **Então** um registro de usuário é criado, um identificador único é atribuído e os dados são persistidos de forma que sobrevivam a uma reinicialização do sistema.
2. **Dado** que já existe um usuário com determinado e-mail, **Quando** uma nova tentativa de cadastro é feita com o mesmo e-mail, **Então** o sistema rejeita a requisição e retorna uma mensagem de erro informativa.
3. **Dado** que os dados de um usuário cadastrado foram salvos, **Quando** o sistema é reiniciado, **Então** todos os dados do usuário continuam disponíveis e inalterados.

---

### História de Usuário 2 - Persistência de Dados de Pokémon (Prioridade: P2)

Como consumidor da API, quero criar e recuperar registros de Pokémon com conjuntos variados de atributos, para que novos Pokémon possam ser adicionados ao sistema sem exigir alterações estruturais na camada de armazenamento.

**Por que esta prioridade**: Pokémon são o objeto de dado principal desta API e seu esquema evolui frequentemente (novos tipos, habilidades, estatísticas). Esta história valida que dados de esquema flexível podem ser criados com atributos variados sem restringir a capacidade do sistema de lidar com designs futuros de Pokémon.

**Teste Independente**: Pode ser testada completamente criando dois Pokémon com conjuntos diferentes de atributos (ex: um com campo "mega_evolucao" e outro sem), recuperando ambos e confirmando que todos os atributos foram preservados exatamente como enviados.

**Cenários de Aceitação**:

1. **Dado** que um registro de Pokémon com um conjunto padrão de atributos é enviado, **Quando** a requisição de criação é processada, **Então** o Pokémon é persistido com um identificador único e todos os atributos fornecidos são armazenados sem alteração.
2. **Dado** que um registro de Pokémon com atributos não-padrão é enviado (ex: formas alternativas, variantes regionais, mecânicas de novos jogos), **Quando** a requisição de criação é processada, **Então** o sistema persiste todos os atributos sem rejeitar campos desconhecidos.
3. **Dado** que dois Pokémon existem com conjuntos diferentes de atributos, **Quando** ambos são recuperados, **Então** cada registro retorna exatamente os atributos com os quais foi criado, sem perda de dados.
4. **Dado** que um registro de Pokémon é solicitado por um identificador inválido ou inexistente, **Quando** a recuperação é tentada, **Então** o sistema retorna uma resposta clara de "não encontrado".

---

### História de Usuário 3 - Listagem e Consulta de Dados Persistidos (Prioridade: P3)

Como consumidor da API, quero recuperar listas de usuários e Pokémon para que eu possa navegar pelos dados armazenados entre sessões.

**Por que esta prioridade**: Acesso de leitura a coleções persistidas é essencial para qualquer cliente que consuma a API. Esta história valida que entidades de esquema fixo e de esquema flexível podem ser listadas de forma confiável após a persistência.

**Teste Independente**: Pode ser testada completamente criando múltiplos registros de cada tipo, listando-os e confirmando que a coleção retornada corresponde aos registros armazenados — entregando um conjunto de dados navegável de forma independente das funcionalidades de criação ou atualização.

**Cenários de Aceitação**:

1. **Dado** que múltiplos usuários existem no sistema, **Quando** uma lista de usuários é solicitada, **Então** todos os usuários armazenados são retornados contendo apenas identificador e nome — o e-mail não é incluído na resposta.
2. **Dado** que múltiplos Pokémon existem no sistema, **Quando** uma lista de Pokémon é solicitada, **Então** todos os Pokémon armazenados são retornados com seus conjuntos completos de atributos.
3. **Dado** que não existem registros para um tipo de entidade, **Quando** uma lista é solicitada, **Então** o sistema retorna uma coleção vazia (não um erro).

---

### Casos Extremos

- O que acontece quando um Pokémon é enviado sem nenhum atributo (corpo vazio)?
- Como o sistema lida com conjuntos de atributos extremamente grandes em um registro de Pokémon?
- O que acontece se a conexão com o banco de dados for perdida no meio de uma escrita — a operação é atômica?
- Nomes de Pokémon duplicados são permitidos; a unicidade é garantida pelo número do Pokédex, não pelo nome.
- O que acontece quando um registro de usuário é solicitado por um identificador inexistente?

## Requisitos *(obrigatório)*

### Requisitos Funcionais

- **RF-001**: O sistema DEVE persistir registros de usuários com uma estrutura estável e bem definida contendo no mínimo: identificador único, nome, endereço de e-mail e data/hora de criação.
- **RF-002**: O sistema DEVE rejeitar o cadastro de usuário caso o endereço de e-mail já esteja associado a um registro de usuário existente.
- **RF-003**: O sistema DEVE persistir registros de Pokémon com uma estrutura flexível que aceite qualquer conjunto de atributos fornecido no momento da criação, sem exigir alterações de esquema.
- **RF-003a**: Todo registro de Pokémon DEVE conter obrigatoriamente nome e número do Pokédex; requisições de criação sem esses campos DEVEM ser rejeitadas com erro informativo.
- **RF-003b**: O número do Pokédex DEVE ser único no sistema; tentativas de criar um Pokémon com número já existente DEVEM ser rejeitadas.
- **RF-004**: O sistema DEVE atribuir um identificador único e imutável a cada registro persistido (tanto usuários quanto Pokémon) no momento da criação.
- **RF-005**: O sistema DEVE garantir que os registros persistidos (de ambos os tipos) sobrevivam a reinicializações do serviço e permaneçam recuperáveis após a persistência.
- **RF-006**: O sistema DEVE permitir a recuperação de registros individuais pelo identificador único para ambos os tipos de entidade.
- **RF-007**: O sistema DEVE permitir a recuperação da coleção completa de registros para ambos os tipos de entidade.
- **RF-008**: O sistema DEVE retornar uma resposta clara de "não encontrado" quando um registro é solicitado por um identificador inexistente.
- **RF-009**: O sistema DEVE armazenar todos os atributos enviados para um registro de Pokémon sem descartar silenciosamente campos não reconhecidos.
- **RF-010**: Endpoints de escrita (cadastro de usuário e criação de Pokémon) DEVEM exigir autenticação válida do chamador; requisições não autenticadas DEVEM ser rejeitadas.
- **RF-011**: Endpoints de leitura (listagem e recuperação individual de usuários e Pokémon) DEVEM ser acessíveis sem autenticação.
- **RF-012**: A listagem de usuários DEVE retornar apenas identificador e nome; o endereço de e-mail NUNCA deve ser exposto em respostas públicas.

### Entidades Principais

- **Usuário**: Titular de uma conta cadastrada. Possui um conjunto fixo de atributos (identificador, nome, e-mail, credencial de senha, data/hora de criação). A unicidade é imposta pelo e-mail. O esquema não muda com novos lançamentos de jogos.
- **Pokémon**: Entrada de uma criatura no Pokédex. Possui atributos fixos obrigatórios: identificador interno (gerado pelo sistema), nome, número do Pokédex (número nacional, fornecido pelo criador e único no sistema) e tipos elementais (conjunto fechado, validado contra tabela mestra). Permite atributos adicionais arbitrários para acomodar mecânicas de jogo em evolução — como habilidades, estatísticas, formas e variantes regionais — sem exigir migração estrutural.

## Critérios de Sucesso *(obrigatório)*

### Resultados Mensuráveis

- **CS-001**: Uma operação de cadastro de usuário é concluída e o registro se torna recuperável em menos de 1 segundo sob carga normal (até 50 requisições simultâneas).
- **CS-002**: Uma operação de criação de Pokémon com qualquer conjunto de atributos é concluída e o registro se torna recuperável em menos de 1 segundo sob carga normal (até 50 requisições simultâneas).
- **CS-003**: 100% dos atributos enviados para um Pokémon são retornados inalterados na recuperação — sem perda de dados ou truncamento silencioso.
- **CS-004**: Todos os registros persistidos permanecem disponíveis após uma reinicialização completa do serviço, com zero perda de dados.
- **CS-005**: Cadastros com e-mail duplicado são rejeitados em 100% das tentativas com uma resposta clara e informativa.
- **CS-006**: O sistema suporta pelo menos 500 operações simultâneas de leitura/escrita sem corrupção de dados ou degradação de disponibilidade.

## Esclarecimentos

### Sessão 2026-05-17

- P: Quem pode chamar os endpoints de escrita e leitura? → R: Leituras (listagem e recuperação de usuários e Pokémon) são abertas a qualquer chamador. Escritas (cadastro de usuário e criação de Pokémon) exigem autenticação válida.
- P: Quais campos a listagem pública de usuários expõe? → R: Apenas identificador e nome; o e-mail é omitido por ser dado pessoal sensível.
- P: Quais atributos são obrigatórios na criação de um Pokémon? → R: Nome e número do Pokédex são obrigatórios; o número do Pokédex deve ser único no sistema.
- P: Deve haver garantia de consistência entre os dois armazenamentos? → R: Os bancos são tolerantes a falha parcial. A criação de um Pokémon grava no PostgreSQL (atributos fixos) e depois no MongoDB (atributos flexíveis). Se o MongoDB falhar, o registro existe no PostgreSQL com atributos fixos apenas — não é um erro; os atributos flexíveis podem ser adicionados depois. Não há transação distribuída entre os dois bancos.
- P: O que é "carga normal" nos critérios de desempenho? → R: Até 50 requisições simultâneas.

## Suposições

- Senhas de usuários são armazenadas de forma segura e não reversível; senhas brutas nunca são persistidas.
- O escopo inicial cobre criação e recuperação de usuários e Pokémon; operações de atualização e exclusão estão fora do escopo desta funcionalidade.
- Registros de Pokémon não impõem unicidade por nome — nomes duplicados são permitidos, a menos que seja explicitamente restringido em uma funcionalidade futura.
- A API já possui uma camada HTTP funcional; esta funcionalidade adiciona a camada de persistência abaixo dela.
- Paginação para endpoints de listagem está fora do escopo desta funcionalidade, mas é esperada em uma iteração futura.
- Autenticação (provar quem é o usuário ao fazer requisições) é uma funcionalidade separada; esta funcionalidade cobre apenas a persistência dos dados do usuário.
- Os atributos fixos do Pokémon (nome, tipos, evoluções) são persistidos no PostgreSQL; os atributos flexíveis (ataques, fraquezas, estatísticas, etc.) são persistidos no MongoDB com o mesmo UUID V7 como chave de correlação. Os dois armazenamentos são tolerantes a falha parcial — um Pokémon pode existir apenas no PostgreSQL sem atributos flexíveis.
- O domínio Usuário usa exclusivamente o PostgreSQL; não há operação transversal entre Usuário e Pokemon.
- Validação automatizada dos SLOs de latência (CS-001, CS-002) e do limiar de carga (CS-006) está fora do escopo desta funcionalidade; esses critérios serão verificados manualmente via `quickstart.md` com containers Docker reais e priorizados em uma feature futura de observabilidade e desempenho.
