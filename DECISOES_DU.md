# Decisoes de implementacao - Du

## Etapa 1: excecao de pagamento e contrato DAO

### PagamentoRecusadoException

- Local: src/main/java/br/com/hoteliara/exception/PagamentoRecusadoException.java.
- Herda de Exception: e checked. O compilador exige que quem chama um metodo que declara essa excecao a capture ou a declare com throws.
- Representa uma recusa prevista pelas regras do pagamento, permitindo que a tela informe o motivo ao usuario.
- O construtor recebe uma mensagem e a encaminha para Exception com super(mensagem).
- A excecao nao valida valores sozinha: Pagamento e suas subclasses farao as verificacoes e lancarao a excecao.
- Regras previstas: valor menor ou igual a zero, parcelas fora de 1 a 12 ou valor superior ao saldo da reserva. Ainda nao implementadas nesta etapa.

Defesa: "Criei uma excecao especifica para diferenciar uma recusa de pagamento de outros erros. Ela herda de Exception, entao o compilador exige tratamento ou declaracao. A regra que detecta a recusa fica no pagamento, e a excecao transporta o motivo. Assim, a tela pode informar o problema sem encerrar o fluxo inesperadamente."

### Dao<T>

- Local: src/main/java/br/com/hoteliara/dao/Dao.java.
- Interface porque define operacoes sem impor a implementacao SQL de uma entidade.
- T e um parametro de tipo: em Dao<Hotel>, salvar recebe Hotel e buscarPorId devolve Hotel.
- Mantida a assinatura fornecida pela equipe: salvar, buscarPorId, listar, atualizar e remover.
- Contrato adotado: buscarPorId retorna null se nao houver registro; listar retorna lista vazia se nao houver registros.
- As implementacoes futuras devem seguir essas convencoes. Quem chama buscarPorId deve verificar null antes de usar o resultado.
- A interface nao abre conexao e nao executa SQL. Os DAOs concretos implementarao as operacoes com JDBC.

Defesa: "Usei uma interface para definir as operacoes comuns dos DAOs. O parametro T permite usar o mesmo contrato com entidades diferentes, preservando o tipo dos objetos. Cada DAO vai escrever o SQL da propria tabela. A interface organiza o acesso aos dados, mas nao executa consultas sozinha."

## Etapa 2: BancoDadosException

- Herda de RuntimeException: e unchecked e nao exige catch ou throws em cada chamador.
- A escolha preserva as assinaturas do contrato Dao<T> fornecido pela equipe, sem acrescentar throws SQLException a todos os metodos.
- Os DAOs futuros capturarao SQLException e lancarao BancoDadosException com uma mensagem compreensivel e a causa original.
- O construtor recebe Throwable porque esse e o tipo comum das causas de erros e excecoes em Java; aceita SQLException.
- super(mensagem, causa) preserva tanto a explicacao quanto o erro tecnico original, acessivel por getCause().
- Unchecked nao significa ignorar: a camada de apresentacao devera tratar a falha para informar o usuario. Nenhuma tela foi alterada nesta etapa.
- Exemplo futuro: catch (SQLException erro) { throw new BancoDadosException("Nao foi possivel salvar o hotel.", erro); }
- Ainda nao ha conexao ou DAO concreto usando esta excecao.

## Etapa 3: ConexaoBD

- ConexaoBD abre uma nova Connection JDBC por chamada de conectar(); nao mantem conexao global.
- Classe final com construtor privado e metodo static porque nao possui estado de uma entidade e nao precisa ser instanciada.
- Le db.properties no diretorio de execucao. No IntelliJ, definir Working directory como a pasta hotel-iara/hotel-iara que contem pom.xml.
- Properties le pares chave=valor. URL e usuario devem estar preenchidos; a chave de senha deve existir, mas senha vazia e permitida.
- O arquivo e fechado com try-with-resources. A conexao retornada permanece aberta para uso; cada DAO devera fecha-la com seu proprio try-with-resources.
- IOException e SQLException sao convertidas em BancoDadosException, preservando a causa.
- db.properties.exemplo nao contem credenciais reais. O .gitignore exclui db.properties.
- DriverManager pertence ao Java; o driver MySQL concreto ainda precisa ser adicionado ao pom.xml antes de uma conexao real.
- Nao executamos o script SQL nem alteramos classes das colegas.

