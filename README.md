# GuiaSafra — API de Monitoramento Agrícola

Global Solution 2026/1 — FIAP — Análise e Desenvolvimento de Sistemas  
Disciplina: Java Advanced

---

## Links da entrega

- **Deploy (URL pública da API):** `link`
- **Documentação da API (Swagger UI):** `link`
- **Vídeo de apresentação:** `link`
- **Vídeo pitch (até 3 min):** `link`
- **Repositório:** `link`

---

## Do que se trata o projeto

O tema da Global Solution 2026/1 parte da economia espacial: satélites que observam a Terra,
preveem o clima e orientam o agronegócio. O GuiaSafra pega esse legado — dados climáticos
derivados de observação por satélite — e o aplica num problema concreto do produtor rural:
saber **quando** e **quanto** irrigar, e ser avisado **antes** de uma geada destruir a lavoura.

A ideia é simples de enunciar e é nela que toda a API se apoia. No campo existem sensores
(ESP32) instalados em canteiros, que medem umidade e temperatura do solo. Na nuvem, a previsão
do tempo chega de uma fonte derivada de satélite (a Open-Meteo). Cruzando as duas informações,
o sistema decide regar automaticamente quando o solo seca, registra cada rega, e emite alertas
quando a umidade fica crítica ou quando a previsão indica risco de geada nos próximos dias.

Esta API é a camada de back-end dessa solução. Ela guarda o histórico de leituras, mantém a
previsão climática sincronizada, registra as regas (manuais e automáticas) e os alertas, e
expõe tudo isso através de uma API REST.

---

## Tecnologias utilizadas

- Java 17
- Spring Boot 4.0.6 (Web MVC, Data JPA, HATEOAS, Validation)
- Hibernate / Spring Data JPA como ORM
- Oracle Database (FIAP) como banco principal; H2 disponível para testes locais
- springdoc-openapi (Swagger UI) para documentação
- Lombok para reduzir código repetitivo
- jBCrypt para hash de senhas
- Spring Boot DevTools para produtividade no desenvolvimento
- Open-Meteo como API externa de clima
- Docker + Docker Compose para containerização

---

## Arquitetura Macro

![Arquitetura Macro](docs/arquitetura.png)

A solução é composta por um único container Docker executando a API Spring Boot, que se conecta
ao banco Oracle externo da FIAP e à API pública Open-Meteo para sincronização climática.

```
                        ┌─────────────────────────────────────────┐
                        │           guiasafra-net (bridge)        │
                        │                                         │
  Usuário / ESP32 ──────►  ┌──────────────────────────────────┐   │
  :8080                 │  │  guiasafra-app-rm565592          │   │
                        │  │  Spring Boot :8080               │   │
                        │  │  Imagem: custom (Dockerfile)     │   │
                        │  │  Usuário: appuser (não-root)     │   │
                        │  └────────────┬─────────────────────┘   │
                        │               │                         │
                        └───────────────┼─────────────────────────┘
                                        │
                    ┌───────────────────┼───────────────────┐
                    │                   │                   │
                    ▼                   ▼                   ▼
          Oracle FIAP (externo)   Open-Meteo API      Swagger UI
          oracle.fiap.com.br:1521  api.open-meteo.com  /swagger-ui.html
```

> O desenho completo em formato visual está disponível em `docs/arquitetura.png`.

---

## Como a aplicação está organizada

O projeto segue uma arquitetura em camadas. Cada requisição atravessa as camadas sempre na
mesma ordem, e a razão de separar assim é manter cada parte com uma única responsabilidade —
quando algo quebra, você sabe exatamente onde olhar.

```
Controller  ->  Service  ->  Repository  ->  Banco de dados
    |             |
   DTO         Entity
```

- **Controller** recebe a requisição HTTP, valida o corpo, e devolve a resposta com o status
code correto. Ele não contém regra de negócio. Se você abrir um controller, vai ver só
orquestração: chama o service, converte o resultado em DTO e monta os links HATEOAS.

- **Service** é onde mora a regra de negócio. É ele que decide, por exemplo, que uma rega
automática nasce sem usuário associado, ou que um alerta sempre começa em aberto. Quando o
service precisa barrar uma operação (um slot que não existe, um e-mail duplicado), ele lança
uma exceção que a camada de tratamento converte no status HTTP adequado.

- **Repository** fala com o banco. São interfaces que estendem `JpaRepository`, então a maior
parte das operações vem pronta. Onde foi preciso uma busca com filtro opcional, há uma query
declarada explicitamente.

- **DTO** (Data Transfer Object) é o que entra e sai pela API. Ele existe para que a entidade do
banco nunca seja exposta diretamente. Isso te dá liberdade para mudar o modelo interno sem
quebrar o contrato da API, e também te protege: a senha do usuário, por exemplo, simplesmente
não tem campo no DTO de resposta, então nunca vaza. Os DTOs são Java Records, porque são
imutáveis por natureza e descrevem o contrato de forma enxuta.

