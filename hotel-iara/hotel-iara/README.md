# Hotel Iara (JavaFX)

As instruções completas estão no [README da raiz do repositório](../../README.md).

## Resumo para rodar

1. No MySQL: rodar `sql/hotel_banco_v3.sql` e depois `sql/dados_exemplo.sql`.
2. Copiar `db.properties.exemplo` para `db.properties` (nesta pasta) e preencher usuário e senha.
3. Nesta pasta: `mvn javafx:run` — ou, no IntelliJ, rodar `br.com.hoteliara.Launcher`.

Login de teste: `ana.silva@email.com` / `123456`.

## Telas

| Tela | FXML | Controller |
|------|------|------------|
| Login | `fxml/login.fxml` | `LoginController` |
| Cadastro | `fxml/cadastro.fxml` | `CadastroController` |
| Pesquisa de quartos | `fxml/pesquisa.fxml` | `PesquisaController` |
| Detalhe do quarto + reserva | `fxml/quarto.fxml` | `QuartoController` |
| Pagamento | `fxml/pagamento.fxml` | `PagamentoController` |
| Minhas reservas | `fxml/reservas.fxml` | `ReservasController` |
| Área do funcionário | `fxml/admin.fxml` | `AdminController` |
| Mapa dos hotéis | `fxml/mapa.fxml` | `MapaController` |

Para abrir no Scene Builder: arquivos em `src/main/resources/br/com/hoteliara/fxml/`.
O CSS das telas fica em `css/estilo.css` (login, cadastro, quarto, pagamento, reservas)
e `css/pesquisa.css` (pesquisa e mapa). A biblioteca Leaflet (licença BSD) fica em `web/leaflet/`.
