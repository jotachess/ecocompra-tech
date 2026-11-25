$ErrorActionPreference = 'Stop'
. (Join-Path $PSScriptRoot 'entorno-local.ps1')
$taskRoot = Split-Path $PSScriptRoot -Parent
Set-Location -LiteralPath $taskRoot
$taskLocal = Join-Path $taskRoot '.local'
New-Item -ItemType Directory -Force -Path $taskLocal | Out-Null
$taskConfigPath = Join-Path $taskLocal 'demo-config.json'
if (-not (Test-Path -LiteralPath $taskConfigPath)) {
    function New-DemoSecret {
        $taskBytes = New-Object byte[] 36
        $taskRng = [Security.Cryptography.RandomNumberGenerator]::Create()
        try { $taskRng.GetBytes($taskBytes); [Convert]::ToBase64String($taskBytes) }
        finally { $taskRng.Dispose() }
    }
    @{ JwtSecret=(New-DemoSecret); AdminPassword=(New-DemoSecret); VendedorPassword=(New-DemoSecret) } |
        ConvertTo-Json | Set-Content -LiteralPath $taskConfigPath -Encoding utf8
}
$taskConfig = Get-Content -LiteralPath $taskConfigPath -Raw | ConvertFrom-Json
$env:JWT_SECRET = $taskConfig.JwtSecret
$env:ADMIN_PASSWORD = $taskConfig.AdminPassword
$env:VENDEDOR_PASSWORD = $taskConfig.VendedorPassword
Write-Output 'Perfil demo con datos ficticios en H2. No utiliza MySQL.'
Write-Output 'Usuarios: admin y vendedor. Sus claves locales estan en .local/demo-config.json.'
Write-Output 'No compartir este archivo ni incluirlo en capturas o repositorios.'
& .\mvnw.cmd spring-boot:run '-Dspring-boot.run.profiles=demo'
exit $LASTEXITCODE