- **Entity** é o mapeamento das tabelas do banco. É a única camada que conhece detalhes de
persistência (tipos de coluna, chaves, relacionamentos).

Além dessas, há pacotes de apoio: `config` (Swagger e CORS), `validation` (o tratamento global
de exceções), e `client` (o componente que consome a API externa de clima).

---

## Domínio e divisão com o módulo .NET

A solução completa tem dois back-ends que dividem o mesmo banco Oracle. O módulo .NET é dono do
cadastro base (espécies plantadas e os slots/canteiros físicos). Esta API Java é dona de tudo que
é monitoramento e operação: usuários, previsão climática, leituras dos sensores, regas e alertas.

Um ponto importante de modelagem nasce dessa divisão. Os slots pertencem ao .NET, mas o Java
precisa lê-los (toda leitura, rega ou alerta acontece em um slot). A entidade `Slot` aqui é
marcada como somente-leitura: o Java consulta, valida que o slot existe, mas nunca grava nessa
tabela. Isso evita que dois donos escrevam no mesmo lugar e mantém a fronteira entre os módulos
clara.

### As entidades

| Entidade          | Tabela                     | Dono | Papel                                            |
| ----------------- | -------------------------- | ---- | ------------------------------------------------ |
| `User`            | TB\_CAD\_USER              | Java | Produtores e operadores do sistema               |
| `ClimateForecast` | TB\_MON\_CLIMATE\_FORECAST | Java | Previsão do tempo por dia, com risco de geada    |
| `Reading`         | TB\_MON\_READING           | Java | Cada medição de umidade/temperatura de um sensor |
| `WateringEvent`   | TB\_MON\_WATERING\_EVENT   | Java | Cada rega ocorrida (manual ou automática)        |
| `Alert`           | TB\_MON\_ALERT             | Java | Avisos de umidade crítica ou risco de geada      |
| `Slot`            | TB\_CAD\_SLOT              | .NET | Canteiro físico (lido pelo Java, nunca gravado)  |

---

## Modelagem avançada

O edital pede modelagem avançada com herança, chave composta, Embedded e múltiplas tabelas.
Cada um desses recursos foi usado onde o domínio realmente pedia, e não de enfeite. Abaixo o
raciocínio por trás de cada escolha.

### Herança: a classe Event

As regas e os alertas têm três coisas em comum: um identificador, o slot onde o evento
aconteceu, e o instante em que ocorreu. Em vez de repetir esses campos nas duas classes, eles
foram extraídos para uma classe-base abstrata `Event`, da qual `WateringEvent` e `Alert` herdam.

A estratégia escolhida foi `@MappedSuperclass`, e não as estratégias polimórficas do JPA
(`SINGLE_TABLE`, `JOINED`, `TABLE_PER_CLASS`). O motivo é concreto: rega e alerta vivem em
tabelas fisicamente separadas, cada uma com sua própria chave primária e colunas distintas, e
não existe nenhuma consulta no sistema que precise tratar "um evento qualquer" de forma genérica.
As estratégias polimórficas só fazem sentido quando você quer consultar a hierarquia inteira de
uma vez — aqui isso nunca acontece. Usá-las adicionaria uma coluna discriminadora e joins que o
domínio não pede.

Com `@MappedSuperclass`, cada subclasse vira sua própria tabela e herda os campos comuns. Onde uma
subclasse precisa de algo diferente, ela ajusta: o `Alert` usa `@AttributeOverride` para gravar o
instante na coluna `ALERT_TIME`, enquanto a `WateringEvent` usa a coluna `EVENT_TIME` padrão. Se
no futuro você precisar de um novo tipo de evento, basta estender `Event` — os três campos comuns
vêm de graça e você só declara o que for específico.

### Chave composta e Embedded: a entidade Reading

Uma leitura de sensor é identificada não por um número sequencial, mas pela combinação de **qual
slot** e **em que instante** ela foi medida. Faz sentido: o mesmo canteiro produz várias leituras
ao longo do dia, e duas leituras nunca compartilham o mesmo par (slot, instante).

Por isso a chave primária de `Reading` é composta, modelada com uma classe `ReadingId` anotada com
`@Embeddable` e referenciada na entidade com `@EmbeddedId`. O efeito prático aparece na própria
API: você busca ou remove uma leitura informando os dois valores na URL
(`/leituras/{slotId}/{readTimestamp}`), porque é esse par que identifica o registro de forma única.
O banco, por sua vez, impede que duas leituras do mesmo slot no mesmo instante sejam gravadas — e
quando isso é tentado, a API responde com conflito em vez de duplicar o dado.

### Conversão de tipo: o campo "resolvido" do alerta

