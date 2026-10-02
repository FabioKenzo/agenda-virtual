# 📅 Agenda Virtual Escolar

Sistema Full Stack desenvolvido para atender uma necessidade real de uma instituição de ensino, com o objetivo de centralizar e facilitar a comunicação entre a escola e os responsáveis pelos alunos.

Por questões de privacidade, o nome da instituição cliente não é divulgado neste repositório.

A aplicação permite que responsáveis acompanhem seus alunos, eventos escolares e comunicados publicados pela escola. Administradores possuem acesso às funcionalidades de gerenciamento de eventos e comunicados.

O projeto está sendo desenvolvido com **Java e Spring Boot no backend** e **Angular no frontend**, aplicando conceitos utilizados no desenvolvimento de aplicações reais, como API REST, autenticação JWT, autorização baseada em roles, migrations de banco de dados, validação, tratamento de exceções, documentação da API e testes automatizados.

---

## 🚧 Status do projeto

### Backend

🟢 **Backend V1 funcional**

Funcionalidades implementadas:

- Cadastro de responsáveis e seus alunos
- Autenticação stateless utilizando JWT
- Controle de acesso por roles (`ADMIN` e `RESPONSAVEL`)
- Armazenamento seguro de senhas utilizando hash
- CRUD de eventos para administradores
- Associação de alunos aos eventos
- CRUD de comunicados para administradores
- Consulta dos alunos vinculados ao responsável autenticado
- Consulta dos eventos associados aos alunos do responsável
- Consulta de comunicados ativos pelos responsáveis
- Validação de dados e regras de negócio
- Tratamento global de exceções
- Documentação da API com Swagger/OpenAPI
- Testes unitários e de segurança

Atualmente o backend possui **39 testes automatizados executados com sucesso**.

### Frontend

🟡 **Próxima etapa**

O frontend será desenvolvido utilizando **Angular, TypeScript e Bootstrap**, consumindo a API REST desenvolvida no backend.

---

## 🎯 Objetivo

A Agenda Virtual Escolar surgiu a partir de uma necessidade real de melhorar a comunicação entre uma instituição de ensino e os responsáveis pelos alunos.

A proposta é disponibilizar em um único sistema informações importantes do cotidiano escolar, como eventos e comunicados, permitindo que cada responsável visualize os dados relacionados aos seus alunos.

Além de solucionar uma necessidade real do cliente, o projeto também busca aplicar boas práticas de desenvolvimento de software e consolidar conhecimentos em desenvolvimento Full Stack.

---

## 🛠️ Tecnologias

### Backend

- Java 21
- Spring Boot
- Spring Web / Spring MVC
- Spring Data JPA
- Spring Security
- JWT
- Bean Validation
- Maven

### Banco de dados

- PostgreSQL 16
- Flyway

### Testes

- JUnit
- Mockito
- MockMvc
- Spring Security Test

### Infraestrutura e documentação

- Docker
- Docker Compose
- Swagger / OpenAPI
- Git
- GitHub

### Frontend

- Angular
- TypeScript
- HTML
- CSS
- Bootstrap

---

## 📐 System Design

Antes do desenvolvimento da aplicação foi elaborado um **System Design** para definir a arquitetura e os principais fluxos do sistema.

A documentação inclui:

- Visão geral da arquitetura
- Estrutura planejada do frontend Angular
- Arquitetura do backend Spring Boot
- Modelo do banco de dados
- Fluxo de autenticação com JWT
- Fluxo de cadastro e gerenciamento de eventos

Os diagramas e arquivos relacionados ao System Design estão disponíveis em:

```text
docs/system-design
```

---

## 🏗️ Arquitetura do Backend

O backend foi desenvolvido utilizando uma arquitetura em camadas, separando as responsabilidades da aplicação.

Fluxo principal:

```text
Cliente
   │
   ▼
Controller
   │
   ▼
Service
   │
   ▼
Repository
   │
   ▼
PostgreSQL
```

### Controller

Responsável por receber as requisições HTTP, validar os dados de entrada e retornar as respostas da API.

### Service

Contém as regras de negócio da aplicação e coordena as operações entre controllers e repositories.

### Repository

Responsável pelo acesso e persistência dos dados utilizando Spring Data JPA.

### DTO

Utilizado para definir os contratos de entrada e saída da API, evitando a exposição direta das entidades JPA.

