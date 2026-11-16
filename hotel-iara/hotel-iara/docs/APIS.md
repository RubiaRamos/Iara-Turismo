# CEP e localizacao: entrega sem telas

## CepService

Usa java.net.http.HttpClient e Gson para consultar ViaCEP.
consultar aceita 8 digitos ou o formato 00000-000. Letras nao sao descartadas silenciosamente.
DadosCep e um record imutavel com cep, logradouro, cidade e estado.
Nao cria Endereco automaticamente: ViaCEP nao fornece o numero do imovel.
Alguns CEPs genericos nao possuem logradouro; o resultado permite rua vazia para preenchimento manual.
CEP invalido gera IllegalArgumentException antes de usar rede.
CEP inexistente, erro HTTP, sem rede, interrupcao e JSON invalido geram ConsultaApiException.
A excecao nao abre alertas nem cria telas: quem chamar decide como informar.
Tempo limite de conexao: 5 segundos. Tempo limite de requisicao: 10 segundos.

## MapaService

localizar(Endereco) consulta coordenadas e atualiza a entidade.
localizarESalvar(Hotel) consulta, grava apenas latitude/longitude no HotelDAO e depois atualiza a entidade.
O hotel deve estar salvo antes dessa segunda operacao.
Se ja houver coordenadas, a rede nao e chamada.
Ha cache por endpoint + endereco, compartilhado entre instancias na mesma JVM.
Uma consulta por vez e pelo menos 1 segundo entre inicios; header User-Agent identifica HotelIara.
Nao existe tarefa automatica, autocomplete, varredura ou geocodificacao em lote.

O endpoint pode ser substituido sem recompilar:
-Dhoteliara.nominatim.url=https://SEU_PROVEDOR/search
O endpoint deve aceitar format=json, limit=1 e q, sem query ou fragmento na configuracao.
Os resultados fornecidos pelo provedor podem ser aproximados; nao confirmam propriedade nem endereco real.

## Uso do Nominatim publico

Politica oficial: https://operations.osmfoundation.org/policies/nominatim/
Decisao de usar este provedor veio da especificacao informada pela equipe.
Limite maximo: 1 requisicao por segundo por aplicacao, com identificacao e cache.
O controle implementado vale para uma JVM; nao distribua consultas simultaneas em computadores diferentes.
A consulta deve ser acionada para um hotel especifico; nao usar autocomplete ou rotinas periodicas.
Nao enviar dados pessoais ou sigilosos.
Dados OpenStreetMap sob ODbL; atribuicao obrigatoria em qualquer futura exibicao:
"© OpenStreetMap contributors" com link https://www.openstreetmap.org/copyright.
Nenhum mapa visual, tiles, Leaflet ou WebView foi criado, conforme exclusao de telas pelo usuario.

## Fontes

ViaCEP: https://viacep.com.br/
Politica Nominatim: https://operations.osmfoundation.org/policies/nominatim/
Connector/J Maven: https://dev.mysql.com/doc/connector-j/en/connector-j-installing-maven.html

## Verificacao

29 verificacoes com HttpClient simulado e H2:
formato de CEP, URL e timeout; sucesso com acentos; inexistente; JSON invalido;
HTTP 429/503; resposta incompleta; IOException; interrupcao preservada;
coordenadas; cache; intervalo compartilhado; User-Agent; URL encode;
resultado vazio; NaN; lat/lon ausentes; hotel sem ID; persistencia das coordenadas;
cache apos leitura do banco e falha de persistencia sem alterar o objeto.
Nao foram feitas chamadas reais de Java a ViaCEP ou Nominatim: rede bloqueada neste ambiente.
Servicos sao sincronos. Uma futura interface deve chama-los fora da thread JavaFX.