Um detalhe pequeno mas que ilustra o cuidado com o banco. A coluna que indica se um alerta foi
resolvido é um CHAR(1) que guarda 'S' ou 'N', um padrão comum em bancos Oracle. No Java, porém, o
natural é representar isso como um booleano. Um `AttributeConverter` faz a ponte: converte `true`
para 'S' e `false` para 'N' ao gravar, e o caminho inverso ao ler. Assim o código Java trabalha com
um booleano limpo enquanto o banco mantém seu formato histórico.

---

## Os endpoints

A API usa os verbos HTTP pelo seu significado: GET para consultar, POST para criar, PUT para
atualizar, DELETE para remover. Os caminhos estão em português por convenção do projeto, enquanto
as classes seguem o padrão em inglês.

Todas as listagens são paginadas (parâmetros `page`, `size`, `sort`) e devolvem links HATEOAS, de
forma que o cliente consegue navegar entre os recursos sem precisar montar URLs na mão.

### Usuários — `/usuarios`

| Método | Caminho          | O que faz                                                       |
| ------ | ---------------- | --------------------------------------------------------------- |
| GET    | `/usuarios`      | Lista usuários (filtros opcionais: `name`, `email`)             |
| GET    | `/usuarios/{id}` | Busca um usuário por ID                                         |
| POST   | `/usuarios`      | Cria um usuário (a senha é hasheada com BCrypt antes de salvar) |
| PUT    | `/usuarios/{id}` | Atualiza um usuário                                             |
| DELETE | `/usuarios/{id}` | Remove um usuário                                               |

### Previsões climáticas — `/previsoes`

| Método | Caminho                         | O que faz                                                     |
| ------ | ------------------------------- | ------------------------------------------------------------- |
| GET    | `/previsoes`                    | Lista previsões (filtros opcionais: `source`, `date`)         |
| GET    | `/previsoes/{id}`               | Busca uma previsão por ID                                     |
| POST   | `/previsoes/sincronizar?dias=N` | Busca a previsão dos próximos N dias na Open-Meteo e persiste |
| POST   | `/previsoes`                    | Cria uma previsão manualmente (uso pontual)                   |
| DELETE | `/previsoes/{id}`               | Remove uma previsão                                           |

A sincronização é o fluxo principal aqui. Ela consulta a API externa, deriva o risco de geada a
partir da temperatura mínima prevista, e grava o resultado. A criação manual existe para casos de
correção ou teste. Vale saber que a sincronização atualiza a previsão existente quando já há um
registro para aquela data, em vez de duplicar — então uma previsão inserida manualmente para uma
data futura pode ser sobrescrita por uma sincronização posterior.

### Leituras dos sensores — `/leituras`

| Método | Caminho                              | O que faz                                                             |
| ------ | ------------------------------------ | --------------------------------------------------------------------- |
| GET    | `/leituras`                          | Lista leituras (filtro opcional: `slotId`)                            |
| GET    | `/leituras/{slotId}/{readTimestamp}` | Busca uma leitura pela chave composta                                 |
| POST   | `/leituras`                          | Registra uma leitura (se o instante for omitido, usa o momento atual) |
| DELETE | `/leituras/{slotId}/{readTimestamp}` | Remove uma leitura pela chave composta                                |

O instante na URL segue o formato ISO, por exemplo `2026-06-05T12:00:00`.

### Regas — `/regas`

| Método | Caminho       | O que faz                                                         |
| ------ | ------------- | ----------------------------------------------------------------- |
| GET    | `/regas`      | Lista regas (filtro opcional: `slotId`)                           |
| GET    | `/regas/{id}` | Busca uma rega por ID                                             |
| POST   | `/regas`      | Registra uma rega (manual com usuário, ou automática sem usuário) |
| DELETE | `/regas/{id}` | Remove uma rega                                                   |

Uma rega tem origem `MANUAL` ou `AUTOMATIC`. Quando manual, ela carrega o usuário que a acionou;
quando automática, o campo de usuário fica nulo, porque foi a regra do sistema que disparou.

### Alertas — `/alertas`

| Método | Caminho                  | O que faz                                               |
| ------ | ------------------------ | ------------------------------------------------------- |
| GET    | `/alertas`               | Lista alertas (filtros opcionais: `slotId`, `resolved`) |
| GET    | `/alertas/{id}`          | Busca um alerta por ID                                  |
| POST   | `/alertas`               | Registra um alerta (nasce sempre em aberto)             |
| PUT    | `/alertas/{id}/resolver` | Marca o alerta como resolvido                           |
| DELETE | `/alertas/{id}`          | Remove um alerta                                        |

A severidade de um alerta é `LOW`, `MEDIUM` ou `CRITICAL`. O endpoint de resolver é idempotente:
chamá-lo num alerta já resolvido não causa erro, apenas mantém o estado.

