package br.com.hoteliara.util;

import java.io.IOException;
import java.net.*;
import java.net.http.*;
import java.nio.file.*;
import java.sql.*;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import javax.net.ssl.*;
import br.com.hoteliara.dao.*;
import br.com.hoteliara.model.*;
import br.com.hoteliara.exception.*;

// Cliente HTTP simulado: testa o tratamento das respostas sem acessar a internet.
public class VerificarApis {
    static int checks;
    static void conferir(boolean ok, String regra) {
        if (!ok) throw new AssertionError(regra);
        checks++;
    }
    interface Acao { void executar() throws Exception; }
    static void rejeitar(Class<? extends Throwable> tipo, Acao acao) throws Exception {
        try { acao.executar(); } catch (Exception erro) {
            if (!tipo.isInstance(erro)) throw erro;
            checks++; return;
        }
        throw new AssertionError("Esperava " + tipo.getSimpleName());
    }
    static Endereco endereco(String rua) {
        return new Endereco(rua, "1", "", "80000000", "Curitiba", "PR");
    }
    static class ClienteFalso extends HttpClient {
        int status = 200, chamadas;
        String corpo = "{}";
        boolean offline, interromper;
        final List<Long> horarios = new ArrayList<>();
        final List<HttpRequest> pedidos = new ArrayList<>();
        @Override @SuppressWarnings("unchecked")
        public <T> HttpResponse<T> send(HttpRequest pedido, HttpResponse.BodyHandler<T> handler)
                throws IOException, InterruptedException {
            chamadas++; pedidos.add(pedido); horarios.add(System.nanoTime());
            if (offline) throw new IOException("Sem rede simulada");
            if (interromper) throw new InterruptedException("Interrupcao simulada");
            return (HttpResponse<T>) new Resposta(status, corpo, pedido);
        }
        public Optional<CookieHandler> cookieHandler() { return Optional.empty(); }
        public Optional<Duration> connectTimeout() { return Optional.of(Duration.ofSeconds(5)); }
        public Redirect followRedirects() { return Redirect.NEVER; }
        public Optional<ProxySelector> proxy() { return Optional.empty(); }
        public SSLContext sslContext() { throw new UnsupportedOperationException(); }
        public SSLParameters sslParameters() { return new SSLParameters(); }
        public Optional<Authenticator> authenticator() { return Optional.empty(); }
        public Version version() { return Version.HTTP_1_1; }
        public Optional<Executor> executor() { return Optional.empty(); }
        public <T> CompletableFuture<HttpResponse<T>> sendAsync(HttpRequest r, HttpResponse.BodyHandler<T> h) {
            throw new UnsupportedOperationException();
        }
        public <T> CompletableFuture<HttpResponse<T>> sendAsync(HttpRequest r, HttpResponse.BodyHandler<T> h,
                HttpResponse.PushPromiseHandler<T> p) { throw new UnsupportedOperationException(); }
    }
    record Resposta(int statusCode, String body, HttpRequest request) implements HttpResponse<String> {
        public Optional<HttpResponse<String>> previousResponse() { return Optional.empty(); }
        public HttpHeaders headers() { return HttpHeaders.of(Map.of(), (a,b) -> true); }
        public Optional<SSLSession> sslSession() { return Optional.empty(); }
        public URI uri() { return request.uri(); }
        public HttpClient.Version version() { return HttpClient.Version.HTTP_1_1; }
    }
    public static void main(String[] args) throws Exception {
        ClienteFalso cliente = new ClienteFalso();
        CepService cep = new CepService(cliente, URI.create("https://teste.invalid/ws/"));
        cliente.corpo = "{\"logradouro\":\"Praça da Sé\",\"localidade\":\"São Paulo\",\"uf\":\"SP\"}";
        CepService.DadosCep dados = cep.consultar("01001-000");
        conferir(dados.cep().equals("01001000") && dados.cidade().equals("São Paulo"), "CEP normalizado e unicode");
        conferir(cliente.pedidos.get(0).uri().getPath().equals("/ws/01001000/json/"), "URL do CEP");
        conferir(cliente.pedidos.get(0).timeout().orElseThrow().equals(Duration.ofSeconds(10)), "Timeout");
        rejeitar(IllegalArgumentException.class, () -> cep.consultar("95010A10"));
        conferir(cliente.chamadas == 1, "CEP invalido nao consulta rede");
        cliente.corpo = "{\"erro\":true}";
        rejeitar(ConsultaApiException.class, () -> cep.consultar("99999999"));
        cliente.corpo = "<html>erro</html>";
        rejeitar(ConsultaApiException.class, () -> cep.consultar("01001000"));
        cliente.status = 429;
        rejeitar(ConsultaApiException.class, () -> cep.consultar("01001000"));
        cliente.status = 503;
        rejeitar(ConsultaApiException.class, () -> cep.consultar("01001000"));
        cliente.status = 200; cliente.corpo = "{\"uf\":\"PR\"}";
        rejeitar(ConsultaApiException.class, () -> cep.consultar("01001000"));
        cliente.offline = true;
        rejeitar(ConsultaApiException.class, () -> cep.consultar("01001000"));
        cliente.offline = false; cliente.interromper = true;
        rejeitar(ConsultaApiException.class, () -> cep.consultar("01001000"));
        conferir(Thread.interrupted(), "Interrupcao preservada");
        cliente.interromper = false;
        ClienteFalso geocodificador = new ClienteFalso();
        MapaService mapa = new MapaService(geocodificador, URI.create("https://teste.invalid/search"));
        geocodificador.corpo = "[{\"lat\":\"-25.4\",\"lon\":\"-49.2\"}]";
        Endereco e = endereco("Rua de Teste");
        mapa.localizar(e);
        conferir(e.temCoordenadas() && e.getLatitude() == -25.4, "Coordenadas aplicadas");
        mapa.localizar(e); mapa.localizar(endereco("Rua de Teste"));
        conferir(geocodificador.chamadas == 1, "Cache por objeto e endereco");
        new MapaService(geocodificador, URI.create("https://teste.invalid/search")).localizar(endereco("Outra Rua"));
        conferir(geocodificador.horarios.get(1) - geocodificador.horarios.get(0) >= 950_000_000L,
                "Intervalo compartilhado entre instancias");
        conferir(geocodificador.pedidos.get(0).headers().firstValue("User-Agent").orElse("").startsWith("HotelIara/"),
                "User-Agent proprio");
        conferir(geocodificador.pedidos.get(0).uri().getRawQuery().contains("Rua+de+Teste"), "Endereco codificado");
        geocodificador.corpo = "[]";
        rejeitar(ConsultaApiException.class, () -> mapa.localizar(endereco("Vazio")));
        geocodificador.corpo = "[{\"lat\":\"NaN\",\"lon\":\"1\"}]";
        rejeitar(ConsultaApiException.class, () -> mapa.localizar(endereco("Invalido")));
        geocodificador.corpo = "INVALIDO";
        rejeitar(ConsultaApiException.class, () -> mapa.localizar(endereco("Json")));
        geocodificador.status = 429;
        rejeitar(ConsultaApiException.class, () -> mapa.localizar(endereco("Limite")));
        geocodificador.status = 200; geocodificador.corpo = "[{}]";
        rejeitar(ConsultaApiException.class, () -> mapa.localizar(endereco("Ausente")));
        rejeitar(IllegalArgumentException.class, () -> mapa.localizarESalvar(new Hotel(0, "Novo", 3, "", endereco("Novo"))));
        Path config = Path.of("db.properties");
        if (Files.exists(config)) throw new IllegalStateException("Use uma pasta temporaria vazia.");
        Files.writeString(config, "db.url=jdbc:h2:mem:api;MODE=MySQL;DB_CLOSE_DELAY=-1\ndb.usuario=sa\ndb.senha=\n");
        try {
            try (Connection c = ConexaoBD.conectar()) {
                for (String comando : Files.readString(Path.of(args[0])).split(";"))
                    if (!comando.isBlank())
                        try (PreparedStatement ps = c.prepareStatement(comando)) { ps.execute(); }
            }
            Hotel hotel = new Hotel(0, "Hotel API", 3, "", endereco("Outra Rua"));
            HotelDAO dao = new HotelDAO(); dao.salvar(hotel);
            mapa.localizarESalvar(hotel);
            Hotel lido = dao.buscarPorId(hotel.getId());
            conferir(lido.getEndereco().temCoordenadas() && lido.getEndereco().getLatitude() == -25.4, "Coordenadas persistidas");
            int antes = geocodificador.chamadas; mapa.localizarESalvar(lido);
            conferir(geocodificador.chamadas == antes, "Coordenadas do banco evitam rede");
            Hotel inexistente = new Hotel(9999, "Ausente", 3, "", endereco("Outra Rua"));
            rejeitar(IllegalArgumentException.class, () -> mapa.localizarESalvar(inexistente));
            conferir(!inexistente.getEndereco().temCoordenadas(), "Falha no banco nao muda entidade");
            geocodificador.offline = true;
            rejeitar(ConsultaApiException.class, () -> mapa.localizar(endereco("Sem Internet")));
            System.out.println(checks + " verificacoes de APIs passaram com HTTP simulado e H2.");
        } finally { Files.deleteIfExists(config); }
    }
}