### Security

Responsável pela autenticação JWT, carregamento dos usuários e configuração das regras de autorização.

### Exception

Centraliza exceções específicas da aplicação e o tratamento global dos erros retornados pela API.

---

## 🔐 Autenticação e autorização

A API utiliza **Spring Security + JWT** para autenticação e autorização.

A autenticação é stateless, portanto o servidor não mantém uma sessão de usuário.

Fluxo simplificado:

```text
Login
  │
  ▼
E-mail + Senha
  │
  ▼
Spring Security
  │
  ▼
Credenciais válidas
  │
  ▼
JWT
  │
  ▼
Authorization: Bearer <token>
  │
  ▼
JwtAuthenticationFilter
  │
  ▼
Acesso ao recurso protegido
```

O token JWT identifica o usuário autenticado e é validado a cada requisição protegida.

Tokens inválidos ou expirados resultam em:

```text
401 Unauthorized
```

Usuários autenticados que tentam acessar recursos incompatíveis com sua role recebem:

```text
403 Forbidden
```

---

## 👥 Perfis de acesso

A aplicação possui dois perfis principais.

### RESPONSAVEL

Pode acessar:

- Seus alunos vinculados
- Eventos associados aos seus alunos
- Comunicados ativos publicados pela escola

### ADMIN

Pode administrar:

- Eventos
- Associação de alunos aos eventos
- Comunicados

As rotas administrativas são protegidas e não podem ser acessadas pelo perfil `RESPONSAVEL`.

---

## 📡 Principais endpoints

### Autenticação

| Método | Endpoint | Descrição | Acesso |
|---|---|---|---|
| POST | `/auth/register` | Cadastro de responsável e alunos | Público |
| POST | `/auth/login` | Autenticação e geração do JWT | Público |

### Responsável

| Método | Endpoint | Descrição | Acesso |
|---|---|---|---|
| GET | `/responsavel/alunos` | Lista os alunos do responsável autenticado | RESPONSAVEL |
| GET | `/responsavel/eventos` | Lista eventos associados aos alunos do responsável | RESPONSAVEL |
| GET | `/responsavel/comunicados` | Lista os comunicados ativos | RESPONSAVEL |

### Eventos

| Método | Endpoint | Descrição | Acesso |
|---|---|---|---|
| GET | `/events` | Lista os eventos | ADMIN |
| GET | `/events/{id}` | Busca um evento pelo ID | ADMIN |
| POST | `/events` | Cria um evento | ADMIN |
| PUT | `/events/{id}` | Atualiza um evento | ADMIN |
| DELETE | `/events/{id}` | Exclui um evento | ADMIN |

### Comunicados

| Método | Endpoint | Descrição | Acesso |
|---|---|---|---|
| GET | `/announcements` | Lista os comunicados | ADMIN |
| GET | `/announcements/{id}` | Busca um comunicado pelo ID | ADMIN |
| POST | `/announcements` | Cria um comunicado | ADMIN |
| PUT | `/announcements/{id}` | Atualiza um comunicado | ADMIN |
| DELETE | `/announcements/{id}` | Exclui um comunicado | ADMIN |

---

## 📚 Swagger / OpenAPI

A API possui documentação interativa utilizando **Swagger/OpenAPI**.

Com o backend executando localmente, acesse:

```text
http://localhost:8080/swagger-ui/index.html
```

A documentação permite visualizar os endpoints disponíveis, seus contratos de entrada e saída e testar as requisições diretamente pelo navegador.

Para testar endpoints protegidos:

1. Realize o login em `/auth/login`
2. Copie o token JWT retornado
3. Clique em **Authorize**
4. Informe o token
5. Execute os endpoints protegidos

> Após o deploy da aplicação, o endereço público da documentação poderá ser disponibilizado nesta seção.

---

## 🗄️ Banco de dados

O projeto utiliza **PostgreSQL 16** como banco de dados relacional.

Entre as principais estruturas estão:

- Usuários
- Alunos
- Eventos
- Comunicados
- Associação entre eventos e alunos

As alterações estruturais do banco são controladas através do **Flyway**, garantindo o versionamento das migrations.

As migrations ficam em:

```text
src/main/resources/db/migration
```

O Hibernate é configurado para validar o schema existente, enquanto a evolução da estrutura do banco é responsabilidade do Flyway.

