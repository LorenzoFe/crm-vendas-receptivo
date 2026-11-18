# Termo de Abertura do Projeto — Córtex

| | |
|---|---|
| **Projeto** | Córtex — CRM de Vendas Receptivo |
| **Disciplina** | VIA231 — Projetos Integrados 2 · Prof. Romualdo Mathias Filho · Uniube 2026/2 |
| **Período** | 21/08/2026 a 29/10/2026 (checkpoints) · AMOSTRATEC em 18/11/2026 |
| **Versão** | 1.0 — aprovada em 30/08/2026 (redação: Moises · revisão: Gabriel) |
| **Repositórios** | [crm-vendas-receptivo](https://github.com/LorenzoFe/crm-vendas-receptivo) (API) · [crm-vendas-front](https://github.com/LorenzoFe/crm-vendas-front) (front) |

## 1. Problema
Empresas que vendem a partir de leads que chegam por conta própria (formulário do site, campanhas, indicação) perdem vendas pela demora no primeiro contato: o pedido fica parado em planilhas ou e-mails, ninguém sabe quem deve ligar e o histórico do cliente se perde. Quanto mais tempo o lead espera, menor a chance de conversão.

## 2. Objetivo
**Geral:** desenvolver um CRM web para operações de vendas receptivas (inbound) que capte o interesse do cliente, coloque esse contato automaticamente na fila de um discador para retorno rápido e acompanhe a negociação até o fechamento.

**Específicos:**
- Captar leads por um formulário público e por cadastro manual.
- Enviar automaticamente cada novo interesse para a fila do discador **Vonix Fone**, acompanhando a situação da ligação.
- Converter o interesse em oportunidade e conduzi-la por um funil de vendas configurável (kanban).
- Registrar propostas, tarefas, interações, notas e o histórico de cada oportunidade.
- Exibir indicadores de vendas por vendedor.
- Controlar o acesso por perfil de usuário.

## 3. Escopo
**Dentro do escopo**
- Cadastros de empresas, contatos e produtos.
- Formulário público de interesse (Área do Cliente) com aceite LGPD.
- Integração com o discador Vonix Fone: envio automático à fila e acompanhamento do resultado.
- Funil de vendas com estágios configuráveis e visão kanban.
- Oportunidades com itens, propostas, tarefas, interações, notas e histórico.
- Indicadores de desempenho do vendedor (conversão, valor em aberto, total vendido, ticket médio, ciclo de venda).
- Autenticação JWT e perfis **ADMIN**, **GERENTE** e **VENDEDOR**.
- Sistema hospedado em nuvem com banco de dados em produção.

**Fora do escopo**
- Copiloto de IA (resumo, transcrição e sugestões via LLM), previsto no escopo inicial e retirado em 21/08 para caber no semestre; fica como trabalho futuro.
- Discador preditivo próprio, central telefônica receptiva (URA/ACD) e gravação de chamadas — a telefonia fica a cargo do Vonix.
- Integração com WhatsApp, e-mail marketing e redes sociais.
- Aplicativo mobile nativo (o front será web responsivo).

## 4. Stack
| Camada | Tecnologia |
|---|---|
| Banco de dados | PostgreSQL |
| Backend | Java 21 · Spring Boot · Spring Data JPA · Spring Security + JWT · Swagger |
| Frontend | React · Vite · Ant Design · axios |
| Integração | API do discador Vonix Fone |
| Hospedagem | Render (API em Docker, PostgreSQL gerenciado e front estático) |
| Versionamento | GitHub com branches, Pull Requests e board de issues |

## 5. Membros e responsabilidades no CP-1
| Membro | Responsabilidade |
|---|---|
| Moises Melo | Ideia do projeto, Termo de Abertura, modelo ER (com Luiz Felipe), atas |
| Luiz Felipe Morais | Modelo ER, DDL do bloco comercial |
| Lorenzo de Almeida Ferreira | DDL do bloco de acesso, queries de teste |
| Gabriel | Dados iniciais, queries de teste, cronograma e board |

A divisão dos módulos de backend e frontend será definida no planejamento das etapas seguintes.

## 6. Entregas previstas
| CP | Data | Marco |
|---|---|---|
| CP-1 | 03/09 | Banco de dados |
| CP-2 | 17/09 | Backend — API CRUD |
| CP-3 | 01/10 | Integração Frontend + API |
| CP-4 | 15/10 | MVP completo |
| CP-5 | 29/10 | Hospedagem + README final |

## 7. Riscos e mitigação
| Risco | Mitigação |
|---|---|
| Prazo curto para CRM completo + integração | Priorizar o fluxo essencial: interesse → discador → oportunidade → proposta |
| Dependência da API externa do Vonix | Isolar a integração em um módulo próprio, com configuração por variáveis de ambiente |
| Concentração de trabalho em poucos membros | Divisão por módulos, branches por funcionalidade e revisão por PR |

## 8. Critérios de sucesso
- Fluxo principal funcionando de ponta a ponta: formulário → fila do discador → oportunidade → funil → proposta aceita.
- Sistema acessível por URL pública, com banco em produção.
- Documentação de gestão completa em `/docs` e README explicando como rodar o projeto.
