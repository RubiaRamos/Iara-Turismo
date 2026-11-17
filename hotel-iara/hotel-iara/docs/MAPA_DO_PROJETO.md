# Hotel Iara: mapa do codigo e decisoes de POO

## Estado da entrega

> **Atualizacao (17/11):** depois desta entrega, as telas foram integradas ao sistema (PR #2 `integracao-telas` e PR #3 `area-funcionario`). Login, cadastro, pesquisa, reserva, pagamento, minhas reservas, area do funcionario e mapa usam os services, o model e os DAOs. O MapaService e usado pela tela de mapa. Visao geral atualizada: README da raiz.

Base: branch feature/classes-pessoa, commit b27e40e9e3b8c9a3397b6d65983369bb1858f425.
Essa base ja inclui as classes da branch model-hospedagem.
As classes das colegas foram preservadas. O trabalho de Du adiciona pagamento, avaliacao,
persistencia e consultas de CEP/localizacao. A integracao das telas veio depois (PRs #2 e #3).
A compilacao do aplicativo completo foi verificada com javac --release 17 e JavaFX 21.0.5.
Foram executadas 96 verificacoes: 24 de dominio, 43 JDBC e 29 de API simulada.
Depois da integracao, o fluxo completo das telas foi testado com banco MySQL/MariaDB real.

## Como os pacotes se dividem

| Pacote | Responsabilidade |
| --- | --- |
| br.com.hoteliara | Main/Launcher (inicio), Navegador (troca de telas) e Sessao (usuario logado e escolhas entre telas) |
| controller | Uma classe por tela: login, cadastro, pesquisa, quarto, pagamento, reservas, admin e mapa |
| model | Entidades e regras reais: pessoas, hoteis, quartos, reservas, pagamentos |
| exception | Excecoes do dominio, de banco e de consulta externa |
| dao | Persistencia JDBC e reconstrucao das associacoes |
| service | AuthService (login/cadastro), HotelService (pesquisa), ReservaService (reservar, pagar, cancelar, avaliar, acoes do funcionario) e PagamentoService |
| util | Senha, imagens, dialogos, CEP e localizacao |

## Entidades: o que cada classe representa

| Classe | Responsabilidade e relacoes |
| --- | --- |
| Pessoa | Abstrata; dados comuns de Cliente e Funcionario, autenticacao e alteracao de senha |
| Cliente | Herda Pessoa; cria reservas, guarda historico e avalia hospedagens concluidas |
| Funcionario | Herda Pessoa; cadastra hotel/quarto e gerencia reservas |
| Endereco | Rua, numero, CEP, cidade, UF, formatacao e coordenadas opcionais |
| Hotel | Nome, estrelas, endereco, quartos e avaliacoes; calcula media |
| Categoria | Tipo, capacidade e diaria compartilhados pelos quartos |
| Quarto | Abstrata; numero, hotel, categoria e disponibilidade por periodo |
| QuartoPadrao | Calcula valor base multiplicado pelas noites |
| QuartoExecutivo | Desconto de 10% a partir de sete noites |
| QuartoLuxo | Acrescenta 10% de taxa de servico |
| Suite | Acrescenta R$ 80 de cafe da manha por noite |
| Reserva | Liga cliente e quarto a datas; guarda total historico e pagamentos |
| Pagamento | Abstrata; estado comum, validacao, aprovacao protegida e estorno |
| PagamentoPix | Processamento aprova imediatamente na simulacao |
| PagamentoCartao | Valida parcelas no construtor e aprova no processamento |
| PagamentoBoleto | Processamento permanece pendente; baixa aprova |
| Avaliacao | Nota e comentario de uma reserva concluida; edicao validada |

## Contratos e estados

| Elemento | Por que existe |
| --- | --- |
| Cancelavel | Reserva e Pagamento podem ser cancelados sem terem relacao de heranca entre si |
| Reservavel | Define disponibilidade, reserva e liberacao de um periodo |
| Dao<T> | Define cinco operacoes de persistencia com o tipo correto da entidade |
| StatusPagamento | Restringe estado a PENDENTE, APROVADO ou ESTORNADO |
| StatusReserva | Restringe estado a PENDENTE, CONFIRMADA, CANCELADA ou CONCLUIDA |
| StatusQuarto | DISPONIVEL, OCUPADO ou MANUTENCAO; ocupacao e derivada das reservas |
| TipoQuarto | Determina a subclasse escolhida pela fabrica Quarto.criar |

Interface nao e um objeto e nao implementa o SQL. Uma classe concreta que implementa Dao<T>
deve fornecer os cinco metodos (ou herdar implementacoes concretas compativeis).
Os metodos declarados nesse contrato sao publicos. T representa o tipo, nao uma variavel de objeto.

## Onde aparece heranca

Pessoa -> Cliente / Funcionario.
Quarto -> QuartoPadrao / QuartoExecutivo / QuartoLuxo / Suite.
Pagamento -> PagamentoPix / PagamentoCartao / PagamentoBoleto.
As classes de excecao tambem herdam de classes prontas do Java.
Heranca representa um "e um": Pix e um Pagamento. HotelDAO nao e um Hotel; acessa seus dados.
implements expressa cumprimento de contrato, nao heranca de implementacao de uma classe.

## Onde aparece polimorfismo

Reserva.calcularTotal chama quarto.calcularDiaria sem testar qual subclasse recebeu.
PagamentoService.processar recebe Pagamento e chama pagamento.processar.
Cliente e Funcionario sobrescrevem Pessoa.exibirDados.
O tipo declarado pode ser o tipo base; o metodo sobrescrito depende do objeto real.
@Override ajuda o compilador a verificar se o metodo realmente sobrescreve um contrato existente.

## Por que classes abstratas

Reunem dados e operacoes comuns e impedem instanciar diretamente um conceito sem sua variacao.
Possuem construtores para inicializar a parte comum das subclasses.
Metodos abstratos exigem que as subclasses concretas fornecam o comportamento que varia.
Uma classe abstrata pode ter metodos concretos, atributos, construtor e implementar interfaces.

## Encapsulamento

Atributos private impedem alteracao direta por outras classes.
Status de Reserva e Pagamento muda por operacoes, nao por um setter livre.
Os metodos verificam transicoes permitidas, como estornar apenas um pagamento aprovado.
Getters de listas das entidades devolvem visoes nao modificaveis; alteracoes passam por metodos.
Isso nao torna os elementos da lista imutaveis: um Pagamento contido ainda pode ser estornado.
final em referencia impede trocar a referencia, nao impede mudar o objeto apontado.
protected em Pagamento.aprovar permite uso pelas subclasses sem expor aprovacao ao resto do sistema.

## Associacoes e colecoes

Reserva conhece seu Cliente, seu Quarto e seus Pagamentos.
Quarto conhece Hotel e Categoria; Hotel mantem listas de quartos e avaliacoes.
Essas relacoes sao referencias entre objetos, nao relacoes de heranca.
List organiza varios objetos; ArrayList e uma implementacao concreta.
Map<Integer, Entidade> no carregamento relaciona IDs a objetos e evita duplicacao de instancias.
Dois objetos distintos com o mesmo ID nao se tornam automaticamente o mesmo objeto em Java.

## Construtores, this e super

Construtor inicializa a instancia, tem o nome da classe e nao declara tipo de retorno.
this refere-se a instancia atual; this(...) chama outro construtor da mesma classe.
super(...) chama o construtor da classe mae para inicializar sua parte do objeto.
new cria a instancia e executa sua inicializacao; o construtor pode recusar dados invalidos.
Construtores nao sao herdados nem sobrescritos.
O tipo abstrato nao pode receber new diretamente, mas pode ser usado como tipo de uma referencia.

## Excecoes

| Excecao | Tipo | Uso |
| --- | --- | --- |
| PagamentoRecusadoException | Checked | Valor ou parcelas invalidos, saldo insuficiente para o valor, reserva cancelada |
| QuartoIndisponivelException | Checked | Quarto em manutencao ou periodo ocupado |
| ReservaInvalidaException | Checked | Datas, cliente, quarto ou hospedes invalidos |
| ConsultaApiException | Checked | Consulta externa indisponivel ou resposta inadequada |
| BancoDadosException | Unchecked | Falha JDBC/configuracao, preservando causa |
| IllegalArgumentException | Unchecked | Argumento invalido |
| IllegalStateException | Unchecked | Operacao nao permitida no estado atual |

Exception e uma classe Java no pacote java.lang, nao o pacote.
throw lanca uma excecao; throws declara que um metodo pode propaga-la.
try delimita uma tentativa; catch recebe uma excecao compativel.
Checked exige tratar ou declarar; unchecked nao exige isso pelo compilador.
Nenhuma delas corrige automaticamente o problema.
super(mensagem, causa) chama o construtor herdado, preservando explicacao e erro original.
try-with-resources fecha arquivos, conexoes, statements e resultados mesmo em caso de falha.

## Sobrecarga versus sobrescrita

Sobrecarga: mesmo nome com parametros diferentes, como os dois construtores de PagamentoBoleto.
Sobrescrita: uma subclasse implementa o metodo herdado com assinatura compativel, como processar.
O retorno sozinho nao distingue sobrecargas.
static pertence a classe; metodos static nao participam da sobrescrita polimorfica de instancia.

## Fluxo de pagamento

1. Reserva ja existe e possui valor total.
2. Construtor cria um pagamento pendente sem alterar a reserva.
3. PagamentoService chama processar no tipo base.
4. A subclasse valida/processa; se houver recusa, nao associa.
5. O service associa o pagamento a reserva.
6. getValorPago soma apenas aprovados; getSaldoDevedor deriva o saldo.
7. O DAO persiste o pagamento quando chamado; service atual nao grava automaticamente.
8. Boleto so conta apos baixa. Estorno faz o pagamento deixar de contar no valor pago.

## Persistencia sem repetir regras de criacao

Quarto.criar escolhe subclasse a partir da categoria.
Reserva.restaurar preserva datas antigas e valor cobrado, sem usar o construtor de reserva nova.
Pagamento.restaurar preserva status/data, sem simular uma nova cobranca.
Avaliacao.restaurar preserva ID/data, mas ainda exige reserva concluida.
DadosBD associa cada objeto na ordem em que suas dependencias ja existem.
Os DAOs nao substituem a validacao do dominio e nao tornam a demonstracao concorrente.

## Infraestrutura e telas

ConexaoBD le propriedades e abre Connection; Dao nao abre conexao sozinho.
Jdbc reduz repeticao de PreparedStatement; DadosBD monta o grafo por ID.
Main inicia JavaFX, Navegador troca FXML, controllers reagem a eventos.
Sessao guarda a pessoa logada, a pesquisa, o quarto e a reserva escolhidos entre telas.
Cada tela chama um service; o service usa o model (regras) e os DAOs (banco). A tela nunca acessa o banco direto.
Dialogos apresenta avisos e erros; Imagens carrega imagens e arredonda sua exibicao.
O antigo pacote modelo (dados de demonstracao em memoria) foi removido na integracao (PR #2).

## Perguntas para estudar depois

- Por que Pagamento e abstrata, mas Avaliacao nao?
- O que aconteceria se Pix nao implementasse processar?
- Qual metodo executa quando uma referencia Pagamento aponta para PagamentoBoleto?
- Por que Pagamento e Reserva implementam Cancelavel, sem uma herdar da outra?
- Por que status nao tem setter? Qual regra um setter livre permitiria violar?
- Quem associa pagamento a reserva? Quem salva no banco?
- Por que nao usar o construtor de uma reserva nova para carregar uma antiga?
- Qual a diferenca entre List e ArrayList?
- Qual a diferenca entre identidade de objeto e ID no banco?
- O que super(mensagem) faz? O que ele nao faz?
- Qual a diferenca entre throw e throws? Entre checked e unchecked?
- O que muda entre sobrecarga e sobrescrita? Entre private, protected e public?
- Por que final em uma List nao impede adicionar elementos?
- Por que PreparedStatement usa parametros? Quem fecha a conexao?

