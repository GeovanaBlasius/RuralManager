# Backup do banco do RuralManager (Postgres/Supabase) com pg_dump.
# Uso: powershell -File scripts\backup-banco.ps1 [-Destino C:\pasta] [-DiasParaGuardar 14]
# Le RM_DB_URL, RM_DB_USER e RM_DB_PASSWORD do .env na raiz do projeto. Precisa de pg_dump no PATH.
# O arquivo gerado tem dados pessoais: nao envie para o Git; guarde fora deste computador, de preferencia criptografado.
param(
    [string]$Destino = (Join-Path (Split-Path $PSScriptRoot -Parent) 'backups'),
    [int]$DiasParaGuardar = 14
)
$ErrorActionPreference = 'Stop'

$raiz = Split-Path $PSScriptRoot -Parent
$envArquivo = Join-Path $raiz '.env'
if (-not (Test-Path $envArquivo)) { throw ".env nao encontrado em $raiz" }
if (-not (Get-Command pg_dump -ErrorAction SilentlyContinue)) { throw 'pg_dump nao encontrado no PATH (instale o cliente do PostgreSQL).' }

$cfg = @{}
Get-Content $envArquivo | ForEach-Object {
    if ($_ -match '^\s*([A-Z0-9_]+)\s*=\s*(.*)\s*$' -and -not $_.TrimStart().StartsWith('#')) { $cfg[$Matches[1]] = $Matches[2] }
}
foreach ($k in 'RM_DB_URL', 'RM_DB_PASSWORD') { if (-not $cfg[$k]) { throw "$k nao definido no .env" } }

# jdbc:postgresql://HOST:PORTA/BANCO?sslmode=require
if ($cfg['RM_DB_URL'] -notmatch '^jdbc:postgresql://([^:/]+):?(\d+)?/([^?]+)') { throw 'RM_DB_URL em formato inesperado.' }
$hostDb = $Matches[1]; $porta = if ($Matches[2]) { $Matches[2] } else { '5432' }; $banco = $Matches[3]
$usuario = if ($cfg['RM_DB_USER']) { $cfg['RM_DB_USER'] } else { 'postgres' }

New-Item -ItemType Directory -Force -Path $Destino | Out-Null
$arquivo = Join-Path $Destino ("ruralmanager-{0}.dump" -f (Get-Date -Format 'yyyyMMdd-HHmm'))

$env:PGPASSWORD = $cfg['RM_DB_PASSWORD']
$env:PGSSLMODE = 'require'
try {
    & pg_dump --host=$hostDb --port=$porta --username=$usuario --dbname=$banco `
        --format=custom --no-owner --no-privileges --schema=public --file=$arquivo
    if ($LASTEXITCODE -ne 0) { throw "pg_dump terminou com codigo $LASTEXITCODE" }
} finally {
    Remove-Item Env:PGPASSWORD -ErrorAction SilentlyContinue
}

$tamanho = [math]::Round((Get-Item $arquivo).Length / 1KB)
if ($tamanho -lt 10) { throw "Backup suspeito: arquivo com apenas $tamanho KB ($arquivo)." }
Write-Output "Backup gerado: $arquivo ($tamanho KB)"

Get-ChildItem $Destino -Filter 'ruralmanager-*.dump' |
    Where-Object { $_.LastWriteTime -lt (Get-Date).AddDays(-$DiasParaGuardar) } |
    ForEach-Object { Remove-Item $_.FullName; Write-Output "Removido backup antigo: $($_.Name)" }