---

## 🐳 Docker

O PostgreSQL pode ser executado localmente utilizando **Docker Compose**, facilitando a criação e padronização do ambiente de desenvolvimento.

Para iniciar os containers:

```bash
docker compose up -d
```

Para verificar os containers:

```bash
docker compose ps
```

Para encerrar:

```bash
docker compose down
```

---

## ▶️ Executando o backend localmente

### Pré-requisitos

- Java 21
- Maven
- Docker
- Docker Compose
- Git

### 1. Clone o repositório

```bash
git clone https://github.com/FabioKenzo/agenda-virtual.git
```

Entre na pasta do projeto:

```bash
cd agenda-virtual
```

### 2. Configure o ambiente local

Configure localmente as credenciais necessárias para acesso ao banco de dados e as propriedades utilizadas pela autenticação JWT.

> Credenciais, chaves e demais informações sensíveis não são versionadas no repositório.

### 3. Inicie o banco de dados

```bash
docker compose up -d
```

### 4. Execute os testes

```bash
mvn test
```

### 5. Execute a aplicação

```bash
mvn spring-boot:run
```

A API estará disponível em:

```text
http://localhost:8080
```

A documentação Swagger estará disponível em:

```text
http://localhost:8080/swagger-ui/index.html
```

---

## 🧪 Testes automatizados

O backend possui atualmente **39 testes automatizados**.

Os testes cobrem regras importantes da aplicação, incluindo:

- Cadastro de responsáveis
- Codificação de senha
- Cadastro e associação de alunos
- Criação e atualização de eventos
- Associação de alunos aos eventos
- Validação de horários dos eventos
- Busca e exclusão de eventos
- CRUD de comunicados
- Consulta dos dados do responsável
- Consulta de comunicados ativos
- Autorização baseada em roles
- Bloqueio de endpoints administrativos
- Requisições sem autenticação
- Tokens JWT inválidos
- Tokens JWT expirados

Para executar toda a suíte:

```bash
mvn test
```

Resultado esperado no estado atual do projeto:

```text
Tests run: 39, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

---

## ⚠️ Tratamento de erros

A API possui tratamento global de exceções para padronizar erros retornados ao cliente.

Entre os cenários tratados estão:

- Recurso não encontrado
- Dados inválidos
- Falhas de validação
- JSON inválido
- Violações de regras de negócio
- Acesso sem autenticação
- Acesso sem autorização
- JWT inválido ou expirado

Entre os principais status HTTP utilizados estão:

```text
400 Bad Request
401 Unauthorized
403 Forbidden
404 Not Found
```

---

## 📂 Organização do projeto

O backend segue uma separação de responsabilidades entre as principais camadas da aplicação:

```text
src/main/java/br/com/kenzowebstudio/agenda_virtual
│
├── config
├── controller
├── dto
├── exception
├── model
├── repository
├── security
└── service
```

Os testes automatizados ficam em:

```text
src/test/java/br/com/kenzowebstudio/agenda_virtual
```

---

## 🗺️ Roadmap

- [x] Levantamento de requisitos
- [x] System Design
- [x] Modelagem do banco de dados
- [x] Configuração do PostgreSQL com Docker
- [x] Migrations com Flyway
- [x] Cadastro de responsáveis e alunos
- [x] Autenticação JWT
- [x] Autorização por roles
- [x] CRUD de eventos
- [x] CRUD de comunicados
- [x] Área de consulta do responsável
- [x] Swagger / OpenAPI
- [x] Testes automatizados do backend
- [x] Revisão de segurança e regras de negócio
- [ ] Desenvolvimento do frontend Angular
- [ ] Integração frontend e backend
- [ ] Preparação do ambiente de produção
- [ ] Deploy da aplicação
- [ ] CI/CD

---

## 🔒 Privacidade

Este projeto está sendo desenvolvido a partir de uma necessidade real de um cliente.

Por questões de privacidade, informações que possam identificar diretamente a instituição de ensino, seus responsáveis ou alunos não são disponibilizadas publicamente neste repositório.

Dados utilizados durante o desenvolvimento e os testes são fictícios.

---

## 👨‍💻 Autor

Desenvolvido por **Fabio Kenzo Okamura da Silva**.

Projeto Full Stack desenvolvido utilizando Java, Spring Boot, PostgreSQL e Angular.
