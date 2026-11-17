package br.com.hoteliara.controller;

import java.util.ArrayList;
import java.util.List;

import br.com.hoteliara.Navegador;
import br.com.hoteliara.exception.BancoDadosException;
import br.com.hoteliara.exception.ConsultaApiException;
import br.com.hoteliara.model.Hotel;
import br.com.hoteliara.service.HotelService;
import br.com.hoteliara.util.Dialogos;
import br.com.hoteliara.util.MapaService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.web.WebView;

/**
 * Mapa dos hotéis (mapa.fxml). Usa duas integrações externas:
 * - Nominatim (OpenStreetMap), pelo MapaService, para descobrir as coordenadas
 *   de hotéis que ainda não têm latitude/longitude no banco;
 * - mapas do OpenStreetMap (desenhados pelo Leaflet), baixados da internet pelo WebView.
 * A biblioteca Leaflet vai junto do projeto (resources/web/leaflet): sem internet,
 * só o fundo do mapa deixa de aparecer.
 */
public class MapaController {

    @FXML private WebView webView;
    @FXML private Label lblInfo;

    private final HotelService hotelService = new HotelService();
    private final MapaService mapaService = new MapaService();

    @FXML
    private void initialize() {
        try {
            List<Hotel> hoteis = hotelService.carregarHoteis();
            List<Hotel> noMapa = new ArrayList<>();
            for (Hotel hotel : hoteis) {
                if (!hotel.getEndereco().temCoordenadas()) {
                    try {
                        mapaService.localizarESalvar(hotel); // consulta a API e grava no banco
                    } catch (ConsultaApiException e) {
                        continue; // sem internet ou endereço não encontrado: fica fora do mapa
                    }
                }
                noMapa.add(hotel);
            }
            lblInfo.setText(noMapa.size() + " de " + hoteis.size() + " hotéis no mapa");
            webView.getEngine().loadContent(montarHtml(noMapa));
        } catch (BancoDadosException e) {
            Dialogos.erro("Sem conexão com o banco", e.getMessage());
        }
    }

    /** Página HTML com o Leaflet e um marcador para cada hotel. */
    private String montarHtml(List<Hotel> hoteis) {
        StringBuilder marcadores = new StringBuilder();
        for (Hotel h : hoteis) {
            String popup = "<b>" + escapar(h.getNome()) + "</b><br>" + h.getClassificacao() + " estrelas, nota "
                    + String.format("%.1f", h.calcularNotaMedia()) + "<br>"
                    + escapar(h.getEndereco().getEnderecoFormatado());
            marcadores.append(String.format(java.util.Locale.US,
                    "L.marker([%f, %f]).addTo(mapa).bindPopup('%s'); pontos.push([%f, %f]);%n",
                    h.getEndereco().getLatitude(), h.getEndereco().getLongitude(), popup,
                    h.getEndereco().getLatitude(), h.getEndereco().getLongitude()));
        }
        return """
                <!DOCTYPE html><html><head><meta charset="utf-8">
                <link rel="stylesheet" href="%s">
                <script src="%s"></script>
                <style>html,body,#mapa{height:100%%;margin:0}</style></head>
                <body><div id="mapa"></div><script>
                var mapa = L.map('mapa').setView([-15.8, -47.9], 4);
                L.tileLayer('https://{s}.basemaps.cartocdn.com/rastertiles/voyager/{z}/{x}/{y}.png', {
                  maxZoom: 19,
                  attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors &copy; CARTO'
                }).addTo(mapa);
                var pontos = [];
                %s
                if (pontos.length > 0) mapa.fitBounds(pontos, {padding: [60, 60], maxZoom: 15});
                </script></body></html>
                """.formatted(recurso("leaflet.css"), recurso("leaflet.js"), marcadores);
    }

    /** Endereço de um arquivo do Leaflet que está dentro do projeto. */
    private static String recurso(String arquivo) {
        return MapaController.class.getResource("/br/com/hoteliara/web/leaflet/" + arquivo).toExternalForm();
    }

    /** Evita que um nome com apóstrofo (ex.: "D'Ávila") quebre o JavaScript. */
    private static String escapar(String texto) {
        return texto.replace("\\", "\\\\").replace("'", "\\'").replace("<", "&lt;");
    }

    @FXML
    private void onVoltar() {
        Navegador.irPara("pesquisa");
    }
}