## Etapa 4: pagamentos e avaliacao integrados

### Base conferida

- Fontes da branch feature/classes-pessoa, que ja contem os mesmos arquivos da model-hospedagem. Fontes das colegas copiados sem alteracao para o projeto local.
- Nao houve merge, commit ou envio ao GitHub nesta etapa.

### Pagamento e subclasses

- Pagamento e abstrata: compartilha reserva, valor, data, status e estorno; processar e abstrato porque o comportamento depende da forma.
- Pix e cartao aprovam na simulacao. Cartao exige parcelas entre 1 e 12 no construtor; como o atributo e final, ele nao pode mudar depois para um valor invalido.
- Boleto processado continua pendente; registrarBaixa aprova apos validar novamente o saldo. Data de vencimento padrao: hoje mais tres dias.
- Nao ha integracao bancaria real. Nao foi inventada taxa: calcularTaxa retorna zero enquanto a equipe nao definir uma politica.
- Valor nao positivo ou nao finito e recusado na construcao. Saldo e verificado no processamento e na baixa, pois pode mudar apos a criacao do objeto.
- Pagamentos de reserva cancelada sao recusados. Somente pendentes podem ser aprovados; somente aprovados podem ser estornados.
- Pagamento implementa Cancelavel: cancelar delega a estornar. Essa interface nao permite acrescentar uma checked exception a assinatura cancelar; estado invalido gera IllegalStateException.
- Nao ha setter publico de status. aprovar e protected final; subclasses usam essa operacao controlada.
- PagamentoService fica no novo pacote br.com.hoteliara.service, pois coordenacao nao e entidade, DAO, excecao ou utilitario de API.
- O service recebe Pagamento e chama processar sem instanceof: este e o uso concreto de polimorfismo. Depois associa a reserva, evitando efeitos colaterais no construtor e associacao de pagamentos recusados.
- O service rejeita a segunda associacao da mesma instancia. Reserva.adicionarPagamento, da colega, continua publico e sem essa verificacao; outras integracoes devem seguir o service para novas operacoes.
- A restauracao estatica seleciona a subclasse e preserva ID, data e status sem cobrar novamente nem comparar pagamento historico com o saldo atual. O DAO devera associa-lo uma vez a reserva.
- Data representa criacao para um novo pagamento pendente e e atualizada na aprovacao. Restauracao aceita data nula, como o boleto pendente do SQL.

Defesa conceitual: Pagamento nao pode ser instanciado diretamente porque exige um comportamento concreto de processamento. As subclasses herdam o estado comum e sobrescrevem processar. Um servico usa o tipo Pagamento sem decidir qual forma recebeu. Campos privados e operacoes controladas protegem as mudancas de status.

### Avaliacao

- Construtor publico mantido exatamente como Cliente.avaliarHotel usa: Avaliacao(Reserva, int, String).
- Exige reserva concluida e nota de 1 a 5. Comentario limitado a 500 caracteres para corresponder ao SQL.
- editar valida todos os argumentos antes de modificar o estado; uma edicao invalida nao altera parcialmente a avaliacao.
- Quem chama associa ao hotel. Cliente.avaliarHotel ja faz essa associacao; repeti-la no construtor duplicaria a nota na colecao.
- restaurar preserva ID e data historica para o DAO; nao associa automaticamente ao hotel.
- A unicidade de uma avaliacao por reserva e garantida pelo UNIQUE no banco. A classe existente Cliente ainda permite repetir avaliacoes em memoria; isso sera tratado no fluxo de persistencia/integracao, sem alterar fontes da colega.

