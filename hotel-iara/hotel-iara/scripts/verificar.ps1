param()
$ErrorActionPreference = 'Stop'
$projectDir = Split-Path -Parent $PSScriptRoot
$javaExe = 'java'
$javacExe = 'javac'
if ($env:JAVA_HOME) {
    $javaExe = Join-Path $env:JAVA_HOME 'bin/java.exe'
    $javacExe = Join-Path $env:JAVA_HOME 'bin/javac.exe'
}

function Executar {
    param([string]$Programa, [string[]]$Argumentos)
    & $Programa @Argumentos
    if ($LASTEXITCODE -ne 0) { throw "Falha ao executar $Programa (codigo $LASTEXITCODE)." }
}

Push-Location $projectDir
try {
    Executar 'mvn' @('-q', 'compile', 'dependency:copy-dependencies', '-DincludeScope=test')
    $classes = Join-Path $projectDir 'target/classes'
    $deps = Join-Path $projectDir 'target/dependency/*'
    $testes = Join-Path $projectDir 'target/verificacao'
    $execucao = Join-Path $projectDir 'target/verificacao-execucao'
    New-Item -ItemType Directory -Path $testes, $execucao -Force | Out-Null
    $cp = "$classes;$deps"
    Executar $javacExe @('--release', '17', '-encoding', 'UTF-8', '-cp', $cp, '-d', $testes,
        'docs/VerificarDominio.java', 'docs/VerificarPersistencia.java', 'docs/VerificarApis.java')
    $schema = Join-Path $projectDir 'docs/schema_teste.sql'
    Push-Location $execucao
    try {
        $cp = "$classes;$deps;$testes"
        Executar $javaExe @('-cp', $cp, 'VerificarDominio')
        Executar $javaExe @('-cp', $cp, 'VerificarPersistencia', $schema)
        Executar $javaExe @('-cp', $cp, 'br.com.hoteliara.util.VerificarApis', $schema)
    } finally { Pop-Location }
} finally { Pop-Location }
