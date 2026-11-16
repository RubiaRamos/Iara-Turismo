# Hotel Iara (JavaFX)

Aplicativo de reservas com 4 telas criadas a partir dos mockups:

| Tela | Arquivo FXML | Controller |
|------|--------------|------------|
| Login | `fxml/login.fxml` | `LoginController` |
| Cadastro | `fxml/cadastro.fxml` | `CadastroController` |
| Lista de quartos | `fxml/quartos.fxml` | `QuartosController` |
| Detalhe do quarto + reserva | `fxml/quarto.fxml` | `QuartoController` |

## Como executar

Requisitos: JDK 17 ou superior e Maven.

```bash
mvn javafx:run
```

Na primeira execução o Maven baixa o JavaFX (precisa de internet).

Usuário de teste: `teste@hoteliara.com` / `123456` (ou crie outro pela tela de cadastro).

## Abrir no Scene Builder

Abra qualquer arquivo de `src/main/resources/br/com/hoteliara/fxml/` no Scene Builder.
Para o CSS aparecer lá: selecione o componente raiz → aba *Properties* → *Stylesheets* →
adicione `css/estilo.css`.

## Estrutura

```
src/main/java/br/com/hoteliara/
  Main.java                  inicia o app
  Navegador.java             troca de telas
  controller/                um controller por tela
  modelo/                    Quarto, CatalogoQuartos, RepositorioUsuarios, Sessao
  util/                      Dialogos, Imagens
src/main/resources/br/com/hoteliara/
  fxml/                      as 4 telas
  css/estilo.css             cores, fontes, botões, calendário
  img/                       logo, títulos e fotos dos quartos
```

## Observações

- A fonte do mockup parece ser geométrica; o CSS usa Poppins e, se não estiver instalada, cai para Century Gothic / Segoe UI.
- Os emojis da lista de comodidades dependem das fontes do sistema operacional.
- Os textos dos quartos 1 a 3 são exemplos; edite em `CatalogoQuartos.java`.
- Usuários e reservas ficam só em memória (somem ao fechar o app).
