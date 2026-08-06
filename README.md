# Catalog Service

Projeto inicial da disciplina de Arquitetura de Software. O objetivo é praticar
uma **refatoração gradual para uma arquitetura em camadas**, começando por um
caso de uso pequeno: a criação de produtos.

> Este repositório é propositalmente simples e ainda não possui as camadas de
> controller, service e repository devidamente separadas. O comportamento atual
> deve ser preservado enquanto a estrutura evolui.

## Cenário

Uma API de catálogo precisa permitir o cadastro de um produto com nome,
descrição e preço. Por enquanto, não fazem parte do exercício estoque,
categorias, descontos, edição ou consulta de produtos.

### Validações básicas

- `nome` é obrigatório;
- `descricao` é opcional;
- `preco` é obrigatório e deve ser maior que zero;
- o preço é armazenado com duas casas decimais.

### Regras de negócio

- o nome do produto deve ter pelo menos 3 caracteres;
- produtos com preço a partir de R$ 1.000,00 precisam possuir uma descrição.

As regras são executadas antes da persistência. Elas ainda estão no controller
para que, durante a atividade, possam ser identificadas e movidas para o service.

## Executando o projeto

Pré-requisitos: Java 21 e Maven 3.9 ou superior.

```bash
mvn spring-boot:run
```

A API estará disponível em `http://localhost:8080`. <br> 
O projeto usa um banco H2   em memória, recriado sempre que a aplicação inicia.  
O console pode ser acessado em `http://localhost:8080/h2-console`, usando:

- JDBC URL: `jdbc:h2:mem:catalog_service`
- usuário: `sa`
- senha: deixe em branco

## Criando um produto

`POST /api/produtos`

```bash
curl -X POST http://localhost:8080/api/produtos \
  -H "Content-Type: application/json" \
  -d '{
    "nome": "Teclado mecânico",
    "descricao": "Teclado ABNT2 com iluminação",
    "preco": 349.90
  }'
```

Resposta esperada: status `201 Created` e o produto com seu `id` gerado.
Dados inválidos retornam status `400 Bad Request` e uma mensagem no campo
`erro`.

## Sobre a Atividade

Para executar os testes, use no terminal:

```bash
mvn test
```

Observe o arquivo `ProdutoController`: hoje ele recebe a requisição, valida as
regras e executa o SQL. Essas responsabilidades estão juntas de propósito.

A evolução poderia ser feita em etapas pequenas:

1. representar o produto com uma classe, em vez de usar mapas;
2. extrair o acesso ao banco para um repository;
3. extrair as regras de criação para um service;
4. deixar o controller responsável apenas pela comunicação HTTP;
5. manter os testes passando depois de cada etapa.
