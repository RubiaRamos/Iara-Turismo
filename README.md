# Hotel Iara — Agência de Hotéis

Projeto final de **Programação Orientada a Objetos** (PUCPR) em Java.

## O problema

Uma agência trabalha com vários hotéis. O cliente precisa encontrar um quarto livre num período,
reservar, pagar (Pix, cartão ou boleto), acompanhar suas reservas e avaliar a hospedagem.
O sistema organiza tudo isso aplicando os conceitos de POO, com dados guardados em MySQL.

## Funcionalidades

- Cadastro de cliente (nome, CPF, telefone, nascimento, e-mail) e login com senha guardada como hash SHA-256.
- Pesquisa de quartos livres por destino (cidade, estado ou hotel), check-in, check-out e número de hóspedes.
- Detalhe do quarto com o total calculado na hora, conforme o tipo do quarto.
- Reserva (1 reserva = 1 quarto), com bloqueio de período ocupado, datas inválidas e excesso de hóspedes.
- Pagamento por **Pix** (aprova na hora), **cartão** (1 a 12 parcelas) ou **boleto** (fica pendente).
  Dá para pagar em partes; a reserva é confirmada quando fica quitada.
- "Minhas reservas": histórico, pagar o restante, cancelar (estorna os pagamentos aprovados) e avaliar (nota 1 a 5).
- Mapa dos hotéis (OpenStreetMap + Leaflet). Hotel sem coordenadas é localizado pela API Nominatim.
- Mensagens claras para cada erro (exceções próprias do sistema).

## Tecnologias

Java 17, JavaFX 21 (telas em FXML), MySQL 8 + JDBC, Maven, Gson,
API Nominatim / OpenStreetMap (mapa) e ViaCEP (consulta de CEP, em `util/CepService`).

## Como executar

Requisitos: **JDK 17+**, **Maven** e **MySQL 8**. Na primeira vez, o Maven precisa de internet para baixar as bibliotecas.

1. **Banco de dados** — no MySQL, rodar nesta ordem:
   1. `hotel-iara/hotel-iara/sql/hotel_banco_v3.sql` (cria a base `hotel_iara_demo` e as tabelas)
   2. `hotel-iara/hotel-iara/sql/dados_exemplo.sql` (4 hotéis, 15 quartos, clientes, reservas...)
2. **Configuração** — copiar `hotel-iara/hotel-iara/db.properties.exemplo` para `db.properties`
   (na mesma pasta) e preencher usuário e senha do MySQL. Esse arquivo não vai para o GitHub.
3. **Rodar** — na pasta `hotel-iara/hotel-iara`:
   ```bash
   mvn javafx:run
   ```
   No IntelliJ: abrir a pasta `hotel-iara/hotel-iara` (a que tem o `pom.xml`) e rodar a classe
   `br.com.hoteliara.Launcher`, com *Working directory* nessa mesma pasta.

**Logins de teste** (senha `123456`): cliente `ana.silva@email.com` · funcionária `renata.faria@agencia.com`
(funcionário consegue pesquisar, mas só cliente reserva).

## Organização do código (`hotel-iara/hotel-iara/src/main/java/br/com/hoteliara`)

| Pacote | O que tem | Responsabilidade |
|---|---|---|
| `model` | Pessoa, Cliente, Funcionario, Hotel, Endereco, Categoria, Quarto (+4), Reserva, Pagamento (+3), Avaliacao, enums, interfaces | Entidades e regras do negócio |
| `service` | AuthService, HotelService, ReservaService, PagamentoService | Coordena o modelo e o banco para cada caso de uso |
| `dao` | Dao&lt;T&gt;, ConexaoBD e um DAO por entidade | Acesso ao MySQL via JDBC |
| `controller` + `resources/fxml` | Uma tela por arquivo FXML e seu controller | Interface gráfica (só conversa com os services) |
| `exception` | Exceções próprias | Situações de erro do sistema |
| `util` | SenhaUtil, MapaService, CepService, Dialogos, Imagens | Apoio e integração com APIs |

