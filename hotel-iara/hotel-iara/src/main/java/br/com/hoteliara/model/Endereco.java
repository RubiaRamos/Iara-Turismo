package br.com.hoteliara.model;

/**
 * Endereço de um hotel.
 *
 * Virou uma classe própria (em vez de vários atributos soltos dentro de Hotel)
 * porque é um conceito com regras e comportamento próprios: formatação e
 * coordenadas para o mapa.
 */
public class Endereco {

    private String logradouro;
    private String numero;
    private String complemento;
    private String cep;
    private String cidade;
    private String estado;
    // Double (com D maiúsculo) aceita null: o hotel pode ser cadastrado antes
    // de a API de mapas descobrir as coordenadas.
    private Double latitude;
    private Double longitude;

    public Endereco(String logradouro, String numero, String complemento,
                    String cep, String cidade, String estado) {
        setLogradouro(logradouro);
        setNumero(numero);
        setComplemento(complemento);
        setCep(cep);
        setCidade(cidade);
        setEstado(estado);
    }

    /** Ex.: "Av. Atlântica, 1500 - Apto 2, Rio de Janeiro/RJ - CEP 22021-000" */
    public String getEnderecoFormatado() {
        StringBuilder sb = new StringBuilder();
        sb.append(logradouro).append(", ").append(numero);
        if (complemento != null && !complemento.isBlank()) {
            sb.append(" - ").append(complemento);
        }
        sb.append(", ").append(cidade).append("/").append(estado);
        sb.append(" - CEP ").append(cep.substring(0, 5)).append("-").append(cep.substring(5));
        return sb.toString();
    }

    public boolean temCoordenadas() {
        return latitude != null && longitude != null;
    }

    /** Usado pela integração com a API de mapas. */
    public void setCoordenadas(double latitude, double longitude) {
        if (latitude < -90 || latitude > 90 || longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException("Coordenadas inválidas.");
        }
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // ---------- getters ----------

    public String getLogradouro() { return logradouro; }
    public String getNumero() { return numero; }
    public String getComplemento() { return complemento; }
    public String getCep() { return cep; }
    public String getCidade() { return cidade; }
    public String getEstado() { return estado; }
    public Double getLatitude() { return latitude; }
    public Double getLongitude() { return longitude; }

    // ---------- setters com validação ----------

    public void setLogradouro(String logradouro) {
        this.logradouro = obrigatorio(logradouro, "Logradouro");
    }

    public void setNumero(String numero) {
        this.numero = obrigatorio(numero, "Número");
    }

    public void setComplemento(String complemento) {
        this.complemento = complemento; // opcional
    }

    /** Aceita "22021-000" ou "22021000" e guarda só os 8 dígitos. */
    public void setCep(String cep) {
        String digitos = obrigatorio(cep, "CEP").replaceAll("\\D", "");
        if (digitos.length() != 8) {
            throw new IllegalArgumentException("CEP deve ter 8 dígitos.");
        }
        this.cep = digitos;
    }

    public void setCidade(String cidade) {
        this.cidade = obrigatorio(cidade, "Cidade");
    }

    /** Sigla com 2 letras, ex.: "PR". */
    public void setEstado(String estado) {
        String uf = obrigatorio(estado, "Estado").toUpperCase();
        if (!uf.matches("[A-Z]{2}")) {
            throw new IllegalArgumentException("Estado deve ser a sigla com 2 letras (ex.: PR).");
        }
        this.estado = uf;
    }

    private static String obrigatorio(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(campo + " é obrigatório.");
        }
        return valor.trim();
    }

    @Override
    public String toString() {
        return getEnderecoFormatado();
    }
}
