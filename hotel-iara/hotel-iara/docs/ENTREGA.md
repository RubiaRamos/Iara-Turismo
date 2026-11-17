# Entrega de Du

> **Atualizacao (17/11):** depois desta entrega, as telas foram integradas ao sistema (PR #2 `integracao-telas` e PR #3 `area-funcionario`). Login, cadastro, pesquisa, reserva, pagamento, minhas reservas, area do funcionario e mapa usam os services, o model e os DAOs. O MapaService e usado pela tela de mapa. Visao geral atualizada: README da raiz.

## Incluido

- Pagamento abstrato, Pix, cartao, boleto e PagamentoService.
- Avaliacao compativel com Cliente e Hotel.
- Excecoes de pagamento, banco e consulta externa.
- Dao<T>, ConexaoBD e sete DAOs JDBC.
- SQL de demonstracao v3 sem DROP.
- CepService e MapaService, sem telas.
- Documentacao de decisoes, mapa de todo o codigo e verificadores reproduziveis.

## Requisitos

JDK 17 ou superior, Maven e internet para baixar dependencias.
MySQL 8.0.16+ para a persistencia real.
Configuracao do banco: docs/BANCO_E_DAOS.md.
APIs e limites: docs/APIS.md.
Mapa de classes e conceitos: docs/MAPA_DO_PROJETO.md.
Registro das decisoes: docs/DECISOES_DU.md.

## Verificar

Na pasta que contem pom.xml, em PowerShell:
./scripts/verificar.ps1

O script compila via Maven, prepara dependencias e executa os tres verificadores.
Requer java/javac no PATH ou JAVA_HOME apontando para um JDK.
Os testes JDBC e de API usam banco H2 temporario e HTTP simulado, sem tocar no MySQL.
Nenhuma credencial real deve ser colocada nos fontes ou no arquivo de exemplo.

Neste ambiente: compilacao completa com javac --release 17 e dependencias JavaFX/Gson obtidas do Maven Central;
24 + 43 + 29 verificacoes executadas separadamente (96 no total).
O script foi verificado sintaticamente, mas o comando Maven nao foi executado aqui por bloqueio da rede do terminal.

## Pendencias externas

- Validacao no MySQL real com db.properties local.
- Consulta real de Java as APIs, bloqueada neste ambiente.
- Criacao/integracao de telas: feita depois, nos PRs #2 e #3.
- Revisao pela equipe e merge do pull request: feito (PR #1).

Na epoca desta entrega, as telas ainda eram demonstrativas; hoje elas chamam os services, que usam os DAOs.

