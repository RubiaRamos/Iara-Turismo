package br.com.hoteliara.util;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Objects;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import br.com.hoteliara.exception.ConsultaApiException;

public class CepService {
    private final HttpClient cliente;
    private final URI base;

    // Resultado separado de Endereco: CEP nao informa o numero do imovel.
    public record DadosCep(String cep, String logradouro, String cidade, String estado) {}

    public CepService() {
        this(HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build(),
                URI.create("https://viacep.com.br/ws/"));
    }

    // Permite testar com um servidor local, sem depender da internet.
    CepService(HttpClient cliente, URI base) {
        this.cliente = Objects.requireNonNull(cliente);
        this.base = Objects.requireNonNull(base);
    }

    public DadosCep consultar(String cep) throws ConsultaApiException {
        String entrada = cep == null ? "" : cep.trim();
        if (!entrada.matches("[0-9]{5}-?[0-9]{3}"))
            throw new IllegalArgumentException("Informe um CEP com 8 digitos.");
        String digitos = entrada.replace("-", "");
        HttpRequest pedido = HttpRequest.newBuilder(base.resolve(digitos + "/json/"))
                .timeout(Duration.ofSeconds(10)).header("Accept", "application/json").GET().build();
        try {
            HttpResponse<String> resposta = cliente.send(pedido, HttpResponse.BodyHandlers.ofString());
            if (resposta.statusCode() == 429)
                throw new ConsultaApiException("Muitas consultas de CEP. Aguarde e tente novamente.");
            if (resposta.statusCode() != 200)
                throw new ConsultaApiException("O servico de CEP esta indisponivel. Tente novamente depois.");
            JsonObject json = JsonParser.parseString(resposta.body()).getAsJsonObject();
            if (json.has("erro") && json.get("erro").getAsBoolean())
                throw new ConsultaApiException("CEP nao encontrado.");
            String cidade = campo(json, "localidade"), estado = campo(json, "uf");
            if (cidade.isBlank() || !estado.matches("[A-Z]{2}"))
                throw new ConsultaApiException("O servico de CEP retornou um endereco incompleto.");
            return new DadosCep(digitos, campo(json, "logradouro"), cidade, estado);
        } catch (IOException erro) {
            throw new ConsultaApiException("Nao foi possivel consultar o CEP. Confira a conexao com a internet.", erro);
        } catch (InterruptedException erro) {
            Thread.currentThread().interrupt();
            throw new ConsultaApiException("A consulta de CEP foi interrompida.", erro);
        } catch (com.google.gson.JsonParseException | IllegalStateException
                | UnsupportedOperationException erro) {
            throw new ConsultaApiException("O servico de CEP retornou uma resposta invalida.", erro);
        }
    }

    private static String campo(JsonObject json, String nome) {
        return !json.has(nome) || json.get(nome).isJsonNull() ? "" : json.get(nome).getAsString();
    }
}
