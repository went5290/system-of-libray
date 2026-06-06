$ErrorActionPreference = "Stop"
$env:NLS_LANG = "SIMPLIFIED CHINESE_CHINA.AL32UTF8"

if (-not $env:LIBRARY_DB_PASSWORD) {
    throw "Please set the LIBRARY_DB_PASSWORD environment variable first."
}

$sqlPlus = "D:\app\humber\product\12.1.0\dbhome_1\bin\sqlplus.exe"
$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path

& $sqlPlus -L "/ as sysdba" "@$scriptDir\00-create-user.sql" $env:LIBRARY_DB_PASSWORD
if ($LASTEXITCODE -ne 0) { throw "Failed to create the LIBRARY_APP user." }

& $sqlPlus -L "LIBRARY_APP/$env:LIBRARY_DB_PASSWORD@127.0.0.1:1521/ljw4" "@$scriptDir\01-schema.sql"
if ($LASTEXITCODE -ne 0) { throw "Failed to create the database schema." }

& $sqlPlus -L "LIBRARY_APP/$env:LIBRARY_DB_PASSWORD@127.0.0.1:1521/ljw4" "@$scriptDir\02-seed.sql"
if ($LASTEXITCODE -ne 0) { throw "Failed to insert seed data." }

Write-Host "Library database initialization completed." -ForegroundColor Green
