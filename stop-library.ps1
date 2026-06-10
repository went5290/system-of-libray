$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $MyInvocation.MyCommand.Path

function Stop-LibraryPort([int]$Port) {
    $connections = Get-NetTCPConnection -LocalPort $Port -State Listen -ErrorAction SilentlyContinue
    if (-not $connections) {
        Write-Host "Port $Port is not running." -ForegroundColor Yellow
        return
    }

    foreach ($connection in $connections) {
        $process = Get-CimInstance Win32_Process -Filter "ProcessId = $($connection.OwningProcess)"
        $commandLine = [string]$process.CommandLine
        $isLibraryProcess = $commandLine.IndexOf($root, [System.StringComparison]::OrdinalIgnoreCase) -ge 0 `
            -or $commandLine.IndexOf("com.humber.library.LibraryApplication", [System.StringComparison]::OrdinalIgnoreCase) -ge 0

        if (-not $isLibraryProcess) {
            Write-Warning "Refusing to stop port $Port because its process is not recognized as this library system."
            continue
        }

        Stop-Process -Id $connection.OwningProcess -Force
        Write-Host "Stopped service on port $Port." -ForegroundColor Green
    }
}

Stop-LibraryPort 5173
Stop-LibraryPort 8080
