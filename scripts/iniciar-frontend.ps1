$ErrorActionPreference = 'Stop'
$taskFrontend = Join-Path (Split-Path $PSScriptRoot -Parent) 'frontend'
Set-Location -LiteralPath $taskFrontend
& pnpm.cmd install --frozen-lockfile
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
& pnpm.cmd start --host localhost --port 4200
exit $LASTEXITCODE
