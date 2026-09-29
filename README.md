# crm-vendas-receptivo
CRM de Vendas

## Requisitos
- JDK 21 (ou mais novo)
- PostgreSQL com o banco `crm_vendas` criado (schema via script SQL)
- Não precisa instalar o Maven: use o wrapper `mvnw`

## Configuração
Senhas e segredos **não ficam no repositório**. Cada desenvolvedor cria seu arquivo local:

1. Copie `src/main/resources/application-local.example.yml` para `src/main/resources/application-local.yml`
2. Preencha com a senha do seu PostgreSQL e um segredo JWT (mínimo 32 caracteres)

O `application-local.yml` está no `.gitignore` e é carregado automaticamente.

Alternativa: definir as variáveis de ambiente `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET` e `SERVER_PORT`.

## Rodando
```bash
./mvnw spring-boot:run
```
No Windows (cmd/PowerShell): `mvnw.cmd spring-boot:run`

Swagger: http://localhost:8080/swagger-ui.html
