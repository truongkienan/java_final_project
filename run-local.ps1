# Khoi dong toan bo 8 microservices local theo dung thu tu phu thuoc.
# Cach dung:
#   .\run-local.ps1              -> build lai roi chay
#   .\run-local.ps1 -SkipBuild   -> bo qua buoc build (dung khi da build roi, chay lai cho nhanh)

param(
    [switch]$SkipBuild
)

$ErrorActionPreference = "Stop"
$root = "F:\JAVA\final_project\source"

# Thu tu khoi dong: service khong phu thuoc ai truoc, service goi sang service khac sau
$services = @(
    @{ Name = "catalog-service";   Port = 8081 },
    @{ Name = "customer-service";  Port = 8082 },
    @{ Name = "auth-service";      Port = 8085 },
    @{ Name = "inventory-service"; Port = 8086 },
    @{ Name = "basket-service";    Port = 8083 },
    @{ Name = "payment-service";   Port = 8080 },
    @{ Name = "order-service";     Port = 8084 },
    @{ Name = "frontend-service";  Port = 8888 }
)

Write-Host "=== Buoc 1: Dam bao Redis + RabbitMQ dang chay ===" -ForegroundColor Cyan
docker start local-redis rabbitmq-dev | Out-Null

if (-not $SkipBuild) {
    Write-Host "`n=== Buoc 2: Build toan bo 8 service (mvn clean package) ===" -ForegroundColor Cyan
    Push-Location $root
    mvn clean package -DskipTests
    Pop-Location
    if ($LASTEXITCODE -ne 0) {
        Write-Host "Build that bai, dung lai." -ForegroundColor Red
        exit 1
    }
} else {
    Write-Host "`n=== Buoc 2: Bo qua build (dung jar da co san) ===" -ForegroundColor Yellow
}

Write-Host "`n=== Buoc 3: Khoi dong tung service ===" -ForegroundColor Cyan
foreach ($svc in $services) {
    $name = $svc.Name
    $port = $svc.Port
    $jar = Get-ChildItem "$root\$name\target\*.jar" -ErrorAction SilentlyContinue |
           Where-Object { $_.Name -notmatch "sources|javadoc" } |
           Select-Object -First 1

    if (-not $jar) {
        Write-Host "Khong tim thay jar cho $name trong target\ - bo qua build? Thu lai khong co -SkipBuild." -ForegroundColor Red
        continue
    }

    Write-Host "`nDang khoi dong $name (port $port)..." -ForegroundColor White
    Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$root\$name'; java -jar '$($jar.FullName)'"

    # Cho toi da 60s de port san sang truoc khi khoi dong service tiep theo
    $ready = $false
    for ($i = 0; $i -lt 60; $i++) {
        Start-Sleep -Seconds 1
        $conn = Test-NetConnection -ComputerName localhost -Port $port -WarningAction SilentlyContinue -InformationLevel Quiet
        if ($conn) { $ready = $true; break }
    }
    if ($ready) {
        Write-Host "$name da san sang (port $port)." -ForegroundColor Green
    } else {
        Write-Host "$name chua thay san sang sau 60s - kiem tra cua so log cua no. Van tiep tuc khoi dong service ke tiep." -ForegroundColor Yellow
    }
}

Write-Host "`n=== Xong! Truy cap: http://localhost:8888 ===" -ForegroundColor Cyan
Write-Host "Moi service chay trong 1 cua so PowerShell rieng - dong cua so nao la dung service do." -ForegroundColor DarkGray
