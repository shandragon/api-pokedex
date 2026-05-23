<!-- Sync Impact Report:
Version change: 1.1.2 → 1.1.3
Modified principles: Governança — Idioma Oficial (adição da exceção para termos técnicos canônicos)
Added sections: N/A
Removed sections: N/A
Templates requiring updates: ✅ nenhum template referencia diretamente a política de idioma; nenhuma atualização necessária
Follow-up TODOs: N/A
-->
# Constituição da API Pokémon

## Princípios Fundamentais

### I. Qualidade em Primeiro Lugar
A qualidade do código é primordial. Cada alteração deve ser revisada quanto à manutenibilidade, legibilidade e adesão aos padrões de projeto estabelecidos. A refatoração é uma atividade contínua e esperada, não uma tarefa adiada.

### II. Desenvolvimento Orientado a Testes (TDD) (NÃO NEGOCIÁVEL)
O TDD é a metodologia de desenvolvimento primária. O ciclo "vermelho-verde-refatorar" é estritamente aplicado: escrever um teste com falha que define um requisito, implementar apenas o necessário para passar no teste e, em seguida, refatorar para melhorar o design. Nenhum código chega à produção sem passar pelos testes associados.

### III. Verificação Automatizada
Todas as funcionalidades e correções de erros devem ter cobertura de testes automatizados. Isso inclui testes unitários para lógica isolada e testes de integração para limites de contrato ou comunicação. Testes manuais são apenas para a validação final.

### IV. Design Incremental e Simplicidade
Siga os princípios YAGNI (Você Não Vai Precisar Disso) e KISS (Mantenha Simples, Estúpido). Projete sistemas incrementalmente com base nos requisitos atuais e testados. A complexidade deve ser explicitamente justificada e documentada.

### V. Interfaces Baseadas em Contrato
Todos os componentes devem interagir por meio de interfaces (contratos) bem definidas e documentadas. Garanta a compatibilidade com versões anteriores; alterações que quebrem a compatibilidade devem ser versionadas de acordo com o versionamento semântico (MAJOR.MINOR.PATCH) e documentadas nas notas de lançamento.

## Fluxo de Trabalho de Desenvolvimento
O desenvolvimento segue um processo rigoroso:
1. **Gitflow**: Use o fluxo de trabalho Gitflow para gerenciamento de ramificações (master/main, develop, feature/hotfix).
2. **Commits Semânticos**: Todos os commits DEVEM seguir a especificação de Conventional Commits (ex: `feat:`, `fix:`, `docs:`, `refactor:`, `test:`, `chore:`).
3. Requisitos e casos de teste definidos nas especificações.
4. Revisão por pares obrigatória para todas as alterações.
5. Verificação automatizada pelo pipeline de CI/CD antes da fusão (merge).

## Governança
Esta constituição substitui todas as outras práticas.
1. **Idioma Oficial**: O idioma oficial do projeto é o Português do Brasil (PT-BR). Toda a
   documentação, comentários de código e artefatos de projeto DEVEM seguir este padrão.
   **Exceção**: termos técnicos com nomenclatura canônica estabelecida em inglês DEVEM manter
   o nome original, sem tradução. Isso inclui: padrões de projeto (Repository, Service,
   Controller, Factory, Observer, Strategy, Builder), padrões arquiteturais (MVC, REST, CQRS,
   Event Sourcing), convenções de frameworks (Spring Boot, JPA, MongoDB) e siglas amplamente
   reconhecidas (API, DTO, UUID, TDD, CI/CD). Traduzir esses termos introduz ambiguidade e
   dificulta a consulta à documentação oficial das tecnologias envolvidas.
2. Emendas exigem documentação da alteração proposta, análise de impacto e aprovação.
3. Todos os pull requests e revisões devem verificar a conformidade com estes princípios.
4. Dívidas técnicas devem ser rastreadas e priorizadas.

**Versão**: 1.1.3 | **Ratificada**: 2026-04-26 | **Última Emenda**: 2026-05-23