---

## Como a API trata entradas inválidas

A validação acontece antes de qualquer regra de negócio. Cada DTO de entrada declara as suas
restrições (campo obrigatório, tamanho máximo, valor positivo, e-mail válido), e o Spring Validation
rejeita a requisição automaticamente quando algo não bate. O cliente recebe a lista de campos com
problema, em vez de um erro genérico.

Quando o problema não é de formato mas de regra — um slot que não existe, um e-mail já cadastrado —
o service lança uma exceção, e um tratador global de exceções a converte no status HTTP correto.
O resultado é que a API responde de forma previsível e padronizada:

| Status          | Quando acontece                                            |
| --------------- | ---------------------------------------------------------- |
| 200 OK          | Consulta ou atualização bem-sucedida                       |
| 201 Created     | Recurso criado (a resposta traz o link para ele)           |
| 204 No Content  | Remoção bem-sucedida                                       |
| 400 Bad Request | Corpo inválido (validação falhou)                          |
| 404 Not Found   | Recurso ou referência (slot, usuário) inexistente          |
| 409 Conflict    | Violação de unicidade (e-mail duplicado, leitura repetida) |
| 502 Bad Gateway | Falha ao consultar a API externa de clima                  |

---

## Documentação interativa (Swagger)

Com a aplicação no ar, a documentação completa fica disponível em:

```
/swagger-ui.html
```

Lá você consegue ver cada endpoint, os parâmetros que ele aceita, os corpos de requisição e
resposta, e ainda disparar chamadas de teste direto do navegador.

---

## Como executar localmente (sem Docker)

Pré-requisitos: Java 17 e Maven (o projeto já inclui o wrapper `./mvnw`, então o Maven instalado
nem é obrigatório).

1. Configure o acesso ao banco em `src/main/resources/application.properties`. O projeto vem
apontado para o Oracle da FIAP; ajuste `username` e `password` para as suas credenciais.

2. Na raiz do projeto, rode:

```bash
./mvnw spring-boot:run
```

No Windows:

```bash
mvnw.cmd spring-boot:run
```

3. A API sobe em `http://localhost:8080`. A partir daí, acesse o Swagger em
`http://localhost:8080/swagger-ui.html` ou chame os endpoints diretamente.

Observação sobre o banco: a aplicação roda com `ddl-auto=create-drop` via variável de ambiente,
portanto as tabelas são criadas automaticamente ao subir e removidas ao encerrar.

---

## Como executar com Docker

### Pré-requisitos

- Docker e Docker Compose instalados
- Git
- Acesso ao banco Oracle da FIAP (credenciais do seu RM)

### Passo a passo

**1. Clone o repositório:**

```bash
git clone https://github.com/Guia-Safra-GS/Java-GS.git
cd Java-GS
```

**2. Configure as variáveis de ambiente:**

```bash
cp .env.example .env
nano .env
```

Preencha o `.env` com suas credenciais:

```env
DB_URL=jdbc:oracle:thin:@oracle.fiap.com.br:1521:ORCL
DB_USERNAME=seu_rm
DB_PASSWORD=sua_senha
SPRING_JPA_HIBERNATE_DDL_AUTO=create-drop
```

Salve com `Ctrl+O` → `Enter` → `Ctrl+X`.

**3. Suba o container em background:**

```bash
docker compose up -d --build
```

**4. Verifique que está rodando:**

```bash
docker ps
```

Esperado: `guiasafra-app-rm565592` com status `Up`.

**5. Acesse a API:**

```
http://localhost:8080/swagger-ui.html
```

### Validando o container (evidências)

```bash
# Verificar usuário não privilegiado — esperado: appuser
docker container exec -it guiasafra-app-rm565592 whoami

# Verificar diretório de trabalho — esperado: /app
docker container exec -it guiasafra-app-rm565592 pwd

# Listar estrutura de arquivos
docker container exec -it guiasafra-app-rm565592 ls -l

# Ver logs da aplicação
docker compose logs guiasafra-app-rm565592
```

### Encerrando

```bash
docker compose down
```

---

## Como os endpoints foram testados

Os endpoints foram validados com o Insomnia contra o banco Oracle real, exercitando os fluxos
principais e os casos de erro: criação com corpo válido (201), consulta de recurso existente (200)
e inexistente (404), validação com corpo inválido (400) e violação de unicidade (409). A coleção
do Insomnia está versionada em `docs/insomnia_guiasafra.yaml` e pode ser importada para reproduzir
os testes.

---

## Integrantes

- `Orlando Gonçalves` — **rm561584**
- `Gabriel Lourenço Martins` — **rm562194**
- `Matheus Roque Arantes` — **rm561959**
- `Giovane Amato dos Santos` — **rm561336**
- `André Emygdio Ferreira` — **rm565592**
