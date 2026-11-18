# Relatório Final — Córtex

Responsável: Gabriel · Redigido em 28/10/2026 · Atualizado após a entrega e revisado pelo grupo em 03/11/2026

**Sistema:** https://crm-vendas-front.onrender.com · **API (Swagger):** https://crm-vendas-api-0yan.onrender.com/swagger-ui.html

## Planejado x entregue
Comparação com o Termo de Abertura (30/08/2026).

| Planejado | Entregue | Situação |
|---|---|---|
| Banco PostgreSQL modelado (CP-1) | 17 tabelas, scripts idempotentes, criação automática na subida | ✅ |
| Cadastros de empresas, contatos e produtos | API e telas completas | ✅ |
| Formulário público de interesse | Área do Cliente e pop-up nos planos da landing page, com aceite LGPD | ✅ |
| Integração com o discador Vonix | Envio automático do interesse à fila assim que é registrado, situação da ligação na tela de Interessados e reenvio | ✅ |
| Funil configurável com kanban | Estágios em tabela, kanban com arrastar e soltar | ✅ |
| Oportunidades, propostas, tarefas, interações, notas e histórico | API e telas completas; proposta aceita move a oportunidade para o estágio final | ✅ |
| Indicadores de desempenho do vendedor | Painel com conversão, valor em aberto, total vendido, ticket médio e ciclo de venda | ✅ |
| Autenticação e perfis ADMIN/GERENTE/VENDEDOR | JWT, todas as rotas protegidas e permissões por endpoint | ✅ |
| Sistema hospedado | API, banco e front no Render | ✅ |
| Entregas a cada checkpoint (CP-2 a CP-4) | Unificadas em 29/10 por decisão do professor (01/10) | 🔄 |
| Release versionada | Tag `v1.0.0` nos dois repositórios | ✅ |

**Além do planejado:** landing page do Córtex com planos e FAQ, tratamento global de erros com status HTTP padronizados e fuso de São Paulo nos registros.

**Escopo inicial retirado:** o copiloto de IA e o modo de telefonia simulada do documento de escopo inicial saíram em 21/08. A IA ficou como trabalho futuro, e a telefonia passou a usar o Vonix real.

## Distribuição do trabalho
| Membro | Principais entregas |
|---|---|
| Lorenzo | DDL de acesso; base da API, autenticação, usuários, cadastros, segurança e padronização; publicação em nuvem; base e telas internas do front; revisão dos PRs |
| Gabriel | Dados iniciais e queries do CP-1; módulo de vendas da API (oportunidades, pipeline, propostas, tarefas, interações, notas, histórico); cronograma, board, testes manuais e relatório final |
| Luiz Felipe | Modelo ER e DDL comercial; interesses e conversão em oportunidade; integração com o Vonix; ajustes no Swagger; Área do Cliente e Interessados no front |
| Moises | Ideia e Termo de Abertura; modelo ER; atas; landing page do Córtex e identidade visual |

## Lições aprendidas
- **Começar o código cedo:** o código só começou em 29/09 e o front em 23/10, o que concentrou commits na reta final. Entregas internas semanais teriam distribuído melhor o esforço.
- **O banco bem modelado pagou o investimento:** o `schema.sql` do CP-1 virou a base direta das entidades e foi reaproveitado na criação automática em produção.
- **Publicar cedo evita surpresas:** a API foi para o Render em 19/10, o que revelou o problema de fuso horário antes da apresentação.
- **Integração externa exige isolamento:** manter o Vonix em um módulo próprio, configurado por variáveis de ambiente, permitiu desenvolver e testar sem travar o restante do sistema.
- **Branches e PRs facilitam o trabalho paralelo:** os quatro membros trabalharam em módulos diferentes ao mesmo tempo, com integração pelos PRs.
- **Escopo realista:** retirar a IA no início permitiu entregar o fluxo principal completo e funcionando.
