# Enviar a branch de Du

## Estado

O codigo esta pronto para revisao local. A conexao GitHub desta sessao recusou criar a branch
com HTTP 403: Resource not accessible by integration. A branch pagamento-banco-api foi criada pelo usuario na base correta. Nenhum commit de Du ou PR foi publicado.
A main e as branches das colegas nao foram alteradas.

## Pelo Git, em um terminal com acesso ao GitHub

Use uma copia clonada do repositorio, nao a pasta extraida do ZIP:

```powershell
git clone https://github.com/RubiaRamos/Iara-Turismo.git
cd Iara-Turismo
git switch -c pagamento-banco-api origin/feature/classes-pessoa
git apply --check "CAMINHO_COMPLETO/Du_pagamento-banco-api.patch"
git apply "CAMINHO_COMPLETO/Du_pagamento-banco-api.patch"
git status
git add .
git commit -m "Implementa pagamentos, avaliacao, DAOs e consultas externas"
git push -u origin pagamento-banco-api
```

Substitua CAMINHO_COMPLETO pelo local real onde salvou o patch.
Se pagamento-banco-api ja existir, nao force nem sobrescreva: confira a branch existente primeiro.
Se o apply --check falhar, nao aplique a forca: a base pode ter mudado e precisa de comparacao.
Nao inclua db.properties real, senhas, target ou configuracoes pessoais do editor.
O .gitignore entregue exclui esses arquivos locais mais comuns.

No GitHub, abra um pull request em rascunho de pagamento-banco-api para main.
A branch usa feature/classes-pessoa como base porque ja inclui hotel/reservas e pessoas.
Portanto, ate as branches das colegas entrarem na main, o PR mostrara tambem essas dependencias.
O patch de Du apenas acrescenta sua parte e muda o pom.xml; nao altera fontes das colegas.

## Alternativa pelo navegador

No GitHub, selecione feature/classes-pessoa e crie a branch pagamento-banco-api a partir dela.
Extraia Du_alteracoes.zip e use Add file -> Upload files nessa branch.
Mantenha a estrutura de pastas original: hotel-iara/hotel-iara/... e .gitignore na raiz.
Confira os arquivos apresentados antes de confirmar o commit.
Depois abra o pull request em rascunho para main.

## Validacao antes do merge

Consulte docs/ENTREGA.md e rode scripts/verificar.ps1 com JDK 17 e Maven.
MySQL real e chamadas reais de API ainda precisam de validacao em uma maquina com rede.
Integracao/criacao de telas foi excluida do escopo por decisao do usuario.
