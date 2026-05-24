# Feature Specification: User Login

**Feature Branch**: `feature/003-user-login`

**Created**: 2026-05-23

**Status**: Draft

**Input**: User description: "O sistema deve permite que um usuário cadastrado efetue login usando seu email e senha."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Login com Credenciais Válidas (Priority: P1)

Um usuário já cadastrado acessa a tela de login, informa seu e-mail e senha corretos e obtém acesso autenticado ao sistema.

**Why this priority**: É o fluxo principal da feature — sem ele nenhum outro cenário tem valor.

**Independent Test**: Pode ser validado de forma isolada: basta ter um usuário cadastrado e verificar que, ao fornecer as credenciais corretas, o sistema concede acesso.

**Acceptance Scenarios**:

1. **Given** um usuário registrado com e-mail `joao@example.com` e senha válida, **When** ele submete o formulário de login com essas credenciais, **Then** o sistema autentica o usuário, gerando um token de acesso e redireciona para a área logada.
2. **Given** o usuário está autenticado, **When** ele acessa qualquer página protegida, **Then** o sistema permite o acesso sem solicitar login novamente enquanto o token estiver válido.

---

### User Story 2 - Login com Credenciais Inválidas (Priority: P1)

Um usuário tenta fazer login com e-mail inexistente ou senha incorreta e recebe uma mensagem de erro informativa sem expor qual dado está errado.

**Why this priority**: Segurança básica — impede enumeração de usuários e protege contas cadastradas.

**Independent Test**: Testável isoladamente: submeter credenciais inválidas e verificar que o acesso é negado com mensagem genérica.

**Acceptance Scenarios**:

1. **Given** qualquer combinação de e-mail/senha inválida, **When** o usuário submete o formulário, **Then** o sistema nega o acesso e exibe a mensagem "E-mail ou senha inválidos" sem indicar qual campo está incorreto.
2. **Given** o formulário com campos em branco, **When** o usuário tenta submeter, **Then** o sistema bloqueia a submissão e solicita o preenchimento dos campos obrigatórios.
3. **Given** um e-mail com formato inválido (ex.: sem `@`), **When** o usuário tenta submeter, **Then** o sistema rejeita antes do envio e informa que o formato do e-mail é inválido.

---

### User Story 3 - Bloqueio por Tentativas Excessivas (Priority: P2)

Após múltiplas tentativas de login fracassadas seguidas, o sistema bloqueia temporariamente novas tentativas para a mesma conta, protegendo contra ataques de força bruta.

**Why this priority**: Segurança adicional relevante, mas o sistema ainda entrega valor sem este controle no primeiro ciclo.

**Independent Test**: Testável de forma independente: realizar N tentativas inválidas consecutivas e verificar que tentativas subsequentes são bloqueadas temporariamente.

**Acceptance Scenarios**:

1. **Given** 5 tentativas de login inválidas consecutivas para o mesmo e-mail, **When** uma nova tentativa ocorre antes do período de bloqueio expirar, **Then** o sistema recusa o acesso e informa que a conta está temporariamente bloqueada.
2. **Given** a conta bloqueada temporariamente, **When** o período de bloqueio expira, **Then** o sistema volta a aceitar tentativas de login normalmente.

---

### User Story 4 - Encerramento de Sessão (Priority: P2)

O usuário autenticado pode encerrar sua sessão explicitamente, garantindo que nenhum acesso indevido ocorra após o uso.

**Why this priority**: Complementa o login; sem logout, a sessão só expira automaticamente.

**Independent Test**: Testável isoladamente: usuário autenticado aciona logout e verifica que não consegue mais acessar áreas protegidas.

**Acceptance Scenarios**:

1. **Given** um usuário autenticado, **When** ele aciona a ação de logout, **Then** a sessão é encerrada e qualquer acesso subsequente a áreas protegidas redireciona para a tela de login.
2. **Given** uma sessão encerrada via logout, **When** o usuário tenta reutilizar o token/cookie de sessão anterior, **Then** o sistema rejeita o acesso.

---

### Edge Cases

