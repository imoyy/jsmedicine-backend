param(
    [string]$BaseUrl = 'http://127.0.0.1:8080',
    [string]$OutputPath = 'api/api.json'
)

$ErrorActionPreference = 'Stop'

$targetPath = Join-Path $PSScriptRoot $OutputPath
$targetDirectory = Split-Path -Parent $targetPath

if (-not (Test-Path -LiteralPath $targetDirectory)) {
    New-Item -ItemType Directory -Path $targetDirectory | Out-Null
}

$apiDocsUrl = $BaseUrl.TrimEnd('/') + '/api/docs'
Invoke-WebRequest -Uri $apiDocsUrl -Headers @{ Accept = 'application/json' } -OutFile $targetPath
Write-Host "OpenAPI JSON exported to $targetPath"
