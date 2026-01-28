🗳️ API de Votação – Coop Votação

API REST desenvolvida em **Java 21 + Spring Boot 3.x** para gerenciamento de pautas, sessões de votação, registro de votos e apuração de resultados, seguindo boas práticas de arquitetura, REST, testes automatizados e documentação OpenAPI.

---

## 📌 Funcionalidades

- Criar pautas de votação
- Abrir sessão de votação (com duração configurável)
- Registrar votos (**SIM / NAO**) por CPF
- Garantia de **voto único por CPF em cada pauta**
- Validação opcional de CPF (configurável por ambiente)
- Apuração de resultado da votação
- Documentação via **Swagger (OpenAPI 3)**
- Testes unitários e de integração com cobertura

---

## 🧱 Arquitetura e Padrões

- Java 21
- Spring Boot 3.x
- Spring Data JPA
- Liquibase (controlado por profile)
- Oracle Database (produção)
- H2 (testes)
- DTOs imutáveis (records)
- Controllers enxutos
- Regras de negócio explícitas na camada Service
- Exceções mapeadas para HTTP
- Configuração por profiles
- Clock injetável para testes determinísticos
- Jacoco para cobertura de testes

---

## 🔐 Identidade do Votante

- A identidade do votante é baseada **exclusivamente no CPF**
- Não existe mais conceito de `associadoId`
- Um mesmo CPF pode votar **apenas uma vez por pauta**

---

## 🔐 Validação de CPF

```yaml
cpf-validation:
  enabled: true | false
```

- Local / Testes: desabilitada
- Outros ambientes: integração com serviço externo

---

## 🗄️ Liquibase

- Ativo em ambientes reais
- Desabilitado em testes (H2) via profile de teste

---

## 🚀 Como rodar o projeto

```bash
docker-compose up -d oracle
./gradlew bootRun
```

---

## 🧪 Testes

```bash
./gradlew test
./gradlew jacocoTestReport
```

Relatório:
build/reports/jacoco/test/html/index.html

---

## 📄 Swagger

- http://localhost:8080/swagger-ui.html
- http://localhost:8080/v3/api-docs

---

## 🔁 Endpoints

POST /api/v1/pautas  
GET  /api/v1/pautas/{id}  
POST /api/v1/pautas/{id}/sessao  
POST /api/v1/pautas/{id}/votos  
GET  /api/v1/pautas/{id}/resultado

---
## 🛑 Códigos HTTP

|  Código | Significado                                                             |
| ------: | ----------------------------------------------------------------------- |
| **201** | Recurso criado com sucesso                                              |
| **200** | Operação realizada com sucesso                                          |
| **400** | Requisição inválida (erro de validação de payload)                      |
| **404** | Recurso não encontrado (pauta inexistente)                              |
| **409** | Conflito de negócio (sessão já aberta, voto duplicado para o mesmo CPF) |
| **422** | CPF inválido ou não apto para votação                                   |
| **500** | Erro interno inesperado                                                 |
| **502** | Falha em serviço externo (validação de CPF)                             |

## 👨‍💻 Autor

Tiago Pires – Backend Engineer
