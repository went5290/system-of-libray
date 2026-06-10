param(
    [switch]$NoBrowser
)

$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $MyInvocation.MyCommand.Path
$backendDir = Join-Path $root "backend"
$frontendDir = Join-Path $root "frontend"

function Test-Port([int]$Port) {
    return [bool](Get-NetTCPConnection -LocalPort $Port -State Listen -ErrorAction SilentlyContinue)
}

function Wait-ForPort([int]$Port, [int]$TimeoutSeconds = 45) {
    $deadline = (Get-Date).AddSeconds($TimeoutSeconds)
    while ((Get-Date) -lt $deadline) {
        if (Test-Port $Port) { return $true }
        Start-Sleep -Seconds 1
    }
    return $false
}

if (-not (Test-Path (Join-Path $frontendDir "node_modules"))) {
    Write-Host "Installing frontend dependencies for the first run..." -ForegroundColor Cyan
    & npm.cmd install --prefix $frontendDir
    if ($LASTEXITCODE -ne 0) { throw "Failed to install frontend dependencies." }
}

if (Test-Port 8080) {
    Write-Host "Backend is already running on port 8080." -ForegroundColor Yellow
} else {
    if (-not $env:LIBRARY_DB_PASSWORD) {
        $securePassword = Read-Host "Enter the LIBRARY_APP database password" -AsSecureString
        $env:LIBRARY_DB_PASSWORD = [System.Net.NetworkCredential]::new("", $securePassword).Password
    }
    Start-Process -FilePath (Join-Path $backendDir "mvnw.cmd") `
        -ArgumentList "spring-boot:run" `
        -WorkingDirectory $backendDir `
        -WindowStyle Hidden
    if (-not (Wait-ForPort 8080)) { throw "Backend startup timed out. Check Oracle and the database password." }
    Write-Host "Backend started: http://127.0.0.1:8080/api" -ForegroundColor Green
}

if (Test-Port 5173) {
    Write-Host "Frontend is already running on port 5173." -ForegroundColor Yellow
} else {
    Start-Process -FilePath "npm.cmd" `
        -ArgumentList "run", "dev", "--", "--host", "127.0.0.1" `
        -WorkingDirectory $frontendDir `
        -WindowStyle Hidden
    if (-not (Wait-ForPort 5173)) { throw "Frontend startup timed out. Check Node.js and dependencies." }
    Write-Host "Frontend started: http://127.0.0.1:5173" -ForegroundColor Green
}

if (-not $NoBrowser) {
    Start-Process "http://127.0.0.1:5173"
}

Write-Host "Library management system is ready." -ForegroundColor Green
