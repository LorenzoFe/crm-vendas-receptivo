# crm-vendas-receptivo
CRM de Vendas

## Requisitos
- JDK 21 (ou mais novo)
- PostgreSQL com um banco `crm_vendas` (pode estar vazio)
- Não precisa instalar o Maven: use o wrapper `mvnw`

## Configuração
Senhas e segredos **não ficam no repositório**. Cada desenvolvedor cria seu arquivo local:

1. Copie `src/main/resources/application-local.example.yml` para `src/main/resources/application-local.yml`
2. Preencha com a senha do seu PostgreSQL e um segredo JWT (mínimo 32 caracteres)

O `application-local.yml` está no `.gitignore` e é carregado automaticamente.

## Banco de dados
Ao subir, a aplicação cria as tabelas que faltarem (`db/schema.sql`) e os dados iniciais (`db/dados_iniciais.sql`):
permissões, roles `ADMIN`, `GERENTE`, `VENDEDOR` e os estágios do funil. Os scripts podem rodar sempre sem duplicar nada.

Se o usuário admin ainda não existir, ele é criado com `ADMIN_EMAIL` / `ADMIN_PASSWORD`
(padrão local: `admin@crm.com` / `senha123`).

## Rodando
```bash
./mvnw spring-boot:run
```
No Windows (cmd/PowerShell): `mvnw.cmd spring-boot:run`

Swagger: http://localhost:8080/swagger-ui.html (faça login em `/auth/login` e use o botão **Authorize**)

## Variáveis de ambiente
| Variável | Para quê | Padrão |
|---|---|---|
| `DB_URL` ou `DB_HOST` + `DB_PORT` + `DB_NAME` | Endereço do PostgreSQL | `localhost:5432/crm_vendas` |
| `DB_USERNAME` / `DB_PASSWORD` | Usuário e senha do banco | `postgres` / - |
| `JWT_SECRET` | Segredo do token (32+ caracteres) | - |
| `ADMIN_EMAIL` / `ADMIN_PASSWORD` | Admin criado na primeira subida | `admin@crm.com` / `senha123` |
| `CORS_ALLOWED_ORIGINS` | Endereços do front, separados por vírgula | `http://localhost:5173,http://localhost:3000` |
| `PORT` | Porta HTTP (o Render define sozinho) | `8080` |
| `LOG_LEVEL` | Nível de log do pacote da aplicação | `DEBUG` |

## Deploy no Render
O arquivo `render.yaml` cria o PostgreSQL e a API (Docker) já ligados:

1. No Render: **New → Blueprint** e selecione este repositório
2. Informe `CORS_ALLOWED_ORIGINS` quando pedido (pode deixar em branco até o front existir)
3. A senha do admin é gerada automaticamente: veja em **crm-vendas-api → Environment → ADMIN_PASSWORD**
