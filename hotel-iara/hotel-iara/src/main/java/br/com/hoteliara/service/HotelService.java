package br.com.hoteliara.service;

import java.text.Normalizer;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import br.com.hoteliara.dao.HotelDAO;
import br.com.hoteliara.model.Hotel;
import br.com.hoteliara.model.Quarto;

/** Pesquisa de hotéis e quartos disponíveis. */
public class HotelService {

    private final HotelDAO hotelDAO = new HotelDAO();

    // Map: acha um hotel pelo id direto, sem percorrer a lista inteira.
    private final Map<Integer, Hotel> hoteisPorId = new LinkedHashMap<>();

    /** Lê os hotéis do banco (com quartos, reservas e avaliações). */
    public List<Hotel> carregarHoteis() {
        hoteisPorId.clear();
        for (Hotel hotel : hotelDAO.listar()) {
            hoteisPorId.put(hotel.getId(), hotel);
        }
        return new ArrayList<>(hoteisPorId.values());
    }

    public Hotel buscarHotel(int id) {
        return hoteisPorId.get(id);
    }

    /**
     * Quartos livres no período, que comportam os hóspedes, em hotéis cuja
     * cidade, estado ou nome contém o destino digitado. Do mais barato ao mais caro.
     */
    public List<Quarto> buscarQuartos(String destino, LocalDate checkIn, LocalDate checkOut,
                                      int hospedes) {
        if (checkIn == null || checkOut == null || !checkOut.isAfter(checkIn))
            throw new IllegalArgumentException("Escolha um check-out depois do check-in.");
        if (checkIn.isBefore(LocalDate.now()))
            throw new IllegalArgumentException("O check-in não pode ser numa data que já passou.");
        String filtro = normalizar(destino);
        List<Quarto> resultado = new ArrayList<>();
        for (Hotel hotel : carregarHoteis()) {
            String onde = normalizar(hotel.getNome() + " " + hotel.getEndereco().getCidade()
                    + " " + hotel.getEndereco().getEstado());
            if (filtro.isEmpty() || onde.contains(filtro)) {
                // A regra de disponibilidade fica no Hotel/Quarto; aqui só juntamos.
                resultado.addAll(hotel.listarQuartosDisponiveis(checkIn, checkOut, hospedes));
            }
        }
        resultado.sort(Comparator.comparingDouble(q -> q.getCategoria().getValorDiaria()));
        return resultado;
    }

    /** "São Paulo" e "sao paulo" viram a mesma coisa na busca. */
    private static String normalizar(String texto) {
        if (texto == null) return "";
        return Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase()
                .trim();
    }
}