### Verificacao desta etapa

- Compilados model, exception, dao, service e SenhaUtil com javac --release 17: sem erros.
- Executado verificador independente com 20 verificacoes: Pix e saldo; dupla associacao; valor zero e NaN; parcelas invalidas; excesso de saldo sem associacao; boleto pendente e baixa; estorno; cartao quitando; baixa acima do saldo; restauracao historica; avaliacao apenas apos conclusao; integracao com Cliente e Hotel; edicao invalida e media atualizada.
- Verificacao apenas do dominio e infraestrutura compilavel, sem executar JavaFX, MySQL ou APIs. As telas ainda usam o modelo de demonstracao.
- Proximas entregas: DAOs e ajustes do esquema, CEP e mapa. Continuam pendentes a integracao das telas e o envio da branch.

## Etapa 5: DAOs e SQL

- Implementados PessoaDAO, HotelDAO, CategoriaDAO, QuartoDAO, ReservaDAO, PagamentoDAO e AvaliacaoDAO.
- Detalhes de configuracao, decisoes, verificacoes e limites em docs/BANCO_E_DAOS.md.
- PreparedStatement, try-with-resources, chaves geradas e tratamento de SQLException compartilhados por Jdbc.
- DadosBD reconstroi o grafo em ordem com uma instancia por ID dentro do carregamento.
- PessoaDAO usa transacao para pessoa e funcionario; falha na segunda tabela desfaz a primeira.
- Criado sql/hotel_banco_v3.sql para base nova hotel_iara_demo, sem DROP e sem executar no MySQL do usuario.
- Dependencia MySQL Connector/J 8.4.0 incluida; existencia confirmada no repositorio Maven Central.
- 35 verificacoes JDBC passaram em H2 modo MySQL; MySQL real ainda precisa de validacao.
- Proxima etapa: CepService, depois MapaService. Criacao e integracao de telas excluidas por instrucao do usuario.

## Etapa 6: CEP e localizacao sem telas

- CepService consulta ViaCEP com HttpClient, timeout e resultado imutavel DadosCep.
- MapaService usa Nominatim com User-Agent, cache, serializacao e intervalo minimo de um segundo na JVM.
- Coordenadas podem ser gravadas via localizarESalvar; persistencia antecede mudanca da entidade.
- ConsultaApiException checked comunica falhas externas sem abrir telas.
- Dependencia Gson 2.11.0 para JSON. H2 2.3.232 somente no escopo test.
- Compilacao de TODO src/main/java passou com --release 17, incluindo telas existentes e JavaFX 21.0.5.
- 29 verificacoes adicionais com HttpClient simulado e H2 passaram. Rede real nao foi validada.
- Total: 84 verificacoes. Documentacao completa em MAPA_DO_PROJETO.md, APIS.md e ENTREGA.md.
- Script PowerShell para reproduzir testes criado; comando Maven nao executado neste ambiente.
- Nao foram criadas telas, HTML Leaflet ou WebView, conforme instrucao posterior do usuario.

## Verificacao e limites anteriores (etapas 1 a 3)

- Criadas as excecoes, o contrato Dao, ConexaoBD e configuracao de exemplo; fontes anteriores preservados.
- Ainda nao ha persistencia concreta ou processamento de pagamento nesta entrega. A conexao real nao foi testada.
- Java e javac nao estavam no PATH. Localizado o javac do runtime incluido no IntelliJ: as quatro classes foram compiladas com --release 17 sem erros. Isso verifica a compilacao, nao a conexao com MySQL.
- Proxima etapa: configurar o driver MySQL e verificar a conexao; entidades e DAOs dependem da branch atualizada das colegas.

## Ajuste da entrega publica

- Revisao automatica sinalizou dados cadastrais de exemplo no SQL herdado.
- Removidos todos os INSERTs de exemplo do SQL publico; preservadas tabelas e views.
- Verificadores usam fixtures explicitamente sinteticas, dominio reservado example.test e identificadores gerados.
