$ErrorActionPreference = 'Stop'
. (Join-Path $PSScriptRoot 'entorno-local.ps1')
$taskRoot = Split-Path $PSScriptRoot -Parent
Set-Location -LiteralPath $taskRoot
$taskH2 = Get-ChildItem "$env:USERPROFILE/.m2/repository/com/h2database/h2" -Recurse -Filter 'h2-*.jar' -ErrorAction SilentlyContinue | Sort-Object FullName -Descending | Select-Object -First 1
if (-not $taskH2) { throw 'Primero inicie el backend para descargar la dependencia H2 con Maven.' }
$taskSql = Get-Content -LiteralPath 'docs/sql/07-verificacion-actividad05.sql' -Raw
& java.exe -cp $taskH2.FullName org.h2.tools.Shell -url 'jdbc:h2:file:./.local/campo/ecocompra05;MODE=MySQL;AUTO_SERVER=TRUE;IFEXISTS=TRUE' -user sa -password '' -sql $taskSql
exit $LASTEXITCODE