Fluxo das telas: login → pesquisa → detalhe do quarto → pagamento → minhas reservas (mapa pelo menu ☰).

## Decisões de modelagem (conceitos de POO)

| Conceito | Onde | Por quê |
|---|---|---|
| **Classes e objetos** | Hotel, Quarto, Reserva, Cliente, Pagamento... | Cada classe representa algo real da agência, com uma responsabilidade clara. |
| **Encapsulamento** | Atributos `private`; `Reserva` sem `setStatus`; `Pessoa` sem `getSenha`; listas devolvidas só para leitura | O estado só muda por métodos que validam (ex.: `confirmar()`, `cancelar()`, `concluir()` conferem se a mudança é permitida). |
| **Herança** | `Pessoa` → Cliente/Funcionario; `Quarto` → Padrão/Executivo/Luxo/Suíte; `Pagamento` → Pix/Cartão/Boleto | Relação "é um": a parte comum fica na superclasse e não se repete. |
| **Classes e métodos abstratos** | `Pessoa`, `Quarto`, `Pagamento`; `calcularDiaria()`, `processar()`, `exibirDados()` | Não existe "quarto genérico" nem "pagamento genérico"; o método abstrato obriga cada subclasse a ter a sua regra. |
| **Polimorfismo** | `Reserva.calcularTotal()` chama `quarto.calcularDiaria()`; `ReservaService.pagar()` chama `processar()` | Quem usa não precisa saber o tipo real. Um tipo novo de quarto não exige mudar a Reserva. |
| **Interfaces** | `Cancelavel` (Reserva, Pagamento), `Reservavel` (Quarto), `Dao<T>` (DAOs) | Contratos para classes sem parentesco: o cancelamento de uma reserva estorna os pagamentos tratando os dois como `Cancelavel`. |
| **Coleções** | `List<Quarto>` no Hotel, `List<Reserva>` no Cliente, `List<Pagamento>` na Reserva, `Map<Integer, Hotel>` no HotelService | Relações um-para-muitos; filtros e ordenação com streams e `Comparator`. |
| **Associação** | Reserva liga Cliente e Quarto; Quarto pertence a Hotel e Categoria | Os objetos colaboram: a reserva pede ao quarto o preço e se ele está livre. |
| **Exceções** | `QuartoIndisponivelException`, `ReservaInvalidaException`, `PagamentoRecusadoException`, `LoginInvalidoException`, `BancoDadosException`... | Situações esperadas viram mensagens para o usuário em vez de travar o programa. |

Outras decisões:
- A situação "ocupado" do quarto **não é guardada**: é calculada pelas reservas (`Quarto.getStatus()`), assim nunca fica desatualizada.
- A coluna `tipo` da categoria decide qual subclasse de Quarto criar (`Quarto.criar(...)`, uma fábrica).
- Telas não acessam o banco: chamam os services, que usam o modelo e os DAOs (camadas separadas).

## Uso de IA

Primeiro modelamos o sistema em grupo, no documento do projeto (classes, heranças, interfaces e banco).
Depois usamos IA (Claude) para gerar a primeira versão do código de cada parte a partir desse modelo,
para ajustar as partes para se encaixarem, para revisar e testar, e também para estudar os conceitos de POO
aplicados. Todo o código foi revisado e testado pela equipe.

## Equipe

| Integrante | Principal contribuição |
|---|---|
| Nacimy Santos | Hotel, Endereco, Categoria, Quarto e subclasses, Reserva, interfaces Reservavel/Cancelavel, enums; integração das telas com o sistema; revisão dos Pull Requests |
| Luiza Wesp Fernandes| Pessoa, Cliente, Funcionario e senha com hash; documentação |
| Ruan Eduardo Yamaguchi (Du) | Pagamento e subclasses, Avaliacao, DAOs e conexão JDBC, MapaService e CepService; banco de dados |
| Rúbia Ramos | Telas de login, cadastro e detalhe do quarto; modelagem do banco; documentação |
| Fabrine wrublevski | Tela de pesquisa (layout e estilo) |
