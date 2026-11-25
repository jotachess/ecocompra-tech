# Usa las herramientas instaladas sin cambiar el PATH permanente de Windows.
if (-not (Get-Command java.exe -ErrorAction SilentlyContinue)) {
    $taskWorkspace = Split-Path (Split-Path (Split-Path $PSScriptRoot -Parent) -Parent) -Parent
    $taskJava = Get-ChildItem (Join-Path $taskWorkspace '.tools/java') -Filter java.exe -Recurse -ErrorAction SilentlyContinue | Select-Object -First 1
    if ($taskJava) {
        $env:JAVA_HOME = Split-Path (Split-Path $taskJava.FullName -Parent) -Parent
        $env:Path = "$env:JAVA_HOME\bin;$env:Path"
    }
}
if (-not (Get-Command node.exe -ErrorAction SilentlyContinue)) {
    $taskNode = Get-ChildItem "$env:ProgramFiles/WindowsApps/OpenAI.CodexPrimaryRuntime*/dependencies/node/bin/node.exe" -ErrorAction SilentlyContinue | Sort-Object FullName -Descending | Select-Object -First 1
    if ($taskNode) { $env:Path = "$(Split-Path $taskNode.FullName -Parent);$env:Path" }
}
if (-not (Get-Command pnpm.cmd -ErrorAction SilentlyContinue)) {
    $taskPnpm = Get-ChildItem "$env:ProgramFiles/WindowsApps/OpenAI.CodexPrimaryRuntime*/dependencies/bin/fallback/pnpm.cmd" -ErrorAction SilentlyContinue | Sort-Object FullName -Descending | Select-Object -First 1
    if ($taskPnpm) { $env:Path = "$(Split-Path $taskPnpm.FullName -Parent);$env:Path" }
}