- **Conta desativada**: O sistema exibe a mesma mensagem genérica "E-mail ou senha inválidos", sem revelar que a conta existe, prevenindo enumeração de contas.
- Como o sistema se comporta se a sessão expirar enquanto o usuário está ativo?
- O que ocorre se o usuário enviar o formulário múltiplas vezes simultaneamente (double submit)?
- Como tratar e-mails com letras maiúsculas/minúsculas misturadas (ex.: `Joao@Example.com`)?

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: O sistema DEVE permitir que um usuário cadastrado se autentique fornecendo seu e-mail e senha.
- **FR-002**: O sistema DEVE validar o formato do e-mail antes de processar a autenticação.
- **FR-003**: O sistema DEVE tratar endereços de e-mail de forma case-insensitive durante a autenticação.
- **FR-004**: O sistema DEVE negar o acesso quando as credenciais fornecidas não corresponderem a um usuário ativo, inclusive quando o e-mail pertencer a uma conta desativada.
- **FR-005**: O sistema DEVE exibir a mensagem genérica "E-mail ou senha inválidos" para qualquer falha de autenticação — credenciais incorretas, conta inexistente ou conta desativada — sem indicar qual condição específica ocorreu.
- **FR-006**: O sistema DEVE bloquear temporariamente novas tentativas de login após 5 falhas consecutivas para o mesmo e-mail.
- **FR-007**: O sistema DEVE emitir um token de autenticação stateless após login bem-sucedido, permitindo que o usuário acesse recursos protegidos sem reautenticação durante a validade do token.
- **FR-008**: O sistema DEVE permitir que o usuário encerre explicitamente sua sessão (logout), invalidando somente o token do dispositivo atual; tokens de outros dispositivos do mesmo usuário permanecem válidos.
- **FR-009**: O sistema DEVE manter um mecanismo de invalidação de tokens revogados, garantindo que o token descartado via logout seja rejeitado mesmo dentro do prazo de validade original.
- **FR-012**: O sistema DEVE suportar que o mesmo usuário possua múltiplos tokens ativos simultaneamente (acesso de diferentes dispositivos).
- **FR-010**: O sistema DEVE definir um TTL fixo para cada token emitido; após esse prazo, o token expira automaticamente independentemente do uso, e o usuário deve autenticar-se novamente.
- **FR-011**: O sistema DEVE registrar cada evento de login (sucesso e falha) contendo: timestamp, identificador da conta e endereço IP de origem, para fins de auditoria e detecção de ataques.

### Key Entities

- **Usuário**: Entidade previamente cadastrada, identificada por e-mail único; possui status ativo/inativo.
- **Credenciais**: Par e-mail + senha fornecido no momento do login; a senha nunca é armazenada em texto plano.
- **Token de Autenticação**: Credencial stateless emitida após login bem-sucedido; contém identidade do usuário e prazo de validade; pode ser revogado explicitamente via logout; múltiplos tokens por usuário são suportados (um por dispositivo/cliente).
- **Tentativa de Login**: Registro de cada tentativa de autenticação, usado para controle de bloqueio por força bruta.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Usuários com credenciais válidas conseguem completar o processo de login em menos de 3 segundos sob carga normal.
- **SC-002**: 100% das tentativas de login com credenciais inválidas são negadas e retornam mensagem genérica de erro.
- **SC-003**: Após 5 tentativas falhas consecutivas, 100% das novas tentativas para aquele e-mail são bloqueadas pelo período definido.
- **SC-004**: Após logout, 0% das requisições com tokens/cookies de sessão anteriores têm acesso permitido.
- **SC-005**: 95% dos usuários autenticados conseguem acessar a área protegida sem ser solicitados a fazer login novamente durante a validade da sessão.
- **SC-006**: Todos os eventos de login (sucesso e falha) são registrados com timestamp, identificador da conta e endereço IP de origem; 0% de eventos de login ocorrem sem registro.

## Clarifications

### Session 2026-05-23

- Q: Qual comportamento/mensagem exibir quando uma conta desativada tenta fazer login? → A: Exibir a mesma mensagem genérica "E-mail ou senha inválidos", sem revelar que a conta existe (prevenção de enumeração de contas).
- Q: O sistema suporta sessões simultâneas em múltiplos dispositivos? Qual modelo de autenticação? → A: Padrão RESTful — autenticação stateless via token; cada dispositivo possui token independente, sessões simultâneas ilimitadas suportadas naturalmente.
- Q: O logout revoga apenas o token atual ou todos os tokens ativos do usuário? → A: Apenas o token do dispositivo atual é revogado; outras sessões ativas em outros dispositivos permanecem intactas.
- Q: A expiração do token é por TTL fixo desde a emissão ou por inatividade? → A: TTL fixo desde a emissão — token expira após duração fixa independentemente do uso, consistente com modelo stateless RESTful.
- Q: Quais dados devem constar nos registros de auditoria de eventos de login? → A: Padrão — timestamp + identificador da conta + resultado (sucesso/falha) + endereço IP de origem.

## Assumptions

- Usuários já possuem cadastro prévio no sistema; o cadastro (registro) de novos usuários está fora do escopo desta feature.
- A recuperação de senha ("esqueci minha senha") está fora do escopo desta feature.
- O sistema opera sobre HTTPS em produção, garantindo a confidencialidade das credenciais em trânsito.
- E-mails são únicos por usuário no sistema.
- O período de bloqueio por tentativas excessivas será definido durante o planejamento técnico (padrão razoável: 15 minutos).
- O TTL do token de autenticação será definido durante o planejamento técnico (padrão razoável: 1 hora a partir da emissão).
- Autenticação por terceiros (OAuth2, SSO, login social) está fora do escopo desta feature.
- O sistema utiliza modelo de autenticação stateless (padrão RESTful): tokens emitidos pelo servidor são verificados sem consulta a estado de sessão centralizado, exceto para checagem de tokens revogados.
- O armazenamento de endereços IP nos logs de auditoria deve ser divulgado na política de privacidade do sistema (relevante para conformidade com LGPD).
