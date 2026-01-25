# 🗳️ API de Votação – Coop Votação

API REST desenvolvida em **Java 21 + Spring Boot** para gerenciamento de **pautas**, **sessões de votação**, **registro de votos** e **apuração de resultados**, seguindo boas práticas de arquitetura, REST, testes automatizados e documentação.

---

## 📌 Funcionalidades

- Criar pautas de votação
- Abrir sessão de votação (com duração configurável)
- Registrar votos (`SIM` / `NAO`)
- Validação opcional de CPF (configurável por ambiente)
- Apuração de resultado da votação
- Documentação via Swagger (OpenAPI)
- Testes unitários e de integração com cobertura

---

## 🧱 Arquitetura e Padrões

- Java 21
- Spring Boot 3.x
- Spring Data JPA
- Liquibase
- Oracle Database (produção)
- H2 (testes)
- DTOs separados de entidades
- Regras de negócio na camada Service
- Controllers enxutos
- Exceções mapeadas para HTTP
- Configuração por profiles
- Clock injetável para testes
- Jacoco para cobertura de testes

---

## 🔐 Validação de CPF

A validação de CPF é desacoplada e controlada por configuração:

```yaml
cpf-validation:
  enabled: true|false
```

- **Local/Testes:** desabilitada
- **Outros ambientes:** integração com serviço externo

Isso garante independência de serviços externos durante desenvolvimento e testes.

---

## 🚀 Como rodar o projeto

### Subir o banco Oracle (Docker)

```bash
docker-compose up -d oracle
```

### Executar a aplicação

```bash
./gradlew bootRun
```

Ou execute a classe `CoopVotocaoApiApplication` pela IDE.

---

## 🧪 Testes

### Executar testes

```bash
./gradlew test
```

### Relatório de cobertura

```bash
./gradlew jacocoTestReport
```

Relatório disponível em:

```
build/reports/jacoco/test/html/index.html
```

---

## 📄 Swagger

- Swagger UI  
  http://localhost:8080/swagger-ui.html

- OpenAPI JSON  
  http://localhost:8080/v3/api-docs

---

## 🔁 Endpoints

| Método | Endpoint |
|------|---------|
| POST | /api/v1/pautas |
| GET | /api/v1/pautas/{id} |
| POST | /api/v1/pautas/{id}/sessao |
| POST | /api/v1/pautas/{id}/votos |
| GET | /api/v1/pautas/{id}/resultado |

---

## 🛑 Códigos HTTP

| Código | Significado |
|------|------------|
| 201 | Criado |
| 200 | Sucesso |
| 400 | Dados inválidos |
| 404 | Não encontrado |
| 409 | Conflito |
| 422 | CPF não apto |
| 502 | Falha em serviço externo |

---

## 📦 Tecnologias

Java 21 · Spring Boot · JPA · Liquibase · Oracle · H2 · Lombok · Swagger · JUnit · Mockito · WireMock · Jacoco

---

## 👨‍💻 Autor

Tiago Pires  
Backend Engineer
