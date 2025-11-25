# Guia de Instalação - Sistema de Estágios UTFPR

## Pré-requisitos

### Backend
- **Java 21+** - [Download](https://www.oracle.com/java/technologies/downloads/#java21)
- **Maven 3.8+** - [Download](https://maven.apache.org/download.cgi)
- **MySQL 8.0+** (opcional, se usar banco de dados)

### Frontend
- **Node.js 18+** - [Download](https://nodejs.org/)
- **npm 9+** (geralmente vem com Node.js)
- **Angular CLI 17+**

### Ferramentas Auxiliares
- **Git** - [Download](https://git-scm.com/)
- **Visual Studio Code** (recomendado) - [Download](https://code.visualstudio.com/)

---

## Instalação do Backend

### 1. Navegue até o diretório do backend
```bash
cd BackEnd/estagio
```

### 2. Instale as dependências Maven
```bash
mvn clean install
```

### 3. Configure as variáveis de ambiente

Crie um arquivo `application.properties` em `src/main/resources/`:

```properties
# Configuração de Email
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=seu_email@gmail.com
spring.mail.password=sua_senha_app
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
spring.mail.properties.mail.smtp.starttls.required=true

# Google Sheets API (se necessário)
GOOGLE_APPLICATION_CREDENTIALS=caminho/para/seu/arquivo.json
```

### 4. Compile o projeto
```bash
mvn clean compile
```

### 5. Execute a aplicação
```bash
mvn spring-boot:run
```

A aplicação estará disponível em `http://localhost:8080`

---

## Instalação do Frontend

### 1. Navegue até o diretório do frontend
```bash
cd FrontEnd/estagio-utfpr
```

### 2. Instale as dependências npm
```bash
npm install
```

### 3. Configure o servidor backend

Edite o arquivo `src/environments/environment.ts`:

```typescript
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080/api'
};
```

### 4. Execute o servidor de desenvolvimento
```bash
ng serve
```

Ou use:
```bash
npm start
```

A aplicação estará disponível em `http://localhost:4200`


## Configuração de Tarefas Agendadas

O sistema possui duas tarefas agendadas que executam **todos os dias às 9:00 AM** (horário de Brasília):

### 1. Download Automático de Planilhas
- **Classe**: `ScheduledTaskService.executarDownloadPlanilhasDiariamente()`
- **Função**: Baixa todas as planilhas do Google Sheets
- **Horário**: 09:00:00 (cron: `0 0 9 * * *`)

### 2. Envio de Lembretes por Email
- **Classe**: `ScheduledTaskService.executarEnvioLembretesDiariamente()`
- **Função**: Envia lembretes de relatórios pendentes
- **Horário**: 09:00:00 (cron: `0 0 9 * * *`)

Para alterar o horário, edite `src/main/java/com/utfpr/estagio/service/ScheduledTaskService.java` e modifique o atributo `cron` das anotações `@Scheduled`.

---

## Configuração de Email

O sistema usa **Gmail SMTP** para envio de emails. Para configurar:

### 1. Habilite Autenticação de Dois Fatores no Gmail
Acesse: https://myaccount.google.com/security

### 2. Gere uma Senha de App
Acesse: https://myaccount.google.com/apppasswords

### 3. Configure no `application.properties`
```properties
spring.mail.username=seu_email@gmail.com
spring.mail.password=sua_senha_app_de_16_caracteres
```

## Licença

Este projeto é parte do trabalho de estágio da UTFPR.

