# Banco e DAOs - como usar e o que foi decidido

## Escopo

Esta etapa nao cria nem altera telas (a integracao com as telas veio depois, no PR #2). As classes de negocio das colegas foram preservadas.
O banco nao foi configurado nem executado no MySQL do usuario.

## Preparacao no MySQL

1. Abra sql/hotel_banco_v3.sql e revise. Execute uma unica vez para criar a base nova hotel_iara_demo.
2. O script nao usa DROP. Se ja houver tabelas com esses nomes nessa base, pare e revise.
3. Copie db.properties.exemplo para db.properties ao lado do pom.xml.
4. Preencha usuario e senha locais. Nao versione esse arquivo.
5. No IntelliJ, use a pasta que contem pom.xml como Working directory da execucao.
6. Recarregue o Maven para baixar com.mysql:mysql-connector-j:8.4.0.
7. DAOs sao independentes de JavaFX e podem ser chamados por um teste, servico ou futura tela.

Nenhum DAO executa automaticamente o script SQL.

## Operacoes

HotelDAO dao = new HotelDAO();
dao.salvar(hotel); // hotel novo precisa ter id 0; recebe o ID gerado.
Hotel salvo = dao.buscarPorId(hotel.getId());

A assinatura buscarPorId retorna null se o ID nao existir; listar retorna lista vazia se nao houver registros.
Objetos relacionados precisam estar salvos antes do objeto dependente.
Ordem de insercao: hotel, categoria, quarto, cliente, reserva, pagamento, avaliacao.
PessoaDAO tambem salva Funcionario e usa uma transacao para pessoa + funcionario.
remover pode ser recusado pelo banco quando ha registros dependentes; isso vira BancoDadosException.
Atualizacoes e exclusoes nao atualizam outras instancias ja carregadas na memoria; recarregue para obter o estado salvo.

## Por que sete DAOs

Alem dos seis previstos, CategoriaDAO permite cadastrar e recuperar a categoria necessaria para salvar quartos.
Cada classe implementa Dao<T> com sua entidade. Interface e genericos garantem as assinaturas em tempo de compilacao.
Nao adicionamos uma hierarquia abstrata de DAOs apenas para exibir heranca: as entidades de pagamento ja tem um uso real dela.

## Jdbc

Classe auxiliar interna ao pacote: concentra repeticao de preenchimento de parametros, retorno de chaves geradas e fechamento.
Todo SQL usa PreparedStatement. Valores externos entram por placeholders ?, nunca por concatenacao de dados.
Apenas textos fixos de SQL sao divididos em literais para facilitar leitura.
Datas usam java.sql.Date; double monetario e convertido com BigDecimal.valueOf antes de enviar ao DECIMAL.
O modelo double foi mantido para compatibilidade. Valores com mais de duas casas podem ser arredondados pelo banco; a interface futura deve validar centavos.

## DadosBD e identidade dos objetos

Cada leitura de DAO constroi um grafo completo, em uma transacao REPEATABLE_READ:
hoteis -> categorias -> quartos -> pessoas -> reservas -> pagamentos -> avaliacoes.

Map<Integer, Entidade> conserva uma unica instancia por ID DENTRO de um carregamento.
Isso importa porque Hotel.adicionarQuarto e Cliente.adicionarReservaAoHistorico verificam referencias de objetos.
Quarto.criar escolhe a subclasse correta; Reserva.restaurar aceita datas historicas e preserva valor_total.
Reservas ativas sao registradas no quarto; cada reserva tambem entra no historico do mesmo cliente.
O estado MANUTENCAO e reaplicado somente depois de restaurar as reservas existentes.
Assim, uma manutencao atual nao impede reconstruir o historico, mas continua bloqueando novas reservas.
Pagamentos restaurados sao associados uma vez a reserva. Avaliacoes sao associadas uma vez ao hotel.

A leitura completa foi escolhida para evitar chamadas recursivas entre DAOs e pagamentos incompletos no saldo.
Custo conhecido: mesmo buscarPorId le todas as tabelas e valida suas entidades. Serve ao pequeno conjunto academico,
mas nao e adequado a grande volume. Cada chamada cria um novo grafo; identidade nao e compartilhada entre chamadas.
Um registro invalido pode impedir carregar o conjunto. A mensagem preserva a causa para diagnostico.

## Ajustes no SQL v3

- Base nova hotel_iara_demo, sem apagar a base hotel_banco.
- Numero do endereco VARCHAR: aceita s/n e valores com letras.
- Numero do quarto INT: compativel com a fabrica existente.
- Categoria com AUTO_INCREMENT, permitindo salvar e receber ID gerado.
- Estado persistido do quarto: apenas DISPONIVEL ou MANUTENCAO. OCUPADO e calculado pelas reservas.
- Telefone e nascimento da pessoa obrigatorios, como exigido pelos construtores.
- data_reserva ja existia e foi mantida.
- Unicidade de avaliacao por reserva permanece no banco.
- Cadastros de exemplo removidos do SQL publico; a base inicia vazia e recebe dados locais.

## O que isso mostra de POO

- Dao<Hotel>, Dao<Pessoa> etc.: interface e genericos.
- PessoaDAO restaura Cliente ou Funcionario e preserva seus comportamentos herdados.
- Quarto e Pagamento sao referenciados pelos tipos base e restaurados nas subclasses corretas.
- Reserva, Cliente, Quarto, Hotel, Pagamento e Avaliacao se associam por referencias.
- Collections e Map organizam os objetos; o encapsulamento das entidades continua controlando suas regras.
- Excecoes SQL sao traduzidas para BancoDadosException, preservando getCause().
- PessoaDAO usa commit/rollback porque um funcionario ocupa duas tabelas e nao deve ficar salvo pela metade.

## Validacao realizada

- Compilacao de model, exception, dao, service e SenhaUtil: javac --release 17, sem erros.
- 24 verificacoes do dominio, de pagamentos e avaliacoes, incluindo saldo com centavos.
- 43 verificacoes JDBC em H2 2.3.232 no modo MySQL, incluindo manutencao com reserva ativa.
- Verificados: vazio/ID ausente; IDs gerados; leitura polimorfica; hash de senha; rollback de funcionario;
  CRUD das entidades; identidade dentro do grafo; ocupacao; saldo; baixa e estorno persistidos;
  parcelas; nota media; avaliacao duplicada; protecao de registros dependentes; reserva historica;
  persistencia de DISPONIVEL com calculo de OCUPADO e remocao em ordem.
- Fontes dos verificadores: docs/VerificarDominio.java e docs/VerificarPersistencia.java.
- H2 e usado apenas para verificacao e foi incluido no pom.xml com scope test; nao e dependencia de producao.
- Ainda nao executado no MySQL real nem com os dados de exemplo completos da v3.
- O teste de persistencia deve rodar em uma pasta temporaria vazia. Ele aborta se encontrar db.properties,
  gera configuracao H2 somente nessa pasta e remove essa configuracao ao concluir.

## Limites de integracao

DAOs persistem o estado recebido; nao substituem o PagamentoService nem transacionam todo o fluxo em memoria.
Nao ha controle de concorrencia para duas reservas simultaneas ou dois pagamentos simultaneos: o uso previsto
e a demonstracao academica sequencial. Confirmacao de boleto continua sendo simulada.
SenhaUtil da colega usa SHA-256 simples; foi preservada para compatibilidade com o trabalho, sem afirmar adequacao a producao.
Telas, CEP e mapas nao foram ligados a estes DAOs.

