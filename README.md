# Cafeteria – Projeto Spring Boot

Projeto acadêmico desenvolvido com **Spring Boot (Java 21)**, oferecendo API REST, frontend com Thymeleaf e autenticação JWT para a API. A interface web utiliza autenticação baseada em sessão.

---

## Visão Geral

Aplicação modelo para uma cafeteria com os seguintes recursos:

- **Backend:** Spring Boot (Java 21)
- **Autenticação:**  
  - JWT para endpoints da API (`/api/**`)  
  - Login via formulário para a interface web  
  - Endpoint de conversão JWT → Sessão (`/api/auth/session`)
- **Documentação:** Swagger / OpenAPI (springdoc)
- **Persistência:** H2 (memória) por padrão, com opção de MySQL
- **Segurança:** Duas filter chains (API e Web) e `DaoAuthenticationProvider`

---

## Pré-requisitos

- Java 21 instalado e `JAVA_HOME` configurado  
- Maven 3.8+  
- IDE opcional: IntelliJ, VS Code ou Eclipse  

---

## Configuração e Execução

### 1. Configuração do Ambiente

Defina a chave secreta JWT em: src/main/resources/application.properties:

# JWT (desenvolvimento)
jwt.secret=TroquePorUmaChaveSegura_MaisDe_32_Caracteres
jwt.expiration=3600000

# H2 (desenvolvimento)
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=password
spring.h2.console.enabled=true

# JPA
spring.jpa.hibernate.ddl-auto=update
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect

# Thymeleaf
spring.thymeleaf.cache=false


# Limpar e construir
mvn clean package

# Executar com Maven
mvn spring-boot:run

# Ou executar o JAR diretamente
java -jar target/cafeteria-0.0.1-SNAPSHOT.jar


## 3. Acessos

- **Aplicação Web:** `http://localhost:8080/`  
- **Login (Thymeleaf):** `http://localhost:8080/login`  
- **Swagger UI:** `http://localhost:8080/swagger-ui/index.html`  
- **OpenAPI JSON:** `http://localhost:8080/v3/api-docs`  
- **Console H2:** `http://localhost:8080/h2-console`

---

## Endpoints Principais

### Autenticação (API)

**POST** `/api/auth/login`  
Autenticação via JWT.

**Request (exemplo):**
```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "seu_usuario",
  "password": "sua_senha"
}
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 3600
}
```

## Documentação da API

- **Swagger UI:** `http://localhost:8080/swagger-ui/index.html`  
- **OpenAPI JSON:** `http://localhost:8080/v3/api-docs`

---

## Fluxo de Autenticação

1. Usuário preenche o formulário na interface (UI).  
2. O JavaScript envia uma requisição **POST** para `/api/auth/login`.  
3. Se as credenciais forem válidas, o servidor retorna um **JWT**.  
4. O JavaScript envia **POST** para `/api/auth/session` passando o token.  
5. O backend valida o JWT, cria o **SecurityContext** e persiste na sessão HTTP (`JSESSIONID`).  
6. O JavaScript redireciona para `/redirecionarPorRole`, e o Spring reconhece o usuário pela sessão criada.

## Contribuição

1. Realize um **fork** do projeto.  
2. Crie uma branch para sua feature:  
   ```bash
   git checkout -b feature/AmazingFeature
## Licença

Este projeto é destinado exclusivamente a fins educacionais.  
Desenvolvido como projeto acadêmico — Senac.









