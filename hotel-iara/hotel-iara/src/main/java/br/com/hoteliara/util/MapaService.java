package br.com.hoteliara.util;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import br.com.hoteliara.dao.HotelDAO;
import br.com.hoteliara.exception.ConsultaApiException;
import br.com.hoteliara.model.Endereco;
import br.com.hoteliara.model.Hotel;

// Localizacao por endereco; nao cria WebView ou qualquer outra tela.
public class MapaService {
    private static final Object CONTROLE = new Object();
    private static final Map<String, Coordenadas> CACHE = new HashMap<>();
    private static long ultimaConsulta;
    private final HttpClient cliente;
    private final URI endpoint;

    public record Coordenadas(double latitude, double longitude) {
        public Coordenadas {
            if (!Double.isFinite(latitude) || !Double.isFinite(longitude)
                    || latitude < -90 || latitude > 90 || longitude < -180 || longitude > 180)
                throw new IllegalArgumentException("Coordenadas invalidas.");
        }
    }

    public MapaService() {
        this(HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build(), URI.create(
                System.getProperty("hoteliara.nominatim.url", "https://nominatim.openstreetmap.org/search")));
    }

    MapaService(HttpClient cliente, URI endpoint) {
        this.cliente = Objects.requireNonNull(cliente);
        this.endpoint = Objects.requireNonNull(endpoint);
        if (endpoint.getQuery() != null || endpoint.getFragment() != null)
            throw new IllegalArgumentException("Endpoint nao deve incluir parametros ou fragmento.");
    }

    public Coordenadas localizar(Endereco endereco) throws ConsultaApiException {
        Coordenadas coordenadas = obter(endereco);
        endereco.setCoordenadas(coordenadas.latitude(), coordenadas.longitude());
        return coordenadas;
    }

    public Coordenadas localizarESalvar(Hotel hotel) throws ConsultaApiException {
        Objects.requireNonNull(hotel, "Hotel e obrigatorio.");
        if (hotel.getId() <= 0) throw new IllegalArgumentException("Salve o hotel antes de localizar.");
        Coordenadas coordenadas = obter(hotel.getEndereco());
        // Persiste antes de mudar a entidade: falha no banco nao simula sucesso na memoria.
        new HotelDAO().atualizarCoordenadas(hotel.getId(), coordenadas.latitude(), coordenadas.longitude());
        hotel.getEndereco().setCoordenadas(coordenadas.latitude(), coordenadas.longitude());
        return coordenadas;
    }

    private Coordenadas obter(Endereco endereco) throws ConsultaApiException {
        Objects.requireNonNull(endereco, "Endereco e obrigatorio.");
        if (endereco.temCoordenadas())
            return new Coordenadas(endereco.getLatitude(), endereco.getLongitude());
        String consulta = endereco.getEnderecoFormatado();
        String chave = endpoint + "|" + consulta;
        // Limite compartilhado entre instancias desta aplicacao (uma JVM).
        synchronized (CONTROLE) {
            Coordenadas existente = CACHE.get(chave);
            if (existente != null) return existente;
            try {
                long restante = ultimaConsulta == 0 ? 0
                        : 1_000_000_000L - (System.nanoTime() - ultimaConsulta);
                if (restante > 0) Thread.sleep(restante / 1_000_000L, (int) (restante % 1_000_000L));
                ultimaConsulta = System.nanoTime();
                URI uri = URI.create(endpoint + "?format=json&limit=1&q="
                        + URLEncoder.encode(consulta, StandardCharsets.UTF_8));
                HttpRequest pedido = HttpRequest.newBuilder(uri).timeout(Duration.ofSeconds(10))
                        .header("User-Agent", "HotelIara/1.0 (projeto academico PUCPR)")
                        .header("Accept", "application/json").GET().build();
                HttpResponse<String> resposta = cliente.send(pedido, HttpResponse.BodyHandlers.ofString());
                if (resposta.statusCode() == 429)
                    throw new ConsultaApiException("Limite de consultas ao mapa atingido. Aguarde antes de tentar novamente.");
                if (resposta.statusCode() != 200)
                    throw new ConsultaApiException("O servico de localizacao esta indisponivel.");
                JsonArray resultados = JsonParser.parseString(resposta.body()).getAsJsonArray();
                if (resultados.isEmpty())
                    throw new ConsultaApiException("Endereco nao localizado. Confira rua, numero e cidade.");
                JsonObject primeiro = resultados.get(0).getAsJsonObject();
                if (!primeiro.has("lat") || !primeiro.has("lon"))
                    throw new ConsultaApiException("O servico nao retornou coordenadas.");
                Coordenadas coordenadas = new Coordenadas(primeiro.get("lat").getAsDouble(),
                        primeiro.get("lon").getAsDouble());
                CACHE.put(chave, coordenadas);
                return coordenadas;
            } catch (IOException erro) {
                throw new ConsultaApiException("Nao foi possivel localizar o hotel. Confira a internet.", erro);
            } catch (InterruptedException erro) {
                Thread.currentThread().interrupt();
                throw new ConsultaApiException("A consulta de localizacao foi interrompida.", erro);
            } catch (com.google.gson.JsonParseException | IllegalArgumentException | IllegalStateException
                    | UnsupportedOperationException erro) {
                throw new ConsultaApiException("O servico de localizacao retornou uma resposta invalida.", erro);
            }
        }
    }
}
