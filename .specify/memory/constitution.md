<!-- Sync Impact Report:
Version change: 1.1.1 → 1.1.2
Modified principles: Tradução total para PT-BR
Added sections: N/A
Removed sections: N/A
Templates requiring updates: ⚠ pendente
Follow-up TODOs: Verificar conformidade de todos os arquivos do projeto com o padrão PT-BR.
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
1. **Idioma Oficial**: O idioma oficial do projeto é o Português do Brasil (PT-BR). Toda a documentação, comentários de código e artefatos de projeto DEVEM seguir este padrão.
2. Emendas exigem documentação da alteração proposta, análise de impacto e aprovação.
3. Todos os pull requests e revisões devem verificar a conformidade com estes princípios.
4. Dívidas técnicas devem ser rastreadas e priorizadas.

**Versão**: 1.1.2 | **Ratificada**: 2026-04-26 | **Última Emenda**: 2026-04-26
